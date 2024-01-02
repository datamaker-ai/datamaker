/*
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */

package ai.datamaker.generator;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.utils.schema.InferAvroSchema;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Array;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.ClassUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Options: schema definition
 */
@Slf4j
public class AvroGenerator implements DataGenerator {

  static final PropertyConfig GENERATOR_COMPRESS_CONTENT =
          new PropertyConfig("avro.generator.compress.content",
                             "Compress content",
                             PropertyConfig.ValueType.BOOLEAN,
                             false,
                             Arrays.asList(true, false));

  static final PropertyConfig AVRO_COMPRESS_CODEC =
          new PropertyConfig("avro.generator.compress.codec",
                             "Codec",
                             PropertyConfig.ValueType.STRING,
                             "null",
                             Arrays.asList("null", "deflate", "bzip2", "xz", "zstandard", "snappy"));

  @Override
  public void generate(Dataset dataset, OutputStream outputStream) throws Exception {
    generate(dataset, outputStream, JobConfig.EMPTY);
  }

  @Override
  public List<PropertyConfig> getConfigProperties() {
    List<PropertyConfig> properties = Lists.newArrayList();
    properties.add(GENERATOR_COMPRESS_CONTENT);
    properties.add(AVRO_COMPRESS_CODEC);
    return properties;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void generate(Dataset dataset, OutputStream outputStream, JobConfig config) throws Exception {
    boolean compressContent = (boolean) config.getConfigProperty(GENERATOR_COMPRESS_CONTENT);

    Schema schema = InferAvroSchema.inferFrom(dataset);

    log.debug("schema: {}", schema.toString());

    GenericDatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
    //Json.ObjectWriter objectWriter = new Json.ObjectWriter();

    //ReflectDatumWriter<GenericRecord> datumWriter = new ReflectDatumWriter<>(schema);
    try (DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(datumWriter)) {

      // Supports    *   <li>{@code null}</li>
      //   *   <li>{@code deflate}</li>
      //   *   <li>{@code snappy}</li>
      //   *   <li>{@code bzip2}</li>

      // deflate compressions level
      if (compressContent) {
        String compressionCodec = (String) config.getConfigProperty(AVRO_COMPRESS_CODEC);
        CodecFactory codecFactory = CodecFactory.fromString(compressionCodec);
        dataFileWriter.setCodec(codecFactory);
      }
      dataFileWriter.create(schema, outputStream);

      dataset.processAllValues(fieldValues -> {
        GenericRecord field = new GenericData.Record(schema);

        fieldValues.forEach(fv -> {

//        field.put(InferAvroSchema.cleanName(fv.getField().getName(),
//                                            fv.getField().getLocale()),
//                  fv.getValue());
          setValue(fv.getField(), fv.getValue(), field, schema);

        });
        try {
          dataFileWriter.append(field);

          if (dataset.getFlushOnEveryRecord()) {
            dataFileWriter.flush();
          }

        } catch (IOException e) {
          throw new IllegalStateException("Error while processing record", e);
        }
      });
    }
  }

  // FIXME Handle null values
  private void setValue(Field field, Object value, GenericRecord record, Schema schema) {
    final String name = InferAvroSchema.cleanName(field.getName(), field.getLocale());

    if (field instanceof ArrayField) {

      ArrayField af = (ArrayField) field;
      Schema arrayFieldSchema = null;

      if (schema.getField(name).schema().isNullable()) {
        int i = getIndexNotNullType(schema.getField(name).schema());
        arrayFieldSchema = schema.getField(name).schema().getTypes().get(i).getElementType();

        if (arrayFieldSchema.isNullable()) {
          i = getIndexNotNullType(arrayFieldSchema);

          arrayFieldSchema = arrayFieldSchema.getTypes().get(i);
        }
      } else {
        arrayFieldSchema = schema.getField(name).schema().getElementType();
      }

      //GenericRecord arrayFieldRecord = new GenericData.Record(arrayFieldSchema);

      List<GenericRecord> arrayFields = new ArrayList<>();

      List<Object> values = ((List) value);

      if (values.get(0) instanceof String || ClassUtils.isPrimitiveOrWrapper(values.get(0).getClass())) {
        GenericData.Array<Object> arrays = new Array<>(values.size(), arrayFieldSchema);
        arrays.addAll(values);
        record.put(name, arrays);

      } else {
        if (af.getReference() instanceof ComplexField) {
          ComplexField cf = (ComplexField)af.getReference();


          for (Object v : values) {
            Map map = (Map) v;
            GenericRecord arrayFieldRecord = new GenericData.Record(arrayFieldSchema);

            for (Field fv : cf.getReferences()) {

//            Schema childFieldSchema = null;
//            final String childName = InferAvroSchema.cleanName(fv.getName(), fv.getLocale());
//
//            if (arrayFieldSchema.getField(childName).schema().isNullable()) {
//              int i = getIndexNotNullType(arrayFieldSchema.getField(childName).schema());
//              childFieldSchema = arrayFieldSchema.getField(childName).schema().getTypes().get(i);
//            } else {
//              childFieldSchema = arrayFieldSchema.getField(childName).schema();
//            }

              setValue(fv, map.get(fv.getName()), arrayFieldRecord, arrayFieldSchema);
            }
            arrayFields.add(arrayFieldRecord);
          }

          record.put(name, arrayFields);
        }
      }

      return;
    } else if (field instanceof ComplexField) {
      ComplexField cf = (ComplexField) field;

      Schema complexFieldSchema = schema.getField(name) == null ? schema : schema.getField(name).schema();
      if (complexFieldSchema.isNullable()) {
        int i = getIndexNotNullType(schema.getField(name).schema());
        complexFieldSchema = schema.getField(name).schema().getTypes().get(i);
      }

      GenericRecord complexFieldRecord = new GenericData.Record(complexFieldSchema);

      for (Field f : cf.getReferences()) {
        Map map = (Map) value;
        setValue(f, map.get(f.getName()), complexFieldRecord, complexFieldSchema);
      }
      record.put(name, complexFieldRecord);
      return;
    }

    record.put(name, value);
  }

  private int getIndexNotNullType(Schema schema) {
    for (int i=0; i<schema.getTypes().size(); i++) {
      if (!schema.getTypes().get(i).getType().equals(Schema.Type.NULL)) {
        return i;
      }
    }
    return 0;
  }

  @Override
  public FormatType getDataType() {
    return FormatType.AVRO;
  }

}

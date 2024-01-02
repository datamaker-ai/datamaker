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
import ai.datamaker.utils.schema.InferAvroSchema;
import com.google.common.collect.Lists;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParquetGenerator implements DataGenerator {

  static final PropertyConfig GENERATOR_COMPRESS_CONTENT =
          new PropertyConfig("parquet.generator.compress.content",
                             "Compress content",
                             PropertyConfig.ValueType.BOOLEAN,
                             false,
                             Arrays.asList(true, false));

  static final PropertyConfig PARQUET_COMPRESS_CODEC =
          new PropertyConfig("parquet.generator.compress.codec",
                             "Codec",
                             PropertyConfig.ValueType.STRING,
                             CompressionCodecName.UNCOMPRESSED.toString(),
                             Arrays.stream(CompressionCodecName.values()).map(CompressionCodecName::toString).collect(Collectors.toList()));

  @Override
  public void generate(Dataset dataset, OutputStream outputStream) throws Exception {
    generate(dataset, outputStream, JobConfig.EMPTY);
  }

  @Override
  public List<PropertyConfig> getConfigProperties() {
    List<PropertyConfig> properties = Lists.newArrayList();
    properties.add(GENERATOR_COMPRESS_CONTENT);
    properties.add(PARQUET_COMPRESS_CODEC);
    return properties;
  }

  @Override
  public void generate(Dataset dataset, OutputStream outputStream, JobConfig config) throws Exception {
    final Schema schema = InferAvroSchema.inferFrom(dataset);

    boolean compressContent = (boolean) config.getConfigProperty(GENERATOR_COMPRESS_CONTENT);

    java.nio.file.Path tempParquetPath = Paths.get(Files.createTempDirectory("parquet-").toString(), "file.parquet");

    CompressionCodecName compressionCodecName = CompressionCodecName.UNCOMPRESSED;
    if (compressContent) {
      String compressionCodec = (String) config.getConfigProperty(PARQUET_COMPRESS_CODEC);
      compressionCodecName = CompressionCodecName.fromConf(compressionCodec);
    }

    try (ParquetWriter<GenericData.Record> writer = AvroParquetWriter
            .<GenericData.Record>builder(new Path(tempParquetPath.toUri()))
            //.<GenericData.Record>builder(OutputFile.nioPathToOutputFile(tempParquetFile.toPath()))
            .withSchema(schema)
            .withConf(new Configuration())
            .withCompressionCodec(compressionCodecName == null ? CompressionCodecName.UNCOMPRESSED : compressionCodecName)
            .build()) {

      dataset.processAllValues(fieldValues -> {

        GenericData.Record record = new GenericData.Record(schema);
        fieldValues.forEach(fv -> {
          record.put(InferAvroSchema.cleanName(fv.getField().getName(), fv.getField().getLocale()), fv.getValue());
        });

        try {
          writer.write(record);
        } catch (IOException e) {
          throw new IllegalStateException("Error while processing record", e);
        }
      });

    }
    Files.copy(tempParquetPath, outputStream);
    Files.deleteIfExists(tempParquetPath);
  }

  @Override
  public FormatType getDataType() {
    return FormatType.PARQUET;
  }
}

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

package ai.datamaker.utils.schema;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.formatter.DateTimeStringFormatter;
import ai.datamaker.model.field.formatter.FieldFormatter;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.ChoiceField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.DateTimeField;
import ai.datamaker.model.field.type.DurationField;
import ai.datamaker.model.field.type.UuidField;
import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.commons.lang3.ClassUtils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public final class InferAvroSchema {

    private InferAvroSchema() {

    }

    private static <V> String getType(Class<V> objectType) {
        if (objectType == Byte.class) {
            return "bytes";
        }
        if (objectType == Boolean.class) {
            return "boolean";
        }
        if (objectType == Long.class) {
            return "long";
        }
        if (objectType == Integer.class) {
            return "int";
        }
        if (objectType == String.class) {
            return "string";
        }
        if (objectType == Double.class) {
            return "double";
        }
        if (objectType == Float.class) {
            return "float";
        }
        if (objectType == Void.class) {
            return "null";
        }
        return "null";
    }

    private static <V> void getType(Class<V> objectType, SchemaBuilder.FieldBuilder<Schema> typeBuilder) {
        if (objectType == Byte.class) {
            typeBuilder.type().nullable().bytesType().noDefault();
        }
        if (objectType == Boolean.class) {
            typeBuilder.type().nullable().booleanType().noDefault();
        }
        if (objectType == Long.class) {
            typeBuilder.type().nullable().longType().noDefault();
        }
        if (objectType == Integer.class) {
            typeBuilder.type().nullable().intType().noDefault();
        }
        if (objectType == String.class) {
            typeBuilder.type().nullable().stringType().noDefault();
        }
        if (objectType == Double.class) {
            typeBuilder.type().nullable().doubleType().noDefault();
        }
        if (objectType == Float.class) {
            typeBuilder.type().nullable().floatType().noDefault();
        }
        if (objectType == Void.class) {
            typeBuilder.type().nullable().nullType().nullDefault();
        }
    }

    /**
     * ([A-Za-z_][A-Za-z0-9_]*)
     *
     * @param originalValue
     * @return
     */
    public static String cleanName(final String originalValue, final Locale locale) {

        return Normalizer.normalize(originalValue, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replaceAll("\\p{Punct}","")
            .replaceAll("\\p{Space}", "_");
    }

    /**
     * @deprecated field should not return array or map directly except for complex and array fields
     * @param name
     * @param isNullable
     * @param value
     * @param fieldsBuilder
     * @return
     */
//    @Deprecated
//    public static Schema inferFrom(final String name, final boolean isNullable, final Object value, final SchemaBuilder.FieldAssembler<Schema> fieldsBuilder) {
//        if (value instanceof String || ClassUtils.isPrimitiveOrWrapper(value.getClass())) {
//            String type = getType(value.getClass());
//            if (isNullable) {
//                getType(value.getClass(), fieldsBuilder.name(name));
//            } else {
//                fieldsBuilder.name(name).type(type).noDefault();
//            }
//        } else if (value instanceof Enum) {
//            ChoiceField cf = (ChoiceField)value;
//            String[] choices = cf.getChoices().stream().map(String::valueOf).collect(Collectors.toList()).toArray(new String[]{});
//            fieldsBuilder.name(name).type().enumeration(name).symbols(choices);
//
//        } else if (value.getClass().isArray() || value instanceof List) {
//
//            List<Object> values = (List<Object>) value;
//            // infer type
//            if (isNullable) {
//                fieldsBuilder.name(name).type().unionOf().array().items(inferFrom(name, isNullable, values.get(0), fieldsBuilder)).and().nullType().endUnion().noDefault();
//            } else {
//                fieldsBuilder.name(name).type().array().items(inferFrom(name, isNullable, values.get(0), fieldsBuilder));
//            }
//
//        } else if (value instanceof Map) {
//            Map<Object, Object> values = (Map<Object, Object>) value;
//            // infer type
//
//            Object firstObject = values.values().iterator().next();
//            boolean allTheSameInstanceType = values.values().stream().anyMatch(testObject -> !(testObject.getClass().isInstance(firstObject)));
//
//            if (allTheSameInstanceType) {
//                if (isNullable) {
//                    fieldsBuilder.name(name).type().unionOf().map().values(inferFrom(name, isNullable, firstObject, fieldsBuilder)).and().nullType().endUnion().noDefault();
//                } else {
//                    fieldsBuilder.name(name).type().map().values(inferFrom(name, isNullable, firstObject, fieldsBuilder)).noDefault();
//                }
//            }
//
//        } else {
//            fieldsBuilder.requiredBytes(name);
//        }
//
//        return fieldsBuilder.endRecord();
//    }

    public static SchemaBuilder.FieldAssembler<Schema> inferFrom(final Field f, final SchemaBuilder.FieldAssembler<Schema> fieldsBuilder) {

        String cleanFieldName = cleanName(f.getName(), f.getLocale());

        if (String.class == f.getObjectType() || ClassUtils.isPrimitiveOrWrapper(f.getObjectType())) {
            String type = getType(f.getObjectType());
            if (f.getIsNullable()) {
                getType(f.getObjectType(), fieldsBuilder.name(cleanFieldName));
            } else {
                fieldsBuilder.name(cleanFieldName).type(type).noDefault();
            }
        } else if (f instanceof ArrayField) {
            ArrayField af = (ArrayField)f;

            SchemaBuilder.FieldAssembler<Schema> complexFieldsBuilder = SchemaBuilder
                    .record(cleanFieldName)
                    .fields();

            Schema schema = inferFrom(af.getReference(), complexFieldsBuilder).endRecord();
            if (schema.getFields().size() == 1 || schema.getFields().get(0).schema().equals(schema.getFields().get(1).schema())) {
                schema = schema.getFields().get(0).schema();
            }

            // build list
            if (af.getIsNullable()) {
                fieldsBuilder.name(cleanFieldName).type().unionOf().array().items(schema).and().nullType().endUnion().noDefault();
            } else {
                fieldsBuilder.name(cleanFieldName).type().array().items(schema).noDefault();
            }

        } else if (f instanceof ComplexField) {
            // Test if object type is primitive or not
            ComplexField cf = (ComplexField)f;

            // Test if all the values are the same type
            //Field firstField = cf.getValues().get(0);
            //boolean allTheSameInstanceType = cf.getValues().stream().allMatch(testField -> testField.getClass().isInstance(firstField));
            SchemaBuilder.FieldAssembler<Schema> complexFieldsBuilder = SchemaBuilder
                    .record(cleanFieldName)
                    .fields();

            // TODO should support MAP?
//            if (cf.getValues().size() == 1) {
//                Schema schema = inferFrom(cf.getValues().get(0), complexFieldsBuilder).endRecord().getFields().get(0).schema();
//                if (cf.isNullable()) {
//                    fieldsBuilder.name(cleanFieldName).type().unionOf().map().values(schema).and().nullType().endUnion().noDefault();
//                } else {
//                    fieldsBuilder.name(cleanFieldName).type().map().values(schema).noDefault();
//                }
//            } else {

                cf.getReferences().forEach(cfield -> {
                   inferFrom(cfield, complexFieldsBuilder);
                });

                if (cf.getIsNullable()) {
                    fieldsBuilder.name(cleanFieldName).type().unionOf().nullType().and().type(complexFieldsBuilder.endRecord()).endUnion().noDefault();
                } else {
                    fieldsBuilder.name(cleanFieldName).type(complexFieldsBuilder.endRecord()).noDefault();
                }
            //}
            // build map or multiple records
            //A map is an associative array, or dictionary, that organizes data as key-value pairs. The key for an Avro map must be a string. Avro maps supports only one attribute: values. This attribute is required and it defines the type for the value portion of the map.

        } else if (f instanceof ChoiceField) {
            ChoiceField cf = (ChoiceField)f;
            String[] choices = cf.getChoices().stream().map(String::valueOf).collect(Collectors.toList()).toArray(new String[]{});
            fieldsBuilder.name(cleanFieldName).type().enumeration(cleanFieldName).symbols(choices);

//        } else if (f.getObjectType().isArray() || f.getObjectType() == List.class) {
//
//            List<Object> values = (List<Object>) f.getData();
//            SchemaBuilder.FieldAssembler<Schema> complexFieldsBuilder = SchemaBuilder
//                    .record(cleanFieldName)
//                    .fields();
//            Schema schema = inferFrom(cleanFieldName, f.isNullable(), values.get(0), complexFieldsBuilder);
//
//            // infer type
//            if (f.isNullable()) {
//                fieldsBuilder.name(cleanFieldName).type().unionOf().array().items(schema).and().nullType().endUnion().noDefault();
//            } else {
//                fieldsBuilder.name(cleanFieldName).type().array().items(schema);
//            }
//
//        } else if (f.getObjectType() == Map.class) {
//            Map<Object, Object> values = (Map<Object, Object>) f.getData();
//            // infer type
//
//            Object firstObject = values.values().iterator().next();
//            boolean allTheSameInstanceType = values.values().stream().anyMatch(testObject -> !(testObject.getClass().isInstance(firstObject)));
//
//            if (allTheSameInstanceType) {
//                if (f.isNullable()) {
//                    fieldsBuilder.name(cleanFieldName).type().unionOf().map().values(inferFrom(cleanFieldName, f.isNullable(), firstObject, fieldsBuilder)).and().nullType().endUnion().noDefault();
//                } else {
//                    fieldsBuilder.name(cleanFieldName).type().map().values(inferFrom(cleanFieldName, f.isNullable(), firstObject, fieldsBuilder)).noDefault();
//                }
//            }

        } else if (f.getObjectType() == Object.class) {
            // bytes[]
            fieldsBuilder.requiredBytes(f.getName());
        } else if (f instanceof DateTimeField) {
            DateTimeField dtf = (DateTimeField)f;
            FieldFormatter ff = dtf.getFormatter();
            if (ff instanceof DateTimeStringFormatter) {
                if (f.getIsNullable()) {
                    fieldsBuilder.name(cleanFieldName).type().nullable().stringType().noDefault();
                } else {
                    fieldsBuilder.name(cleanFieldName).type().stringType().noDefault();
                }
            } else {
                if (f.getIsNullable()) {
                    fieldsBuilder.name(cleanFieldName).type().nullable().longType().noDefault();
                } else {
                    fieldsBuilder.name(cleanFieldName).type().longType().noDefault();
                }            }
        } else if (f instanceof DurationField) {
            if (f.getIsNullable()) {
                fieldsBuilder.name(cleanFieldName).type().nullable().fixed(cleanFieldName).size(12).noDefault();
            } else {
                fieldsBuilder.name(cleanFieldName).type().fixed(cleanFieldName).size(12).noDefault();
            }
        } else if (f instanceof UuidField) {
            if (f.getIsNullable()) {
                fieldsBuilder.name(cleanFieldName).type().nullable().stringType().noDefault();
            } else {
                fieldsBuilder.name(cleanFieldName).type().stringType().noDefault();
            }
        }

        return fieldsBuilder;
    }

    public static Schema inferFrom(Dataset dataset) {

        SchemaBuilder.FieldAssembler<Schema> fieldsBuilder = SchemaBuilder
                .record(cleanName(dataset.getName(), dataset.getLocale()))
                //.namespace("??")
                .fields();

        dataset.getFields()
                .stream()
                .filter(f -> !f.getIsNested())
                .forEach(f -> inferFrom(f, fieldsBuilder));

        return fieldsBuilder.endRecord();
    }
}

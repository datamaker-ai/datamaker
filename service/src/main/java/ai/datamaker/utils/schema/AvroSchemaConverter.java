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
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.BooleanField;
import ai.datamaker.model.field.type.BytesField;
import ai.datamaker.model.field.type.ChoiceField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.ConstantField;
import ai.datamaker.model.field.type.DoubleField;
import ai.datamaker.model.field.type.FloatField;
import ai.datamaker.model.field.type.IntegerField;
import ai.datamaker.model.field.type.LongField;
import ai.datamaker.model.field.type.NullField;
import ai.datamaker.model.field.type.TextField;
import ai.datamaker.service.FieldDetectorService;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@SuppressWarnings("unchecked")
@Component
public class AvroSchemaConverter {

    @Autowired
    private FieldDetectorService fieldDetectorService;

    public Dataset convertFrom(Schema schema, Locale locale) {
        Dataset dataset = new Dataset();
        dataset.setName(schema.getName());

        schema.getFields().forEach(f -> getField(f.name(), f.schema(), f.doc(), locale).ifPresent(dataset::addField));

        return dataset;
    }

    public Optional<Field> getField(final String name, final Schema schema, String comment, final Locale locale) {
        boolean isNullable = schema.isNullable();
        Field detectedField = null;

        Optional<Field> fieldDetectedOnName = fieldDetectorService.detectTypeOnName(name, locale);

        switch (schema.getType()) {

            case RECORD:
                ComplexField recordField = new ComplexField(name, locale);
                // record
                schema.getFields().forEach(f -> getField(f.name(), f.schema(), f.doc(), locale).ifPresent(recordField.getReferences()::add));
                detectedField = recordField;
                break;
            case ENUM:
                detectedField = new ChoiceField(name, locale);
                break;
            case ARRAY:
                ArrayField arrayField = new ArrayField(name, locale);
                try {
                    Schema arraySchema = schema.getElementType();
                    getField(name, arraySchema, comment, locale).ifPresent(arrayField::setReference);

                } catch (AvroRuntimeException are) {
                    log.warn("Element is not array type", are);
                }
                detectedField = arrayField;
                break;
            case MAP:
                ComplexField complexField = new ComplexField(name, locale);
                try {
                    Schema mapSchema = schema.getValueType();
                    getField(name, mapSchema, comment, locale).ifPresent(complexField.getReferences()::add);

                } catch (AvroRuntimeException are) {
                    log.warn("Element is not map type", are);
                }
                detectedField = complexField;
                break;
            case UNION:
                // ComplexField unionField = new ComplexField(name, locale);
                detectedField = schema
                    .getTypes()
                    .stream()
                    .filter(s -> s.getType() != Type.NULL)
                    .map(s -> {
                        Optional<Field> sField = getField(name, s, comment, locale);
                        return sField.orElse(null);
                    })
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
                //detectedField = unionField;
                break;
            case FIXED:
                detectedField = new ConstantField(name, locale);
                break;
            case STRING:
                detectedField = new TextField(name, locale);
                break;
            case BYTES:
                detectedField = new BytesField(name, locale);
                break;
            case INT:
                detectedField = new IntegerField(name, locale);
                break;
            case LONG:
                detectedField = new LongField(name, locale);
                break;
            case FLOAT:
                detectedField = new FloatField(name, locale);
                break;
            case DOUBLE:
                detectedField = new DoubleField(name, locale);
                break;
            case BOOLEAN:
                detectedField = new BooleanField(name, locale);
                break;
            case NULL:
                detectedField = new NullField(name, locale);
                break;
        }

        Optional<Field> bestFieldMatch = fieldDetectorService.findBestMatch(fieldDetectedOnName, Optional.ofNullable(detectedField));

        if (bestFieldMatch.isPresent()) {
            detectedField = bestFieldMatch.get();
            detectedField.setDescription(comment);
            detectedField.setIsNullable(isNullable);
            return Optional.of(detectedField);
        }

        return Optional.empty();
    }

}

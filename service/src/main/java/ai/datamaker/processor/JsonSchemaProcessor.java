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

package ai.datamaker.processor;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.SupportedMediaType;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.BooleanField;
import ai.datamaker.model.field.type.ChoiceField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.ConstantField;
import ai.datamaker.model.field.type.DoubleField;
import ai.datamaker.model.field.type.IntegerField;
import ai.datamaker.model.field.type.NullField;
import ai.datamaker.model.field.type.TextField;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.ConstSchema;
import org.everit.json.schema.EnumSchema;
import org.everit.json.schema.NullSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class JsonSchemaProcessor extends DatasetProcessor {

    /**
     * Sr.No.	Keyword & Description
     * 1
     * $schema
     *
     * The $schema keyword states that this schema is written according to the draft v4 specification.
     *
     * 2
     * title
     *
     * You will use this to give a title to your schema.
     *
     * 3
     * description
     *
     * A little description of the schema.
     *
     * 4
     * type
     *
     * The type keyword defines the first constraint on our JSON data: it has to be a JSON Object.
     *
     * 5
     * properties
     *
     * Defines various keys and their value types, minimum and maximum values to be used in JSON file.
     *
     * 6
     * required
     *
     * This keeps a list of required properties.
     *
     * 7
     * minimum
     *
     * This is the constraint to be put on the value and represents minimum acceptable value.
     *
     * 8
     * exclusiveMinimum
     *
     * If "exclusiveMinimum" is present and has boolean value true, the instance is valid if it is strictly greater than the value of "minimum".
     *
     * 9
     * maximum
     *
     * This is the constraint to be put on the value and represents maximum acceptable value.
     *
     * 10
     * exclusiveMaximum
     *
     * If "exclusiveMaximum" is present and has boolean value true, the instance is valid if it is strictly lower than the value of "maximum".
     *
     * 11
     * multipleOf
     *
     * A numeric instance is valid against "multipleOf" if the result of the division of the instance by this keyword's value is an integer.
     *
     * 12
     * maxLength
     *
     * The length of a string instance is defined as the maximum number of its characters.
     *
     * 13
     * minLength
     *
     * The length of a string instance is defined as the minimum number of its characters.
     *
     * 14
     * pattern
     *
     * A string instance is considered valid if the regular expression matches the instance successfully.
     * @param jsonInput
     * @param config
     * @return
     */

    @Override
    public Optional<Dataset> process(InputStream jsonInput, JobConfig config) {
        String datasetName = (String) config.getConfigProperty(INPUT_FILENAME_PROPERTY);
        Locale locale = getLocale(config);
        Dataset dataset = new Dataset(datasetName, locale);
        dataset.setExportHeader(false);
        try {
            JSONObject rawSchema = new JSONObject(new JSONTokener(jsonInput));
            Schema schema = SchemaLoader.load(rawSchema);
            if (StringUtils.isNotBlank(schema.getId())) {
                dataset.setName(schema.getId());
            }
            dataset.setDescription(schema.getDescription());
            //schema.validate(new JSONObject("{\"hello\" : \"world\"}")); // throws a ValidationException if this object is invalid

            getFieldForSchema(dataset, schema, schema.getTitle()).ifPresent(dataset::addField);

            return Optional.of(dataset);
        } catch (Exception e) {
            throw new IllegalStateException("Error while processing json schema file", e);
        }

        // type: String values MUST be one of the six primitive types ("null", "boolean", "object", "array", "number", or "string"), or "integer" which matches any number with a zero fractional part.
    }

    Optional<Field> getFieldForSchema(Dataset dataset, Schema schema, String name) {

        if (schema instanceof ObjectSchema) {
            ObjectSchema objectSchema = (ObjectSchema)schema;
            ComplexField complexField = new ComplexField("complex", dataset.getLocale());
            complexField.setDataset(dataset);
            Integer max = objectSchema.getMaxProperties();
            Integer min = objectSchema.getMinProperties();
            List<String> required = objectSchema.getRequiredProperties();

            objectSchema.getPropertySchemas().forEach((k, v) -> {
                log.debug("Key: {}", k);

                getFieldForSchema(dataset, v, k).ifPresent(sf -> {

                    if (!required.contains(k) || (v.isNullable() != null && v.isNullable())) {
                        sf.setIsNullable(true);
                    }
                    sf.setDataset(dataset);
                    sf.setIsNested(true);
                    sf.setName(k);
                    sf.setPosition(complexField.getReferences().size() + 1);
                    complexField.getReferences().add(sf);
                });
            });
            return Optional.of(complexField);
        } else if (schema instanceof ArraySchema) {
            ArraySchema arraySchema = (ArraySchema)schema;
            ArrayField arrayField = new ArrayField(arraySchema.getTitle(), dataset.getLocale());
            arrayField.setDataset(dataset);
            arrayField.setDescription(arraySchema.getDescription());
            if (arraySchema.getMaxItems() != null) {
                arrayField.setNumberOfElements(arraySchema.getMaxItems());
            }
            if (CollectionUtils.isNotEmpty(arraySchema.getItemSchemas())) {
                ComplexField complexField = new ComplexField("complex", dataset.getLocale());

                arraySchema.getItemSchemas().forEach(s -> {
                    getFieldForSchema(dataset, s, name).ifPresent(sf -> {
                        if (s.isNullable() != null && s.isNullable()) {
                            sf.setIsNullable(true);
                        }
//                        sf.setName(k);
                        sf.setDataset(dataset);
                        sf.setIsNested(true);
                        sf.setPosition(complexField.getReferences().size() + 1);
                        complexField.getReferences().add(sf);
                    });
                });

                arrayField.setReference(complexField);
            } else {
                getFieldForSchema(dataset, arraySchema.getAllItemSchema(), name).ifPresent(af -> {
                    af.setIsNested(true);
                    arrayField.setReference(af);
                });
            }

            return Optional.of(arrayField);
        } else if (schema instanceof BooleanSchema) {
            BooleanSchema booleanSchema = (BooleanSchema)schema;
            BooleanField booleanField = new BooleanField(booleanSchema.getTitle(), dataset.getLocale());
            booleanField.setDescription(booleanSchema.getDescription());
            booleanField.setDataset(dataset);
            return Optional.of(booleanField);
        } else if (schema instanceof ConstSchema) {
            ConstSchema constSchema = (ConstSchema)schema;
            ConstantField constantField = new ConstantField(constSchema.getTitle(), dataset.getLocale());
            constantField.setDescription(constSchema.getDescription());
            constantField.setValue(constSchema.getPermittedValue());
            constantField.setDataset(dataset);
            return Optional.of(constantField);
        } else if (schema instanceof NumberSchema) {
            NumberSchema numberSchema = (NumberSchema)schema;

            Number max = numberSchema.getMaximum();
            Number min = numberSchema.getMinimum();

            if (numberSchema.requiresInteger()) {
                IntegerField integerField = new IntegerField(numberSchema.getTitle(), dataset.getLocale());
                if (min != null) {
                    integerField.getConfig().put(IntegerField.MIN_VALUE_PROPERTY, min.intValue());
                }
                if (max != null) {
                    integerField.getConfig().put(IntegerField.MAX_VALUE_PROPERTY, max.intValue());
                }
                integerField.setDescription(schema.getDescription());
                integerField.setDataset(dataset);
                return Optional.of(integerField);
            }
            DoubleField doubleField = new DoubleField(numberSchema.getTitle(), dataset.getLocale());
            if (min != null) {
                doubleField.getConfig().put(DoubleField.MIN_VALUE_PROPERTY, min.intValue());
            }
            if (max != null) {
                doubleField.getConfig().put(DoubleField.MAX_VALUE_PROPERTY, max.intValue());
            }
            doubleField.setDescription(schema.getDescription());
            doubleField.setDataset(dataset);
            return Optional.of(doubleField);

        } else if (schema instanceof ReferenceSchema) {
            ReferenceSchema referenceSchema = (ReferenceSchema)schema;
            return getFieldForSchema(dataset, referenceSchema.getReferredSchema(), name);

        } else if (schema instanceof StringSchema) {
            StringSchema stringSchema = (StringSchema)schema;
            TextField stringField = new TextField(stringSchema.getTitle(), dataset.getLocale());
            if (stringSchema.getMaxLength() != null) {
                stringField.setLength(stringSchema.getMaxLength());
            }
            stringField.setDescription(schema.getDescription());
            stringField.setDataset(dataset);

            Optional<Field> fieldOnName = fieldDetectorService.detectTypeOnName(name, dataset.getLocale());
            Optional<Field> bestMatch = fieldDetectorService.findBestMatch(fieldOnName, Optional.of(stringField));

            return bestMatch;
        } else if (schema instanceof EnumSchema) {
            EnumSchema enumSchema = (EnumSchema)schema;
            ChoiceField choiceField = new ChoiceField(enumSchema.getTitle(), dataset.getLocale());
            choiceField.setDescription(enumSchema.getDescription());
            choiceField.setDataset(dataset);
            return Optional.of(choiceField);
        } else if (schema instanceof NullSchema) {
            NullSchema nullSchema = (NullSchema)schema;
            NullField nullField = new NullField(nullSchema.getTitle(), dataset.getLocale());
            nullField.setDescription(nullSchema.getDescription());
            nullField.setDataset(dataset);
            return Optional.of(nullField);
        }

        return Optional.empty();
    }

    @Override
    public Set<SupportedMediaType> supportedTypes() {
        return Sets.newHashSet(SupportedMediaType.JSON_SCHEMA);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Collections.emptyList();
    }
}

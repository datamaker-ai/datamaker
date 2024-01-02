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

package ai.datamaker.service;

import ai.datamaker.model.Constants;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldMapping;
import ai.datamaker.model.field.constraint.RangeConstraint;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.BigIntegerField;
import ai.datamaker.model.field.type.BooleanField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.DateTimeField;
import ai.datamaker.model.field.type.DateTimeField.DateType;
import ai.datamaker.model.field.type.DecimalField;
import ai.datamaker.model.field.type.DoubleField;
import ai.datamaker.model.field.type.EmptyField;
import ai.datamaker.model.field.type.FloatField;
import ai.datamaker.model.field.type.IntegerField;
import ai.datamaker.model.field.type.LongField;
import ai.datamaker.model.field.type.MoneyField;
import ai.datamaker.model.field.type.NullField;
import ai.datamaker.model.field.type.TextField;
import ai.datamaker.model.field.type.TextField.TextType;
import ai.datamaker.model.forms.FieldForm;
import ai.datamaker.repository.FieldMappingRepository;
import ai.datamaker.utils.model.FieldFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class FieldDetectorService {

    @Value("${field.detector.number.items}")
    private Integer numberItemsToProcess = 10;

    static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(""
            + "[yyyy/MM/dd HH:mm:ss.SSSSSS]"
            + "[yyyy-MM-dd'T'HH:mm:ssX]"
            + "[yyyy-MM-dd HH:mm:ss[.SSS][ z][Z]]"
            + "[ddMMMyyyy:HH:mm:ss.SSS[ Z]]"
            + "[dd.MM.yyyy]"
            + "[d. MMMM yyyy]"
            + "[yyyy-MM-dd]"
            + "[M/d/yyyy]"
            + "[dd/MM/yyyy]"
            + "[MMMM d, yyyy]"
            + "[MMM-dd-yyyy]"
            + "[H:m[:s]]"
            + "[dd-MMM-yyyy]").withZone(ZoneId.of("UTC"));

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // private Map<Pair<String, Locale>, Pair<Class <? extends Field>, String>> synonymsPerLang = Maps.newHashMap();

    @Autowired
    private FieldMappingRepository fieldMappingRepository;

    private Map<String, String> fieldMappings;

    public static String getKey(String originalValue, Locale locale) {
        return Normalizer.normalize(originalValue, Normalizer.Form.NFD)
                .toLowerCase(locale)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\p{Punct}","")
                .replaceAll("\\p{Space}", "") +
                "-" + locale.toLanguageTag();
    }

    public enum NotFoundReason {
        NO_MATCH("match.reason.nomatch");

        @Getter
        private String key;

        NotFoundReason(String key) {
            this.key = key;
        }
    }

    @SuppressWarnings("unchecked")
    public void init() {
        log.info("Initializing field mappings");
        fieldMappings = StreamSupport
            .stream(fieldMappingRepository.findAll().spliterator(), false)
            .collect(Collectors.toMap(FieldMapping::getMappingKey, FieldMapping::getFieldJson));
        log.debug("Field mappings contains: {}", fieldMappings.size());
    }

    public Optional<Field> detect(final String name, Locale locale, Collection<Object> values) {
        // Try with name first
        Optional<Field> fieldFoundName = detectTypeOnName(name, locale);

        // Try best match with all values
        Optional<Field> fieldFoundValue = detectTypeOnValue(name, locale, values);

        return findBestMatch(fieldFoundName, fieldFoundValue);
    }

    public Optional<Field> findBestMatch(Optional<Field> fieldFoundName, Optional<Field> fieldFoundValue) {

        // Reconcile
        if (fieldFoundName.isEmpty() && fieldFoundValue.isEmpty()) {
            log.debug( "no match");
            return Optional.empty();
        }

        //
        if (fieldFoundName.isEmpty()) {
            log.debug("match based on value");
            return fieldFoundValue;
        }

        if (fieldFoundValue.isEmpty()) {
            log.debug("match based on name");
            return fieldFoundName;
        }

        Field foundName = fieldFoundName.get();
        Field foundValue = fieldFoundValue.get();

        if (foundName.getObjectType() != foundValue.getObjectType()) {
            log.debug("Object type do not matches: name type={}, value type={}",
                foundName.getObjectType().getSimpleName(),
                foundValue.getObjectType().getSimpleName());

            return fieldFoundValue;
        }

        if (foundName.getClass() != foundValue.getClass()) {
            // What to do?
            // Example: name is birthday mapped to date but value found is boolean...
            String reason = String.format("field matching clash for %s, name matched=%s, values matched=%s",
                foundName.getName(),
                foundName.getClass().getSimpleName(),
                foundValue.getClass().getSimpleName());

            log.debug(reason);

            return fieldFoundName;
        }

        return fieldFoundName;
    }

    public Optional<Field> detectTypeOnName(final String name, Locale locale) {

        // Clean name
        String key = getKey(name, locale);
        if (!fieldMappings.containsKey(key)) {
            key = getKey(name, Locale.forLanguageTag(locale.getLanguage()));
        }

        if (fieldMappings.containsKey(key)) {
            String fieldFound = fieldMappings.get(key);

            try {
                FieldForm fieldForm = OBJECT_MAPPER.readValue(fieldFound, FieldForm.class);
                fieldForm.setName(name);

                Field field = FieldFactory.createField(fieldForm);
                return Optional.of(field);
            } catch (Exception e) {
                log.warn("Cannot instantiate name: {}, class: {}", name, fieldFound, e);
            }
        }

        // Handle edge case name is birthday but value is string (combination of name and value)

        // No match
        return Optional.empty();
    }

    public Optional<Field> detectTypeOnValue(final String name, Locale locale, Collection<Object> values) {
        // Put weight on possible matches (prefer numeric value over string, BigInteger for currency...)

        // TODO create constraints based on object length
        // Try to detect format (money, date)?

        List<Field> results = values
                .stream()
                .limit(numberItemsToProcess)
                .map(v -> {
                    try {
                        return getField(name, locale, v, values);
                    } catch (RuntimeException re) {
                        log.warn("Error while trying to detect field", re);
                        return new NullField(name, locale);
                    }
                })
                .filter(c-> !(c instanceof NullField))
                .collect(Collectors.toList());

        // TODO Merge ?

        return !results.isEmpty() ? Optional.of(results.get(0)) : Optional.empty();
    }

    @VisibleForTesting
    @SuppressWarnings("rawtypes")
    Field handleNestedField(String name, Locale locale, Object v, Collection<Object> values) {
        if (v instanceof List) {
            List<Object> list = (List) v;
            // Infer type
            if (list.size() > 0) {
                ArrayField arrayField = new ArrayField(name, locale);
                arrayField.setNumberOfElements(list.size());
                Object firstValue = list.get(0);

                // TODO compare
                Field detectedField = detect(name, locale, list).orElse(new NullField(name, locale));
                arrayField.setReference(detectedField);
                //arrayField.setElement(getField(name, locale, firstValue, list));

                return arrayField;
            }
        }

        if (v.getClass().isArray()) { //v instanceof Object[]
            Object[] array = (Object[]) v;
            // Infer type
            if (array.length > 0) {
                ArrayField arrayField = new ArrayField(name, locale);
                arrayField.setNumberOfElements(array.length);
                // infer
                Object firstValue = array[0];

                Field detectedField = detect(name, locale, Arrays.asList(array)).orElse(new NullField(name, locale));
                arrayField.setReference(detectedField);

                //arrayField.setElement(getField(name, locale, firstValue, Arrays.asList(array)));
//                    try {
//                        v.getClass().getDeclaredConstructor().newInstance();
//                        arrayField.setElement(null);
//                        return arrayField;
//                    } catch (Exception e) {
//                        log.warn("Error while inferring type for {}", v.toString(), e)
//                    }
                return arrayField;
            }
        }

        if (v instanceof Map) {
            // Drill down
            Map<Object, Object> map = (Map) v;
            ComplexField complexField = new ComplexField(name, locale);
            // Build name -> Field, infer key, value
            List<Field> detectedFields = map
                    .entrySet()
                    .stream()
                    .map(e -> {
                        List<Object> objects = new ArrayList<>();
                        objects.add(e.getValue());

                        return detect(String.valueOf(e.getKey()), locale, objects).orElse(new NullField(name, locale));
                    })
//                    .map(e -> getField(String.valueOf(e.getKey()),
//                                       locale,
//                                       e.getValue(),
//                                       e.getValue() instanceof Iterable ? Lists.newArrayList((Iterable) e.getValue()) : Lists.newArrayList(e.getValue())))
                    .collect(Collectors.toList());
            complexField.setReferences(detectedFields);

            return complexField;
        }
        return new NullField(name, locale);
    }

    @VisibleForTesting
    @SuppressWarnings({"unchecked", "rawtypes"})
    Field applyRangeConstraint(Field field, Collection<Object> numbers) {
        RangeConstraint rangeConstraint = getBoundaries(numbers, field.getObjectType());
        field.getConfig().put(Constants.MIN_VALUE_PROPERTY, rangeConstraint.getLowerBound());
        field.getConfig().put(Constants.MAX_VALUE_PROPERTY, rangeConstraint.getUpperBound());

        String number = rangeConstraint.getUpperBound().toString();
        if (number.contains(".")) {
            field.getConfig().put(
                Constants.NUMBER_DECIMALS_PROPERTY,
                number.substring(number.indexOf('.') + 1).length());
        }

        return field;
    }

    private RangeConstraint<? extends Number> getBoundaries(Collection<Object> numbers, Class classType) {
        Number minA = numbers.stream().filter(Objects::nonNull).map(o -> (Number)o).min(Comparator.comparing(Number::doubleValue)).orElse(0);
        Number maxA = numbers.stream().filter(Objects::nonNull).map(o -> (Number)o).max(Comparator.comparing(Number::doubleValue)).orElse(Integer.MAX_VALUE);
        if (Double.compare(minA.doubleValue(), maxA.doubleValue()) == 0) {
            return new RangeConstraint<>(0, maxA);
        }

        return new RangeConstraint<>(minA, maxA);
    }

    @VisibleForTesting
    Field getField(String name, Locale locale, Object v, Collection<Object> values) {
        if (v == null) {
            return new NullField(name, locale);
        }

        // String can be date
        if (v instanceof String) {
            // if v contains $ or formatted as xxx.xx
            // Try to detect on value (date formatted...)

            if (StringUtils.isEmpty(v.toString())) {
                return new EmptyField(name, locale);
            }

            Optional<Temporal> date = getDate(v.toString());
            if (date.isPresent()) {
                // Infer format
                DateTimeField dateField = new DateTimeField(name, locale);
                Temporal temporal = date.get();

                if (temporal instanceof LocalDateTime) {
                    dateField.getConfig().put(DateTimeField.START_DATE_PROPERTY,
                                              DATE_ONLY_FORMATTER.format(temporal));
                } if (temporal instanceof ZonedDateTime) {
                    ZonedDateTime zonedDateTime = (ZonedDateTime) temporal;
                    dateField.setTimezone(zonedDateTime.getZone());
                    dateField.getConfig().put(DateTimeField.START_DATE_PROPERTY,
                                              DATE_ONLY_FORMATTER.format(zonedDateTime.toLocalDateTime()));
                } else if (temporal instanceof LocalDate) {
                    dateField.setType(DateType.DATE_ONLY);
                    dateField.getConfig().put(DateTimeField.START_DATE_PROPERTY,
                                              DATE_ONLY_FORMATTER.format(temporal));
                } else if (temporal instanceof LocalTime) {
                    dateField.setType(DateType.TIME_ONLY);
                    dateField.getConfig().put(DateTimeField.START_DATE_PROPERTY,
                                              DATE_ONLY_FORMATTER.format(temporal));
                }

                dateField.getConfig().put(DateTimeField.END_DATE_PROPERTY,
                                          DATE_ONLY_FORMATTER.format(LocalDateTime.now()));

                return dateField;
            }

            Optional<Number> currency = getCurrency(v.toString(), locale);
            if (currency.isPresent()) {
                return new MoneyField(name, locale);
            }

            Optional<Boolean> boolValue = getBoolean(v.toString());
            if (boolValue.isPresent()) {
                return new BooleanField(name, locale);
            }

//            Optional<BigDecimal> bigDecimal = getBigDecimal(v.toString());
//            if (bigDecimal.isPresent()) {
//                return new DecimalField(name, locale);
//            }

            Optional<Number> number = getNumber(v.toString(), locale);

            // Prevents number present somewhere in string
            if (number.isPresent() && number.get().toString().equals(v.toString())) {
                if (number.get().doubleValue() % 1 == 0) {
                    // Convert values to double
                    return applyRangeConstraint(new LongField(name, locale),
                        values
                            .stream()
                            .map(o -> StringUtils.isBlank((String)o) ? null : Long.valueOf(o.toString()))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
                } else {
                    // Convert values to long
                    return applyRangeConstraint(new DoubleField(name, locale),
                        values
                            .stream()
                            .map(o -> StringUtils.isBlank((String)o) ? null : Double.valueOf(o.toString()))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));
                }
            }

            Optional<BigInteger> bigInteger = getBigInteger(v.toString());
            if (bigInteger.isPresent()) {
                return new BigIntegerField(name, locale);
            }

            return applyLength(new TextField(name, locale), values);
        }

        if (v instanceof Date || v instanceof Temporal) {
            DateTimeField dateTimeField = new DateTimeField(name, locale);
            if (v instanceof Date) {
                dateTimeField.getConfig().put(DateTimeField.START_DATE_PROPERTY,
                                              DATE_ONLY_FORMATTER.format(((Date)v).toInstant().atOffset(ZoneOffset.UTC).toLocalDate()));
            } else if (v instanceof LocalDateTime) {
                dateTimeField.getConfig().put(DateTimeField.START_DATE_PROPERTY,
                                              DATE_ONLY_FORMATTER.format(((LocalDateTime)v)));
            } if (v instanceof ZonedDateTime) {
                ZonedDateTime zonedDateTime = (ZonedDateTime) v;
                dateTimeField.setTimezone(zonedDateTime.getZone());
                dateTimeField.getConfig().put(DateTimeField.START_DATE_PROPERTY,
                                              DATE_ONLY_FORMATTER.format(zonedDateTime.toLocalDateTime()));
            }

            dateTimeField.getConfig().put(DateTimeField.END_DATE_PROPERTY,
                                          DATE_ONLY_FORMATTER.format(LocalDateTime.now()));
        }

        if (v instanceof Byte) {
            IntegerField byteField = new IntegerField(name, locale);
            byteField.getConfig().put(IntegerField.MIN_VALUE_PROPERTY, 0);
            byteField.getConfig().put(IntegerField.MAX_VALUE_PROPERTY, 127 + 1);
            return byteField;
        }

        if (v instanceof Short) {
            IntegerField shortField = new IntegerField(name, locale);
            shortField.getConfig().put(IntegerField.MIN_VALUE_PROPERTY, 0);
            shortField.getConfig().put(IntegerField.MAX_VALUE_PROPERTY, 32767 + 1);
            return shortField;
        }

        if (v instanceof Integer) {
            return applyRangeConstraint(new IntegerField(name, locale), values);
        }

        if (v instanceof Boolean) {
            return new BooleanField(name, locale);
        }

        if (v instanceof Long) {
            return applyRangeConstraint(new LongField(name, locale), values);
        }

        if (v instanceof Float) {
            return applyRangeConstraint(new FloatField(name, locale), values);
        }

        if (v instanceof Double) {
            return applyRangeConstraint(new DoubleField(name, locale), values);
        }

        return handleNestedField(name, locale, v, values);
    }

    // TODO should we use absolute max instead?
    @VisibleForTesting
    Field applyLength(TextField field, Collection<Object> values) {
        int maxLength = values
            .stream()
            .filter(Objects::nonNull)
            .mapToInt(o -> o.toString().length())
            .max()
            .orElse(50);

        if (values.stream().anyMatch(v -> StringUtils.isNotEmpty((String) v) && v.toString().contains(" "))) {
            field.setType(TextType.WORDS);
            if (values.stream().anyMatch(v -> StringUtils.isNotEmpty((String) v) && v.toString().contains("."))) {
                field.setType(TextType.SENTENCES);

                if (values.stream().anyMatch(v -> StringUtils.isNotEmpty((String) v) && v.toString().contains("\n"))) {
                    field.setType(TextType.PARAGRAPHS);
                }
            }
        }

        // Round to nearest 10
        maxLength = (int) (Math.ceil((maxLength * 1.25)/10.0) * 10);

        field.setLength(maxLength);

        return field;
    }

    @VisibleForTesting
    Optional<Boolean> getBoolean(String value) {
        if (StringUtils.isNotBlank(value)) {
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                return Optional.of(Boolean.valueOf(value));
            }
        }
        return Optional.empty();
    }

    @VisibleForTesting
    Optional<Number> getCurrency(String value, Locale locale) {
        try {
            Number number = NumberFormat.getCurrencyInstance(locale).parse(value);
            return Optional.of(number);
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    @VisibleForTesting
    Optional<Temporal> getDate(String value) {

        if (value.contains(".") || value.contains("/") || value.contains("-") || value.contains(",")) {
            try {
                return Optional.of(ZonedDateTime.parse(value, DATE_TIME_FORMATTER));
            } catch (DateTimeParseException dtpe) {

                if (value.contains(":")) {
                    try {
                        return Optional.of(LocalTime.parse(value, DATE_TIME_FORMATTER));

                    } catch (DateTimeParseException dtpe2) {
                        return Optional.empty();
                    }
                }
                try {
                    return Optional.of(LocalDate.parse(value, DATE_TIME_FORMATTER));
                } catch (DateTimeParseException dtpe2) {
                    return Optional.empty();
                }
            }
        }

        return Optional.empty();
    }

    @VisibleForTesting
    Optional<BigInteger> getBigInteger(String value) {
        try {
            return Optional.of(new BigInteger(value));
        } catch (NumberFormatException | NullPointerException nfe) {
            return Optional.empty();
        }
    }

    @VisibleForTesting
    Optional<BigDecimal> getBigDecimal(String value) {
        try {
            return Optional.of(new BigDecimal(value));
        } catch (NumberFormatException | NullPointerException nfe) {
            return Optional.empty();
        }
    }

    @VisibleForTesting
    Optional<Number> getNumber(String value, Locale locale) {
        NumberFormat format = NumberFormat.getInstance(locale);
        try {
            return Optional.of(format.parse(value));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    @VisibleForTesting
    Optional<Double> getDouble(String value) {
        try {
            return Optional.of(Double.parseDouble(value));
        } catch (NumberFormatException | NullPointerException nfe) {
            return Optional.empty();
        }
    }

    public Optional<Field> detectType(final String type, final String name, Locale locale) {
        String cleanType = type
            .toLowerCase()
            .replaceAll("\\p{Punct}","")
            .replaceAll("\\p{Space}", "")
            .replaceAll("[\\p{IsDigit}]", "");

        // Detect size using (value)
       String size = type.replaceAll("[^\\p{IsDigit}]", "");

        switch (cleanType) {
            case "bool":
            case "boolean":
            case "booleen":
                return Optional.of(new BooleanField(name, locale));
            case "char":
            case "chainecaractere":
            case "caracteres":
            case "varchar":
            case "string":
                if (StringUtils.isNumeric(size)) {
                    TextField field = new TextField(name, locale);
                    field.setLength(Integer.parseInt(size));
                    return Optional.of(field);
                }
                return Optional.of(new TextField(name, locale));
            case "entier":
            case "int":
            case "number":
            case "integer":
                return Optional.of(new IntegerField(name, locale));
            case "long":
                return Optional.of(new LongField(name, locale));
            case "float":
            case "valeurflottante":
            case "double":
                return Optional.of(new DoubleField(name, locale));
            case "decimal":
            case "bigdecimal":
                String precision = type.replaceAll("[^\\p{IsDigit}]", "");

                // Get formatter from decimal(x,y)

                return Optional.of(new DecimalField(name, locale));
            case "date":
                DateTimeField dateField = new DateTimeField(name, locale);
                dateField.setOutputFormat("dd/MM/yyyy");
                return Optional.of(dateField);
            case "time":
            case "temps":
                DateTimeField timeField = new DateTimeField(name, locale);
                //timeField.setType(DateType.TIME_ONLY);
                timeField.setOutputFormat("HH:mm:ss.SSSSSS");
                return Optional.of(timeField);
            case "timestamp":
                DateTimeField dateTimeField = new DateTimeField(name, locale);
                dateTimeField.setOutputFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
                return Optional.of(dateTimeField);
        }
        if (StringUtils.isNumeric(size)) {
            TextField field = new TextField(name, locale);
            field.setLength(Integer.parseInt(size));
            return Optional.of(field);
        }
        return Optional.of(new TextField(name, locale));
    }
}

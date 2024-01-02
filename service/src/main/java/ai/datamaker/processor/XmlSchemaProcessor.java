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
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.DateTimeField;
import ai.datamaker.model.field.type.DecimalField;
import ai.datamaker.model.field.type.DoubleField;
import ai.datamaker.model.field.type.DurationField;
import ai.datamaker.model.field.type.EmptyField;
import ai.datamaker.model.field.type.FloatField;
import ai.datamaker.model.field.type.IntegerField;
import ai.datamaker.model.field.type.LongField;
import ai.datamaker.model.field.type.TextField;
import ai.datamaker.model.field.type.UrlField;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.visitor.XSVisitor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class XmlSchemaProcessor extends DatasetProcessor {
    @Override
    public Optional<Dataset> process(InputStream input, JobConfig config) {

        String datasetName = (String) config.getConfigProperty(INPUT_FILENAME_PROPERTY);
        Locale locale = getLocale(config);
        Dataset dataset = new Dataset(datasetName, locale);

        try {
            XSOMParser parser = new XSOMParser(SAXParserFactory.newInstance());
            parser.parse(input);
            XSSchemaSet schemaSet = parser.getResult();
            XSSchema xsSchema = schemaSet.getSchema(1);

            XmlSchemaVisitor xmlSchemaVisitor = new XmlSchemaVisitor();
            xmlSchemaVisitor.setDataset(dataset);

            xsSchema.iterateElementDecls().forEachRemaining(e -> e.visit(xmlSchemaVisitor));

            return Optional.of(dataset);
        } catch (SAXException e) {
            throw new IllegalStateException("Error while processing xml schema file", e);
        }

        // The date is specified in the following form "YYYY-MM-DD" where:

        // Binary Data Types
        //Binary data types are used to express binary-formatted data.
        //
        //We have two binary data types:
        //
        //base64Binary (Base64-encoded binary data)
        //hexBinary (hexadecimal-encoded binary data)

        //     <xs:choice>

        // <xs:simpleType name="valuelist">
        //  <xs:list itemType="xs:integer"/>
        //</xs:simpleType>

        // The complexType element defines a complex type. A complex type element is an XML element that contains other elements and/or attributes.

        // The simpleType element defines a simple type and specifies the constraints and information about the values of attributes or text-only elements.

        // If element has child, either array or map field
        // return Optional.empty();
    }

    @Data
    private class XmlSchemaVisitor implements XSVisitor {

        private Dataset dataset;

        private Field currentField;

        @Override
        public void annotation(XSAnnotation ann) {
            System.out.println(ann);
        }

        @Override
        public void attGroupDecl(XSAttGroupDecl decl) {
            System.out.println(decl);
        }

        @Override
        public void attributeDecl(XSAttributeDecl decl) {
            System.out.println(decl);
            Optional<Field> fieldOnName = fieldDetectorService.detectTypeOnName(decl.getName(), dataset.getLocale());
            Optional<Field> fieldOnType = Optional.of(getFieldBasedOnType(decl.getType().getName(), decl.getName(), dataset.getLocale()));
            fieldDetectorService.findBestMatch(fieldOnName, fieldOnType).ifPresent(f -> {
                f.setIsAttribute(true);
                f.setIsNullable(false);
                f.setDataset(dataset);
                if (currentField instanceof ArrayField) {
                    ArrayField arrayField = (ArrayField) currentField;
                    f.setIsNested(true);
                    arrayField.setReference(f);
                } else if (currentField instanceof ComplexField) {
                    ComplexField complexField = (ComplexField) currentField;
                    f.setIsNested(true);
                    complexField.getReferences().add(f);
                } else {
                    dataset.addField(f);
                }
            });
        }

        @Override
        public void attributeUse(XSAttributeUse use) {
            use.getDecl().visit(this);
        }

        @Override
        public void complexType(XSComplexType type) {
            type.getAttributeUses().forEach(a -> a.visit(this));

//            Field previousField = currentField;
//            ComplexField complexField = new ComplexField(type.getName(), dataset.getLocale());
//            currentField = complexField;

            type.getContentType().visit(this);

//            currentField = previousField;
//            dataset.addField(complexField);
        }

        @Override
        public void schema(XSSchema schema) {

        }

        @Override
        public void facet(XSFacet facet) {

        }

        @Override
        public void notation(XSNotation notation) {

        }

        @Override
        public void identityConstraint(XSIdentityConstraint decl) {

        }

        @Override
        public void xpath(XSXPath xp) {

        }

        @Override
        public void simpleType(XSSimpleType simpleType) {
            // list, string, decimal
            if (simpleType.isPrimitive()) {
            }
            if (simpleType.isList()) {
                simpleType.asList().getItemType().visit(this);
            }
        }

        @Override
        public void particle(XSParticle particle) {
            Field previousField = currentField;
            boolean isArray = false;
            ArrayField arrayField = new ArrayField("array", dataset.getLocale());
            arrayField.setDataset(dataset);
            if (particle.getMaxOccurs().intValue() > 1 || particle.getMaxOccurs().intValue() == -1) {
                currentField = arrayField;
                arrayField.setNumberOfElements(particle.getMaxOccurs().intValue());
                isArray = true;
            }
            particle.getTerm().visit(this);
            if (particle.getMinOccurs().intValue() == 0) {
                currentField.setIsNullable(true);
            }
            if (isArray) {
                if (previousField instanceof ComplexField) {
                    ComplexField previousComplexField = (ComplexField) previousField;
                    previousComplexField.getReferences().add(arrayField);
                    arrayField.setPosition(previousComplexField.getReferences().size() + 1);
                    currentField = previousField;
                } else if (previousField instanceof ArrayField) {
                    ArrayField previousArrayField = (ArrayField) previousField;
                    previousArrayField.setReference(arrayField);
                    currentField = previousField;
                } else {
                    dataset.addField(arrayField);
                }
            }
        }

        @Override
        public void empty(XSContentType empty) {
            EmptyField emptyField = new EmptyField();
            dataset.addField(emptyField);
        }

        @Override
        public void wildcard(XSWildcard wc) {

        }

        @Override
        public void modelGroupDecl(XSModelGroupDecl decl) {
            System.out.println(decl);
        }

        @Override
        public void modelGroup(XSModelGroup group) {
            Arrays.stream(group.getChildren()).forEach(c -> c.visit(this));
        }

        @Override
        public void elementDecl(XSElementDecl decl) {
            Field previousField = currentField;

            if (StringUtils.isBlank(dataset.getName())) {
                dataset.setName(decl.getName());
            }

            // FIXME support restrictions
            // TODO get constraints
            if (decl.getType().isComplexType()) {
                ComplexField complexField = new ComplexField(decl.getName(), dataset.getLocale());
                complexField.setDataset(dataset);
                currentField = complexField;
                decl.getType().asComplexType().visit(this);
                if (previousField instanceof ComplexField) {
                    ComplexField previousComplexField = (ComplexField) previousField;
                    complexField.setPosition(complexField.getReferences().size() + 1);
                    previousComplexField.getReferences().add(complexField);
                    currentField = previousField;
                } else if (previousField instanceof ArrayField) {
                    ArrayField previousArrayField = (ArrayField) previousField;
                    complexField.setPosition(complexField.getReferences().size() + 1);
                    previousArrayField.setReference(complexField);
                    // dataset.addField(previousArrayField);
                    currentField = previousField;
                } else {
                    dataset.addField(complexField);
                }
            }
            if (decl.getType().isSimpleType()) {
                Optional<Field> fieldOnName = fieldDetectorService.detectTypeOnName(decl.getName(), dataset.getLocale());
                Optional<Field> fieldOnType = Optional.of(getFieldBasedOnType(decl.getType().asSimpleType().getName(), decl.getName(), dataset.getLocale()));
                fieldDetectorService.findBestMatch(fieldOnName, fieldOnType).ifPresent(f -> {
                    f.setDataset(dataset);
                    if (currentField instanceof ArrayField) {
                        ArrayField arrayField = (ArrayField) currentField;
                        f.setIsNested(true);
                        arrayField.setReference(f);
                    } else if (currentField instanceof ComplexField) {
                        ComplexField complexField = (ComplexField) currentField;
                        f.setIsNested(true);
                        complexField.setPosition(complexField.getReferences().size() + 1);
                        complexField.getReferences().add(f);
                    } else {
                        currentField = f;
                        dataset.addField(f);
                    }
                });

                if (decl.getType().asSimpleType().isList()) {
                    ArrayField arrayField = new ArrayField(decl.getName(), dataset.getLocale());
                    arrayField.setDataset(dataset);
                    currentField = arrayField;
                    decl.getType().asSimpleType().asList().getItemType().visit(this);
                    dataset.addField(arrayField);
                }
            }
        }
    }

    private Field getFieldBasedOnType(String type, String name, Locale locale) {
        switch (type) {
            case "unsignedByte": // 	An unsigned 8-bit integer
            case "byte": // 	A signed 8-bit integer
                IntegerField byteField = new IntegerField(name, locale);
                byteField.getConfig().put(IntegerField.MIN_VALUE_PROPERTY, 0);
                byteField.getConfig().put(IntegerField.MAX_VALUE_PROPERTY, 32767 + 1);
                return byteField;
            case "decimal": // 	A decimal value
                return new DecimalField(name, locale);
            case "int": // 	A signed 32-bit integer
            case "unsignedInt": // 	An unsigned 32-bit integer
            case "integer": // 	An integer value
                return new IntegerField(name, locale);
            case "long": // 	A signed 64-bit integer
            case "unsignedLong": // 	An unsigned 64-bit integer
                return new LongField(name, locale);
            case "negativeInteger": // 	An integer containing only negative values (..,-2,-1)
                IntegerField negativeInteger = new IntegerField(name, locale);
                negativeInteger.getConfig().put(IntegerField.MIN_VALUE_PROPERTY, Integer.MIN_VALUE);
                negativeInteger.getConfig().put(IntegerField.MAX_VALUE_PROPERTY, 0);
                return negativeInteger;
            case "nonNegativeInteger": // 	An integer containing only non-negative values (0,1,2,..)
                return new IntegerField(name, locale);
            case "nonPositiveInteger": // 	An integer containing only non-positive values (..,-2,-1,0)
                IntegerField nonPositiveInteger = new IntegerField(name, locale);
                nonPositiveInteger.getConfig().put(IntegerField.MIN_VALUE_PROPERTY, Integer.MIN_VALUE);
                nonPositiveInteger.getConfig().put(IntegerField.MAX_VALUE_PROPERTY, 1);
                return nonPositiveInteger;
            case "positiveInteger": // 	An integer containing only positive values (1,2,..)
                IntegerField positiveInteger = new IntegerField(name, locale);
                positiveInteger.getConfig().put(IntegerField.MIN_VALUE_PROPERTY, 1);
                return positiveInteger;
            case "unsignedShort": // 	An unsigned 16-bit integer
            case "short": // 	A signed 16-bit integer
                IntegerField shortField = new IntegerField(name, locale);
                shortField.getConfig().put(IntegerField.MIN_VALUE_PROPERTY, 0);
                shortField.getConfig().put(IntegerField.MAX_VALUE_PROPERTY, 127 + 1);
                return shortField;
            case  "ENTITIES":
            case "ENTITY":
            case "ID": 	//A string that represents the ID attribute in XML (only used with schema attributes)
            case "IDREF": // 	A string that represents the IDREF attribute in XML (only used with schema attributes)
            case "IDREFS":
            case "language": // 	A string that contains a valid language id
            case "Name": // 	A string that contains a valid XML name
            case "NCName": //
            case "NMTOKEN": // 	A string that represents the NMTOKEN attribute in XML (only used with schema attributes)
            case "NMTOKENS":
            case "normalizedString": // 	A string that does not contain line feeds, carriage returns, or tabs
            case "QName":
            case "string": // 	A string
                return new TextField(name, locale);
            case "date": // 	Defines a date value
                DateTimeField dateOnly = new DateTimeField();
                dateOnly.setType(DateTimeField.DateType.DATE_ONLY);
                dateOnly.setOutputFormat("YYYY-MM-DD");
                return dateOnly;
            case "dateTime": // 	Defines a date and time value
                DateTimeField dateTimeField = new DateTimeField();
                dateTimeField.setOutputFormat("YYYY-MM-DDThh:mm:ss");
                return dateTimeField;
            case "duration": // 	Defines a time interval
                return new DurationField(name, locale);
            case "gDay": // 	Defines a part of a date - the day (DD)
                DateTimeField gDay = new DateTimeField();
                gDay.setType(DateTimeField.DateType.DATE_ONLY);
                gDay.setOutputFormat("DD");
                return gDay;
            case "gMonth": // 	Defines a part of a date - the month (MM)
                DateTimeField gMonth = new DateTimeField();
                gMonth.setType(DateTimeField.DateType.DATE_ONLY);
                gMonth.setOutputFormat("MM");
                return gMonth;
            case "gMonthDay": // 	Defines a part of a date - the month and day (MM-DD)
                DateTimeField gMonthDay = new DateTimeField();
                gMonthDay.setType(DateTimeField.DateType.DATE_ONLY);
                gMonthDay.setOutputFormat("MM-DD");
                return gMonthDay;
            case "gYear": // 	Defines a part of a date - the year (YYYY)
                DateTimeField gYear = new DateTimeField();
                gYear.setType(DateTimeField.DateType.DATE_ONLY);
                gYear.setOutputFormat("YYYY");
                return gYear;
            case "time": // 	Defines a time value
                DateTimeField timeOnly = new DateTimeField();
                timeOnly.setType(DateTimeField.DateType.TIME_ONLY);
                timeOnly.setOutputFormat("hh:mm:ss");
                return timeOnly;
            case "anyURI":
                return new UrlField(name, locale);
            case "double":
                return new DoubleField(name, locale);
            case "float":
                return new FloatField(name, locale);
            case "boolean":
                return new BooleanField(name, locale);
            case "base64Binary":
            case "hexBinary":
            case "NOTATION":
        }
        return new TextField(name, locale);
    }

    /**
     * Restrictions on String Data Types
     * Restrictions that can be used with String data types:
     *
     * enumeration
     * length
     * maxLength
     * minLength
     * pattern (NMTOKENS, IDREFS, and ENTITIES cannot use this constraint)
     * whiteSpace
     */

    /**
     * Date and Time Data Types
     * Name	Description
     * date	Defines a date value
     * dateTime	Defines a date and time value
     * duration	Defines a time interval
     * gDay	Defines a part of a date - the day (DD)
     * gMonth	Defines a part of a date - the month (MM)
     * gMonthDay	Defines a part of a date - the month and day (MM-DD)
     * gYear	Defines a part of a date - the year (YYYY)
     * gYearMonth	Defines a part of a date - the year and month (YYYY-MM)
     * time	Defines a time value
     * Restrictions on Date Data Types
     * Restrictions that can be used with Date data types:
     *
     * enumeration
     * maxExclusive
     * maxInclusive
     * minExclusive
     * minInclusive
     * pattern
     * whiteSpace
     */

    /**
     * Miscellaneous Data Types
     * Name	Description
     * anyURI
     * base64Binary
     * boolean
     * double
     * float
     * hexBinary
     * NOTATION
     * QName
     * Restrictions on Miscellaneous Data Types
     * Restrictions that can be used with the other data types:
     *
     * enumeration (a Boolean data type cannot use this constraint)
     * length (a Boolean data type cannot use this constraint)
     * maxLength (a Boolean data type cannot use this constraint)
     * minLength (a Boolean data type cannot use this constraint)
     * pattern
     * whiteSpace
     */

    /**
     * Restrictions for Datatypes
     * Constraint	Description
     * enumeration	Defines a list of acceptable values
     * fractionDigits	Specifies the maximum number of decimal places allowed. Must be equal to or greater than zero
     * length	Specifies the exact number of characters or list items allowed. Must be equal to or greater than zero
     * maxExclusive	Specifies the upper bounds for numeric values (the value must be less than this value)
     * maxInclusive	Specifies the upper bounds for numeric values (the value must be less than or equal to this value)
     * maxLength	Specifies the maximum number of characters or list items allowed. Must be equal to or greater than zero
     * minExclusive	Specifies the lower bounds for numeric values (the value must be greater than this value)
     * minInclusive	Specifies the lower bounds for numeric values (the value must be greater than or equal to this value)
     * minLength	Specifies the minimum number of characters or list items allowed. Must be equal to or greater than zero
     * pattern	Defines the exact sequence of characters that are acceptable
     * totalDigits	Specifies the exact number of digits allowed. Must be greater than zero
     * whiteSpace	Specifies how white space (line feeds, tabs, spaces, and carriage returns) is handled
     * @return
     */

    /**
     * XML Schema has a lot of built-in data types. The most common types are:
     *
     * xs:string
     * xs:decimal
     * xs:integer
     * xs:boolean
     * xs:date
     * xs:time
     *
     * Attributes may have a default value OR a fixed value specified.
     *
     * A default value is automatically assigned to the attribute when no other value is specified.
     *
     * In the following example the default value is "EN":
     *
     * <xs:attribute name="lang" type="xs:string" default="EN"/>
     * A fixed value is also automatically assigned to the attribute, and you cannot specify another value.
     *
     * In the following example the fixed value is "EN":
     *
     * <xs:attribute name="lang" type="xs:string" fixed="EN"/>
     * @return
     */

    /**
     * Indicators
     * There are seven indicators:
     *
     * Order indicators:
     *
     * All
     * Choice
     * Sequence
     * Occurrence indicators:
     *
     * maxOccurs
     * minOccurs
     * Group indicators:
     *
     * Group name
     * attributeGroup name
     * @return
     */

    @Override
    public Set<SupportedMediaType> supportedTypes() {
        return Sets.newHashSet(SupportedMediaType.XML_SCHEMA);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(LOCALE_PROPERTY,
                                  INPUT_FILENAME_PROPERTY);
    }
}

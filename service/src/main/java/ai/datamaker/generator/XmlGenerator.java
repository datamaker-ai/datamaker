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

import ai.datamaker.exception.DatasetSerializationException;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldValue;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.EmptyField;
import com.google.common.collect.Lists;
import io.micrometer.core.instrument.util.StringUtils;
import javanet.staxutils.IndentingXMLStreamWriter;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class XmlGenerator implements DataGenerator {

    static final PropertyConfig XML_ROOT_ELEMENT =
            new PropertyConfig("xml.generator.root.element",
                               "Root element",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    static final PropertyConfig XML_ENCODING =
            new PropertyConfig("xml.generator.encoding",
                               "Encoding",
                               PropertyConfig.ValueType.STRING,
                               "UTF-8",
                               Collections.emptyList());

    static final PropertyConfig XML_PRETTY_PRINT =
            new PropertyConfig("xml.generator.pretty.print",
                               "Pretty print",
                               PropertyConfig.ValueType.BOOLEAN,
                               false,
                               Arrays.asList(true, false));

    static final PropertyConfig XML_VERSION =
            new PropertyConfig("xml.generator.version",
                               "Version",
                               PropertyConfig.ValueType.STRING,
                               "1.0",
                               Collections.emptyList());

    @Override
    public List<PropertyConfig> getConfigProperties() {
        List<PropertyConfig> properties = Lists.newArrayList();
        properties.add(XML_ROOT_ELEMENT);
        properties.add(XML_ENCODING);
        properties.add(XML_PRETTY_PRINT);
        properties.add(XML_VERSION);
        return properties;
    }

    @Override
    public void generate(Dataset dataset, OutputStream outputStream) throws Exception {
        generate(dataset, outputStream, JobConfig.EMPTY);
    }

    @Override
    public void generate(Dataset dataset, OutputStream outputStream, JobConfig config) throws Exception {

        String rootElement = (String) config.getConfigProperty(XML_ROOT_ELEMENT);
        String encoding = (String) config.getConfigProperty(XML_ENCODING);

        XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
        // outputFactory.setProperty(WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL, true);

        XMLStreamWriter xmlWriter = (boolean) config.getConfigProperty(XML_PRETTY_PRINT) ?
            new IndentingXMLStreamWriter(outputFactory.createXMLStreamWriter(outputStream, encoding)) :
            outputFactory.createXMLStreamWriter(outputStream, encoding);

        xmlWriter.writeStartDocument(encoding, (String) config.getConfigProperty(XML_VERSION));

        if (StringUtils.isNotEmpty(rootElement)) {
            xmlWriter.writeStartElement(rootElement);
        } else {
            xmlWriter.writeStartElement(dataset.getName());
        }

        dataset.processAllValues(fv -> {
            Collection<FieldValue> attributes = fv.stream().filter(att -> att.getField().getIsAttribute()).collect(Collectors.toList());
            fv.stream().filter(f -> !f.getField().getIsAttribute()).forEach(f -> {
                try {
                    writeField(xmlWriter, f.getField(), f.getValue(), attributes);
                } catch (Exception e) {
                    throw new DatasetSerializationException("Errors during XML serialization", e, dataset);
                }
            });
        });

        if (StringUtils.isNotEmpty(rootElement)) {
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeEndDocument();
    }

    private void writeField(XMLStreamWriter xmlWriter, Field field, Object value, Collection<FieldValue> attributes) throws XMLStreamException {
        if (field.getIsNullable() && value == null || field.getIsAttribute()) {
            // Skip value
            return;
        }

        if (field instanceof ArrayField) {
            ArrayField arrayField = (ArrayField) field;
            List<Object> values = (List<Object>) value;
            if (arrayField.getReference() instanceof ComplexField) {
                ComplexField complexField = (ComplexField) arrayField.getReference();
                Collection<FieldValue> complexAttributes = complexField
                        .getReferences()
                        .stream()
                        .filter(Field::getIsAttribute)
                        .map(att -> FieldValue.of(att, att.getData()))
                        .collect(Collectors.toList());

                values.forEach(v -> {
                    try {
                        writeField(xmlWriter, arrayField.getReference(), v, complexAttributes);
                    } catch (XMLStreamException e) {
                        throw new DatasetSerializationException("Errors during XML serialization", e, field.getDataset());
                    }
                });
            } else {
                if (!field.getIsAttribute()) {
                    xmlWriter.writeStartElement(field.getName());
                    writeAttributes(xmlWriter, field, attributes);
                    xmlWriter.writeCharacters(values.stream().map(Object::toString).collect(Collectors.joining(" ")));
                    xmlWriter.writeEndElement();
                }
            }
        } else if (field instanceof ComplexField) {
            ComplexField complexField = (ComplexField) field;

            xmlWriter.writeStartElement(field.getName());
            Collection<FieldValue> complexAttributes = complexField
                    .getReferences()
                    .stream()
                    .filter(Field::getIsAttribute)
                    .map(att -> FieldValue.of(att, att.getData()))
                    .collect(Collectors.toList());

            writeAttributes(xmlWriter, field, complexAttributes);

            Map<String, Object> values = (Map<String, Object>) value;
            values.forEach((k,v) ->  {
                try {
                    Field keyField = complexField.getReferences().stream().filter(r -> r.getName().equals(k)).findFirst().orElseThrow();
                    if (keyField instanceof ComplexField || keyField instanceof ArrayField) {
                        try {
                            writeField(xmlWriter, keyField, v, Collections.emptyList());
                        } catch (XMLStreamException e) {
                            throw new DatasetSerializationException("Errors during XML serialization", e, field.getDataset());
                        }
                    } else {
                        if (!keyField.getIsAttribute())  {
                            xmlWriter.writeStartElement(k);
                            writeAttributes(xmlWriter, field, Collections.emptyList());
                            xmlWriter.writeCharacters(String.valueOf(v));
                            xmlWriter.writeEndElement();
                        }
                    }
                } catch (XMLStreamException e) {
                    throw new DatasetSerializationException("Errors during XML serialization", e, field.getDataset());
                }
            });
            xmlWriter.writeEndElement();
        } else if (field instanceof EmptyField) {
            xmlWriter.writeEmptyElement(field.getName());
        } else {
            if (!field.getIsAttribute()) {
                xmlWriter.writeStartElement(field.getName());
                writeAttributes(xmlWriter, field, attributes);
                xmlWriter.writeCharacters(String.valueOf(value));
                xmlWriter.writeEndElement();
            }
        }
    }

    private void writeAttributes(XMLStreamWriter xmlWriter, Field field, Collection<FieldValue> attributes) {
        attributes.forEach(att -> {
            try {
                xmlWriter.writeAttribute(att.getField().getName(), String.valueOf(att.getValue()));
            } catch (XMLStreamException e) {
                throw new DatasetSerializationException("Errors during XML serialization", e, field.getDataset());
            }
        });
    }

    @Override
    public FormatType getDataType() {
        return FormatType.XML;
    }

}

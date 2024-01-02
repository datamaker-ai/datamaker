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
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.NullField;
import ai.datamaker.model.field.type.TextField;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


@Component
@Slf4j
public class XmlProcessor extends DatasetProcessor {

    static final PropertyConfig XML_ROOT_ELEMENT_PROPERTY =
            new PropertyConfig("xml.processor.root.element",
                               "XML Root element",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    static final PropertyConfig XML_PROCESSOR_NUMBER_LINES_PROPERTY =
            new PropertyConfig("xml.processor.process.lines.number",
                               "Number of lines to process",
                               PropertyConfig.ValueType.NUMERIC,
                               10,
                               Collections.emptyList());

    @Override
    public Optional<Dataset> process(InputStream input, JobConfig config) {

        @Nullable
        String rootElementTag = (String) config.getConfigProperty(XML_ROOT_ELEMENT_PROPERTY);
        // String encoding = config.getProperty(Constants.XML_ENCODING);
        Integer itemsToProcess = (Integer) config.getConfigProperty(XML_PROCESSOR_NUMBER_LINES_PROPERTY);
        String datasetName = (String) config.getConfigProperty(INPUT_FILENAME_PROPERTY);
        Locale locale = getLocale(config);
        Dataset dataset = new Dataset(datasetName, locale);

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbFactory.setExpandEntityReferences(false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            Document doc = dBuilder.parse(input);
            doc.setStrictErrorChecking(false);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            Element rootElement = findRootElement(doc, rootElementTag);
            dataset.setName(rootElement.getNodeName());

            log.debug("Root element :" + doc.getDocumentElement().getNodeName());
            dataset.addField(processElement(rootElement, locale, null));

        } catch (Exception e) {
            log.error("Error while processing XML file", e);
            throw new IllegalStateException("Error while processing XML file", e);
        }

        // If element has child, either array or map field
        return Optional.of(dataset);
    }

    private Element findRootElement(Document doc, String rootTagElement) {
        Element elementNode = doc.getDocumentElement();

        if (StringUtils.isNotBlank(rootTagElement)) {
            NodeList nodeList = doc.getElementsByTagName(rootTagElement);
            if (nodeList.getLength() <= 0) {
                throw new IllegalArgumentException("root element " + rootTagElement + " not found");
            }
            Node rootNode = nodeList.item(0);
            if (rootNode.getNodeType() != Node.ELEMENT_NODE) {
                throw new IllegalArgumentException("root element " + rootTagElement + " not found");
            }
            return (Element) rootNode;
        }

//        for (int childIndex = 0; childIndex < rootNode.getChildNodes().getLength(); childIndex++) {
//            Node currentNode = rootNode.getChildNodes().item(childIndex);
//
//            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
//                return (Element) currentNode;
//            }
//        }
//        throw new IllegalArgumentException("root element not found");

        return elementNode;
    }

    private Field processElement(Element element, Locale locale, String description) {

        if (element.hasAttributes()) {
            ComplexField complexField = new ComplexField(element.getNodeName(), locale);
            complexField.setDescription(description);

            processAttributes(element, complexField, locale);

            ComplexField children = processChildNodes(element, locale, null);
            complexField.getReferences().addAll(children.getReferences());
            return complexField;
        }

        if (isArray(element)) {
            // Probably an array
            ArrayField arrayField = new ArrayField(element.getNodeName(), locale);

            // TODO improve find number of siblings
            //arrayField.setNumberOfElements(10);
            arrayField.setDescription(description);
            // Add children
            if (element.hasChildNodes()) {
                Element childElement = getFirstChild(element);
                if (childElement != null) {
                    arrayField.setReference(processElement(childElement, locale, null));
                } else {
                    arrayField.setReference(new NullField(element.getNodeName(), locale));
                }
            } else {
                arrayField.setReference(processElement(element, locale, null));
            }

            return arrayField;

        } else if (element.hasChildNodes() && getFirstChild(element) != null) {

            return processChildNodes(element, locale, description);
        } else {
            Field field = fieldDetectorService.detect(element.getNodeName(),
                                                      locale,
                                                      Lists.newArrayList(element.getTextContent()))
                .orElse(new TextField(element.getNodeName(), locale));


            return field;
        }
    }

    private ComplexField processChildNodes(Element element, Locale locale, String description) {
        // Problably Map
        ComplexField complexField = new ComplexField(element.getNodeName(), locale);
        complexField.setDescription(description);

        //Node firstChild = element.getFirstChild();
        NodeList nodeList = element.getChildNodes();
        String comment = "";
        for (int childIndex = 0; childIndex < nodeList.getLength(); childIndex++) {

            Node currentNode = nodeList.item(childIndex);

            if (currentNode.getNodeType() == Node.COMMENT_NODE) {
                Comment commentNode = (Comment) currentNode;
                comment = commentNode.getTextContent();
            }

            if (currentNode.getNodeType() == Node.ELEMENT_NODE) {

                // Element element = (Element) currentNode;
                complexField.getReferences().add(processElement((Element) currentNode, locale, comment));
            }

            if (currentNode.getNodeType() == Node.TEXT_NODE && StringUtils.isNotBlank(currentNode.getTextContent())) {
                Field field = fieldDetectorService.detect(element.getNodeName(),
                    locale,
                    Lists.newArrayList(currentNode.getTextContent()))
                    .orElse(new TextField(element.getNodeName(), locale));

                field.setDescription(comment);
                complexField.getReferences().add(field);
            }
        }
        return complexField;
    }

    private boolean isArray(Element element) {
        if (element.hasChildNodes()) {
            Set<String> names = Sets.newHashSet();
            NodeList nodeList = element.getChildNodes();
            for (int childIndex = 0; childIndex < nodeList.getLength(); childIndex++) {
                Node currentNode = nodeList.item(childIndex);
                if (names.contains(currentNode.getNodeName().toLowerCase())) {
                    return true;
                }
                names.add(currentNode.getNodeName().toLowerCase());
            }
        }
        return false;
    }

    private Element getFirstChild(Element element) {
        if (element.hasChildNodes()) {
            NodeList nodeList = element.getChildNodes();
            for (int childIndex = 0; childIndex < nodeList.getLength(); childIndex++) {
                Node currentNode = nodeList.item(childIndex);
                if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                    return (Element) currentNode;
                }
            }
        }
        return null;
    }

    private boolean nextSibling(Element element) {

        Node sibling = element.getNextSibling();
        while (!(sibling instanceof Element) && sibling != null) {
            sibling = sibling.getNextSibling();
        }

        return sibling != null && sibling instanceof Element && sibling.getNodeName().equalsIgnoreCase(element.getNodeName());
    }

    private void processAttributes(Element element, ComplexField complexField, Locale locale) {
        NamedNodeMap nameNodeMap = element.getAttributes();
        for (int attributeIndex = 0; attributeIndex < element.getAttributes().getLength(); attributeIndex++) {

            Attr attr = (Attr) nameNodeMap.item(attributeIndex);

            String attrName = attr.getNodeName();
            String attrValue = attr.getNodeValue();

            Field field = fieldDetectorService.detect(attrName,
                                                      locale,
                                                      Lists.newArrayList(attrValue)).orElse(new TextField(attrName, locale));
            field.setIsAttribute(true);
            complexField.getReferences().add(field);
        }
    }

    @Override
    public Set<SupportedMediaType> supportedTypes() {
        return Sets.newHashSet(SupportedMediaType.XML);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(INPUT_FILENAME_PROPERTY,
                                  LOCALE_PROPERTY,
                                  XML_ROOT_ELEMENT_PROPERTY,
                                  XML_PROCESSOR_NUMBER_LINES_PROPERTY);
    }
}

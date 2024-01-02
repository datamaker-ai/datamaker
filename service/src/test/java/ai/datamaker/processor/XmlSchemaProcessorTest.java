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
import ai.datamaker.model.SupportedMediaType;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.TextField;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.InputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

class XmlSchemaProcessorTest extends AbstractDatasetProcessorTest {

    protected XmlSchemaProcessorTest() {
        super(new XmlSchemaProcessor());
    }

    @Test
    void process() throws SAXException {
        InputStream input = getClass().getClassLoader().getResourceAsStream("orders.xsd");
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(1)).when(fieldDetectorService).findBestMatch(any(), any());

//        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//        StreamSource ss = new StreamSource(input);
//        Schema schema = factory.newSchema(ss);

        Optional<Dataset> datasetOptional = datasetProcessor.process(input);
        assertTrue(datasetOptional.isPresent());
        Dataset dataset = datasetOptional.get();

        assertEquals("shiporder", dataset.getName());
        assertThat(dataset.getFields())
                .hasSize(1)
                .extracting("name")
                .contains("shiporder");

        assertEquals(ComplexField.class, dataset.getFields().get(0).getClass());
        ComplexField complexField = (ComplexField) dataset.getFields().get(0);

        assertThat(complexField.getReferences())
                .hasSize(4)
                .extracting("name")
                .contains("orderid", "orderperson", "shipto", "array");

        assertThat(complexField.getReferences())
                .hasSize(4)
                .extractingResultOf("getClass")
                .contains(TextField.class, TextField.class, ComplexField.class, ArrayField.class);
    }

    @Test
    void supportedTypes() {
        assertTrue(datasetProcessor.supportedTypes().contains(SupportedMediaType.XML_SCHEMA));
    }
}
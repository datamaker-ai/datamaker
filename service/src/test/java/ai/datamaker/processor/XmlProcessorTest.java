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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.SupportedMediaType;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.TextField;
import java.io.InputStream;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class XmlProcessorTest extends AbstractDatasetProcessorTest {

    protected XmlProcessorTest() {
        super(new XmlProcessor());
    }

    @Test
    void process() {
        InputStream input = getClass().getClassLoader().getResourceAsStream("staff.xml");
        Optional<Dataset> datasetOptional = datasetProcessor.process(input);

        assertTrue(datasetOptional.isPresent());
        Dataset dataset = datasetOptional.get();

        assertEquals("company", dataset.getName());
        assertThat(dataset.getFields())
            .hasSize(1)
            .extracting("name")
            .contains("company");

        assertEquals(ArrayField.class, dataset.getFields().get(0).getClass());
        ComplexField complexField = (ComplexField)((ArrayField) dataset.getFields().get(0)).getReference();

        assertThat(complexField.getReferences())
            .hasSize(5)
            .extracting("name")
            .contains("id", "firstname", "lastname", "nickname", "salary");

        assertThat(complexField.getReferences())
            .hasSize(5)
            .extractingResultOf("getClass")
            .contains(TextField.class, TextField.class, TextField.class, TextField.class, ComplexField.class);
    }

    @Test
    void supportedTypes() {
        assertTrue(datasetProcessor.supportedTypes().contains(SupportedMediaType.XML));
    }
}
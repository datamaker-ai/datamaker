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

import ai.datamaker.generator.FormatType;
import ai.datamaker.generator.TextGenerator;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.type.AddressField;
import ai.datamaker.model.field.type.AgeField;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.FloatField;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextGeneratorTest {

    private TextGenerator generator = new TextGenerator();

    @Test
    void generate() throws Exception {
        Dataset dataset = new Dataset("text", Locale.getDefault());

        ArrayField arrayField = new ArrayField("array", Locale.ENGLISH);
        arrayField.setNumberOfElements(3);
        ComplexField complexField = new ComplexField("complex", Locale.ENGLISH);
        complexField.getReferences().add(new AgeField("age", Locale.ENGLISH));
        complexField.getReferences().add(new FloatField("balance", Locale.ENGLISH));
        arrayField.setReference(complexField);
        dataset.getFields().add(arrayField);
        dataset.getFields().add(new AddressField("address", Locale.ENGLISH));
        dataset.setExportHeader(true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        generator.generate(dataset, baos);

        assertTrue(baos.toString().matches("\\[\\{age=.*, balance=.*\\}, \\{age=.*, balance=.*\\}, \\{age=.*, balance=.*\\}\\].*"));
    }

    @Test
    void getDataType() {
        assertEquals(FormatType.TEXT, generator.getDataType());
    }

    @Test
    void getConfigProperties() {
        assertEquals(3, generator.getConfigProperties().size());
    }
}
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.StringField;
import java.io.ByteArrayOutputStream;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class JsonGeneratorTest extends GeneratorAbstractTest {

    DataGenerator generator = new JsonGenerator();

    @Test
    void getJobProperties() {
        assertEquals(1, generator.getConfigProperties().size());
    }

    @Test
    void generate() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        generator.generate(getDataset(1), outputStream);

        assertTrue(outputStream.toString(UTF_8).matches("\\{\"id\":.*,\"address\":\".*\",\"test\":\".*\",\"number\":.*\\}\\s*"));
    }

    @Test
    void generateString() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Dataset dataset = new Dataset();
        dataset.setName("string");
        dataset.setExportHeader(false);
        StringField field = new StringField("test", Locale.getDefault());
        field.setLength(50);
        field.setAsciiOnly(true);
        dataset.addField(field);

        generator.generate(dataset, outputStream);

        assertTrue(outputStream.toString(UTF_8).matches("\"\\w{50}\"\\n"));
    }

    @Test
    void generateArray() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Dataset dataset = new Dataset();
        dataset.setName("array");
        dataset.setExportHeader(false);
        ArrayField field = new ArrayField("array", Locale.getDefault());
        StringField stringField = new StringField("test", Locale.getDefault());
        stringField.setLength(50);
        stringField.setAlphaNumeric(true);

        field.setReference(stringField);
        field.setNumberOfElements(10);

        dataset.addField(field);

        generator.generate(dataset, outputStream);

        assertTrue(outputStream.toString(UTF_8).matches("\\[(\"\\w{50}\",{0,1}){10}\\]\\n"));
    }

    @Test
    void getDataType() {
        assertEquals(FormatType.JSON, generator.getDataType());
    }
}
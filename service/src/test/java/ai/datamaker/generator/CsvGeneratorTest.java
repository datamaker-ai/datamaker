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

import static org.junit.jupiter.api.Assertions.*;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.field.type.NullField;
import java.io.ByteArrayOutputStream;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class CsvGeneratorTest extends GeneratorAbstractTest {

    DataGenerator generator = new CsvGenerator();

    @Test
    void generate() throws Exception {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Dataset dataset = getDataset(10);
        dataset.addField(new NullField("null", Locale.ENGLISH));

        generator.generate(dataset, bos);

        assertTrue(bos.toString().matches("(?s)id,address,test,number,null.*"));
    }

    @Test
    void testGenerate_overrideConfig() throws Exception {
        JobConfig config = new JobConfig();
        config.put(CsvGenerator.CSV_DELIMITER.getKey(), "|");
        config.put(CsvGenerator.CSV_FILE_ENCODING.getKey(), "ISO-8859-1");
        config.put(CsvGenerator.CSV_QUOTES_ALL.getKey(), true);
        config.put(CsvGenerator.CSV_ESCAPE_CHARACTER.getKey(), "\'");
        config.put(CsvGenerator.CSV_END_OF_LINE.getKey(), "\r\n");
        config.put(CsvGenerator.CSV_NULL_VALUE.getKey(), "null");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Dataset dataset = getDataset(10);
        dataset.addField(new NullField("null", Locale.ENGLISH));

        generator.generate(dataset, bos, config);

        //System.out.println(bos.toString());
        assertTrue(bos.toString().matches("(?s)\"id\"\\|\"address\"\\|\"test\"\\|\"number\"\\|\"null\".*"));
    }

    @Test
    void getDataType() {
        assertEquals(FormatType.CSV, generator.getDataType());
    }
}
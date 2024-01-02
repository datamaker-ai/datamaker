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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import ai.datamaker.generator.DataGenerator;
import ai.datamaker.generator.ExcelGenerator;
import ai.datamaker.generator.FormatType;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.type.AgeField;
import ai.datamaker.model.field.type.NameField;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

class ExcelGeneratorTest {

    DataGenerator generator = new ExcelGenerator();

    @Test
    void getJobProperties() {
        assertEquals(1, generator.getConfigProperties().size());
    }

    @Test
    void generate() throws Exception {

        NameField nameField = new NameField();
        nameField.setName("full name");
        AgeField ageField = new AgeField();
        ageField.setName("age");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        //File tempFile = File.createTempFile("test", ".xlsx");
        //tempFile.deleteOnExit();
        //FileOutputStream fos = new FileOutputStream(tempFile.getCanonicalPath());

        Dataset dataset = new Dataset();
        dataset.setName("Excel");
        dataset.setExportHeader(true);
        dataset.setNumberOfRecords(10l);
        dataset.addField(nameField);
        dataset.addField(ageField);

        generator.generate(dataset, bos);

        //System.out.println(bos.toString());
        assertNotNull(bos.toString());
    }

    @Test
    void getDataType() {
        assertEquals(FormatType.EXCEL, generator.getDataType());
    }
}
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

import ai.datamaker.generator.PdfGenerator;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.field.type.AddressField;
import ai.datamaker.model.field.type.AgeField;
import ai.datamaker.model.field.type.FloatField;
import ai.datamaker.model.field.type.StringField;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

class PdfGeneratorTest {

    private PdfGenerator pdfGenerator = new PdfGenerator();

    @Test
    void generate() throws Exception {
        File tempFile = File.createTempFile("dataset", "table.pdf");
        tempFile.deleteOnExit();

        FileOutputStream fileOutputStream = new FileOutputStream(tempFile.getPath());
        Dataset dataset = new Dataset("test", Locale.ENGLISH);
        dataset.setNumberOfRecords(10L);
        dataset.getFields().add(new StringField("text", Locale.ENGLISH));
        dataset.getFields().add(new AgeField("age", Locale.ENGLISH));
        dataset.getFields().add(new FloatField("balance", Locale.ENGLISH));
        dataset.getFields().add(new AddressField("address", Locale.ENGLISH));
        dataset.setExportHeader(true);

        pdfGenerator.generate(dataset, fileOutputStream);
    }

    @Test
    void generate_table() throws Exception {
        File tempFile = File.createTempFile("dataset", "table.pdf");
        tempFile.deleteOnExit();

        FileOutputStream fileOutputStream = new FileOutputStream(tempFile.getPath());
        Dataset dataset = new Dataset("test", Locale.ENGLISH);
        dataset.setNumberOfRecords(10L);
        dataset.getFields().add(new StringField("text", Locale.ENGLISH));
        dataset.getFields().add(new AgeField("age", Locale.ENGLISH));
        dataset.getFields().add(new FloatField("balance", Locale.ENGLISH));
        dataset.getFields().add(new AddressField("address", Locale.ENGLISH));
        dataset.setExportHeader(true);

        JobConfig jobConfig = new JobConfig();
        jobConfig.setDataset(dataset);
        jobConfig.put(PdfGenerator.PDF_GENERATOR_OUTPUT_TABLE, true);

        pdfGenerator.generate(dataset, fileOutputStream, jobConfig);
    }

    @Test
    void getDataType() {
    }

    @Test
    void getConfigProperties() {
    }
}
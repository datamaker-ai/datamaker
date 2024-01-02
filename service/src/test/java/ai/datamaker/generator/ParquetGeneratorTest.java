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

import ai.datamaker.generator.DataGenerator;
import ai.datamaker.generator.FormatType;
import ai.datamaker.generator.ParquetGenerator;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.field.type.NameField;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static ai.datamaker.generator.ParquetGenerator.GENERATOR_COMPRESS_CONTENT;
import static ai.datamaker.generator.ParquetGenerator.PARQUET_COMPRESS_CODEC;
import static org.junit.jupiter.api.Assertions.*;

class ParquetGeneratorTest {

    DataGenerator generator = new ParquetGenerator();

    @Test
    void getJobProperties() {
        assertEquals(2, generator.getConfigProperties().size());
    }

    @Test
    void generate() throws Exception {
        NameField nameField = new NameField();
        nameField.setName("full name");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Dataset dataset = new Dataset();
        dataset.setName("parquet");
        dataset.setNumberOfRecords(10l);
        dataset.addField(nameField);

        generator.generate(dataset, bos);

        //System.out.println(bos.toString());
        assertNotNull(bos.toString());
        assertTrue(bos.toString().matches("(?s).*parquet\\.avro\\.schema.*\\{\"type\":\"record\",\"name\":\"parquet\",\"fields\":\\[\\{\"name\":\"full_name\",\"type\":\"string\"\\}\\]\\}.*"),
            "invalid schema");
    }

    @Test
    void generate_testCompression() throws Exception {

        JobConfig config = new JobConfig();
        config.put(GENERATOR_COMPRESS_CONTENT.getKey(), true);
        config.put(PARQUET_COMPRESS_CODEC.getKey(), "snappy");

        NameField nameField = new NameField();
        nameField.setName("full name");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Dataset dataset = new Dataset();
        dataset.setName("parquet");
        dataset.setNumberOfRecords(10l);
        dataset.addField(nameField);

        generator.generate(dataset, bos, config);

        //System.out.println(bos.toString());
        assertNotNull(bos.toString());
        assertTrue(bos.toString().matches("(?s).*parquet\\.avro\\.schema.*\\{\"type\":\"record\",\"name\":\"parquet\",\"fields\":\\[\\{\"name\":\"full_name\",\"type\":\"string\"\\}\\]\\}.*"),
            "invalid schema");
    }

    @Test
    void getDataType() {
        assertEquals(FormatType.PARQUET, generator.getDataType());
    }
}
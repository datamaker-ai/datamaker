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

import ai.datamaker.generator.AvroGenerator;
import ai.datamaker.generator.DataGenerator;
import ai.datamaker.generator.FormatType;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.field.type.AddressField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.NameField;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvroGeneratorTest {

    DataGenerator generator = new AvroGenerator();
    @Test
    void generate() throws Exception {

        NameField nameField = new NameField();
        nameField.setName("full name");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Dataset dataset = new Dataset();
        dataset.setName("dataset");
        dataset.setNumberOfRecords(30l);
        dataset.addField(nameField);

        generator.generate(dataset, bos);
        assertTrue(bos.toString().matches("(?s).*\\{\"type\":\"record\",\"name\":\"dataset\",\"fields\":\\[\\{\"name\":\"full_name\",\"type\":\"string\"\\}\\]\\}.*"), "schema doesn't match");
    }

    @Test
    void generate_complex() throws Exception {

        NameField nameField = new NameField();
        nameField.setName("full name");

        AddressField addressField = new AddressField();
        addressField.setName("address");

        ComplexField complexField = new ComplexField("person", Locale.getDefault());
        complexField.getReferences().add(nameField);
        complexField.getReferences().add(addressField);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Dataset dataset = new Dataset();
        dataset.setName("dataset");
        dataset.setNumberOfRecords(30l);
        dataset.addField(complexField);

        generator.generate(dataset, bos);
        assertTrue(bos.toString().matches("(?s).*\\{\"type\":\"record\",\"name\":\"dataset\",\"fields\":\\[\\{\"name\":\"person\",\"type\":\\{\"type\":\"record\",\"name\":\"person\",\"fields\":\\[\\{\"name\":\"full_name\",\"type\":\"string\"\\},\\{\"name\":\"address\",\"type\":\"string\"\\}\\]\\}\\}\\]\\}.*"), "schema doesn't match");
    }

    @Test
    void generate_testCompression() throws Exception {
        JobConfig config = new JobConfig();
        config.put(AvroGenerator.GENERATOR_COMPRESS_CONTENT.getKey(), true);
        config.put(AvroGenerator.AVRO_COMPRESS_CODEC.getKey(), "bzip2");

        NameField nameField = new NameField();
        nameField.setName("full name");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Dataset dataset = new Dataset();
        dataset.setName("dataset");
        dataset.setNumberOfRecords(30l);
        dataset.addField(nameField);

        generator.generate(dataset, bos, config);

        assertTrue(bos.toString().matches("(?s).*\\{\"type\":\"record\",\"name\":\"dataset\",\"fields\":\\[\\{\"name\":\"full_name\",\"type\":\"string\"\\}\\]\\}.*avro\\.codec\\s*bzip2.*"), "schema doesn't match");
    }

    @Test
    void getDataType() {
        assertEquals(FormatType.AVRO, generator.getDataType());
    }
}
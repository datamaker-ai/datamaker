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
import ai.datamaker.generator.SqlGenerator;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.field.type.AddressField;
import ai.datamaker.model.field.type.BooleanField;
import ai.datamaker.model.field.type.DateTimeField;
import ai.datamaker.model.field.type.LongField;
import ai.datamaker.model.field.type.NameField;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlGeneratorTest {

    DataGenerator generator = new SqlGenerator();
    @Test
    void getJobProperties() {
        assertEquals(3, generator.getConfigProperties().size());
    }

    @Test
    void generate() throws Exception {
        NameField nameField = new NameField();
        nameField.setName("full name");

        AddressField addressField = new AddressField("address", Locale.ENGLISH);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Dataset dataset = new Dataset();
        dataset.setName("person");
        dataset.setNumberOfRecords(10l);
        dataset.addField(nameField);
        dataset.addField(addressField);

        JobConfig config = new JobConfig();
        config.put(SqlGenerator.SQL_GENERATOR_END_OF_LINE.getKey(), ";\n");
        generator.generate(dataset, bos, config);
        String insertQuery = bos.toString();
        System.out.println(insertQuery);
        Pattern pattern = Pattern.compile("INSERT INTO person VALUES \\('.*', '.*'\\);");
        assertTrue(pattern.matcher(insertQuery).lookingAt());
    }

    @Test
    void generate_differentTypes() throws Exception {

        BooleanField booleanField = new BooleanField("bool", Locale.ENGLISH);
        LongField longField = new LongField("long", Locale.ENGLISH);
        DateTimeField dateTimeField = new DateTimeField("date", Locale.ENGLISH);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        Dataset dataset = new Dataset();
        dataset.setName("person");
        dataset.setNumberOfRecords(10l);
        dataset.addField(booleanField);
        dataset.addField(longField);
        dataset.addField(dateTimeField);

        JobConfig config = new JobConfig();
        config.put(SqlGenerator.SQL_GENERATOR_END_OF_LINE.getKey(), ";\n");
        generator.generate(dataset, bos, config);
        String insertQuery = bos.toString();
        System.out.println(insertQuery);

        Pattern pattern = Pattern.compile("INSERT INTO person VALUES \\((TRUE|FALSE), -*\\d*, '.*'\\);");
        assertTrue(pattern.matcher(insertQuery).lookingAt());
    }

    @Test
    void getDataType() {
        assertEquals(FormatType.SQL, generator.getDataType());
    }
}
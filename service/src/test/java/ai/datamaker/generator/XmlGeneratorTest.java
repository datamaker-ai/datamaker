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
import ai.datamaker.generator.XmlGenerator;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.field.type.AddressField;
import ai.datamaker.model.field.type.AgeField;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.FloatField;
import ai.datamaker.model.field.type.StringField;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlGeneratorTest {

    DataGenerator generator = new XmlGenerator();

    @Test
    void getJobProperties() {
        assertEquals(4, generator.getConfigProperties().size());
    }

    @Test
    void generate() throws Exception {

        Dataset dataset = new Dataset("xml", Locale.getDefault());
        dataset.setNumberOfRecords(1L);
        dataset.getFields().add(new StringField("field1", Locale.ENGLISH));
        dataset.getFields().add(new AgeField("age", Locale.ENGLISH));
        dataset.getFields().add(new FloatField("balance", Locale.ENGLISH));
        dataset.getFields().add(new AddressField("address", Locale.ENGLISH));
        dataset.setExportHeader(true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JobConfig config = new JobConfig();
        config.put(XmlGenerator.XML_ROOT_ELEMENT.getKey(), "data");

        generator.generate(dataset, baos, config);

        //System.out.println(baos.toString(StandardCharsets.UTF_8));

        assertTrue(baos.toString(StandardCharsets.UTF_8).matches("<\\?xml version='1\\.0' encoding='UTF-8'\\?><data><field1>.*</field1><age>.*</age><balance>.*</balance><address>.*</address></data>\\s*"));
    }

    @Test
    void complex() throws Exception {

        Dataset dataset = new Dataset("xml", Locale.getDefault());
        dataset.setNumberOfRecords(1L);

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
        JobConfig config = new JobConfig();
        config.put(XmlGenerator.XML_ROOT_ELEMENT.getKey(), "data");

        generator.generate(dataset, baos, config);

        //System.out.println(baos.toString(StandardCharsets.UTF_8));

        assertTrue(baos.toString(StandardCharsets.UTF_8).matches("<\\?xml version='1\\.0' encoding='UTF-8'\\?><data>(<complex><age>.*</age><balance>.*</balance></complex>){3}<address>.*</address></data>\\s*"));
    }

    @Test
    void complex_attributes() throws Exception {

        Dataset dataset = new Dataset("xml", Locale.getDefault());
        dataset.setNumberOfRecords(1L);

        ArrayField arrayField = new ArrayField("array", Locale.ENGLISH);
        arrayField.setNumberOfElements(3);
        ComplexField complexField = new ComplexField("complex", Locale.ENGLISH);
        AgeField ageField = new AgeField("age", Locale.ENGLISH);
        ageField.setIsAttribute(true);
        complexField.getReferences().add(ageField);
        complexField.getReferences().add(new FloatField("balance", Locale.ENGLISH));
        arrayField.setReference(complexField);
        dataset.getFields().add(arrayField);
        dataset.getFields().add(new AddressField("address", Locale.ENGLISH));
        dataset.setExportHeader(true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JobConfig config = new JobConfig();
        config.put(XmlGenerator.XML_ROOT_ELEMENT.getKey(), "data");

        generator.generate(dataset, baos, config);

        System.out.println(baos.toString(StandardCharsets.UTF_8));

        assertTrue(baos.toString(StandardCharsets.UTF_8).matches("<\\?xml version='1\\.0' encoding='UTF-8'\\?><data>(<complex age=\".*\"><balance>.*</balance></complex>){3}<address>.*</address></data>\\s*"));
    }

    @Test
    void testNullableOrZero() {
        //fail("implement");
    }

    @Test
    void getDataType() {
        assertEquals(FormatType.XML, generator.getDataType());
    }
}
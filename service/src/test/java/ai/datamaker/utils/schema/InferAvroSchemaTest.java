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

package ai.datamaker.utils.schema;

import ai.datamaker.utils.schema.InferAvroSchema;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.type.AddressField;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.DateTimeField;
import ai.datamaker.model.field.type.DurationField;
import ai.datamaker.model.field.type.FloatField;
import ai.datamaker.model.field.type.SequenceField;
import ai.datamaker.model.field.type.StringField;
import ai.datamaker.model.field.type.UuidField;
import org.apache.avro.Schema;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InferAvroSchemaTest {

    @Test
    void inferFrom() {
        Schema schema = InferAvroSchema.inferFrom(generateDataset());

        assertNotNull(schema);
        assertEquals("{\"type\":\"record\",\"name\":\"datatest\",\"fields\":[{\"name\":\"id\",\"type\":\"long\"},{\"name\":\"address\",\"type\":\"string\"},{\"name\":\"stringvar\",\"type\":[\"string\",\"null\"]},{\"name\":\"number\",\"type\":\"float\"}]}", schema.toString());
    }

    @Test
    void cleanName() {
        assertEquals("eeA_fJ_u", InferAvroSchema.cleanName("éèA f#J..``-_ ù", Locale.FRANCE));
    }

    @Test
    void inferFrom_ComplexField() {
        Dataset dataset = new Dataset();
        dataset.setName("data-test");
        dataset.setNumberOfRecords(1L);

        ComplexField complexField = new ComplexField("people", Locale.getDefault());
        complexField.getReferences().add(new AddressField("address", Locale.getDefault()));
        Field field = new StringField("string-var", Locale.getDefault());
        field.setIsNullable(true);
        complexField.getReferences().add(field);
        complexField.getReferences().add(new FloatField("number", Locale.getDefault()));

        dataset.addField(complexField);

        Schema schema = InferAvroSchema.inferFrom(dataset);

        assertNotNull(schema);
        assertEquals("{\"type\":\"record\",\"name\":\"datatest\",\"fields\":[{\"name\":\"people\",\"type\":{\"type\":\"record\",\"name\":\"people\",\"fields\":[{\"name\":\"address\",\"type\":\"string\"},{\"name\":\"stringvar\",\"type\":[\"string\",\"null\"]},{\"name\":\"number\",\"type\":\"float\"}]}}]}", schema.toString());
    }

    @Test
    void inferFrom_ArrayField() {
        Dataset dataset = new Dataset();
        dataset.setName("data-test");
        dataset.setNumberOfRecords(1L);
        ArrayField arrayField = new ArrayField("array", Locale.getDefault());
        arrayField.setReference(new FloatField("number", Locale.getDefault()));
        arrayField.setNumberOfElements(10);
        dataset.addField(arrayField);

        Schema schema = InferAvroSchema.inferFrom(dataset);

        assertNotNull(schema);
        assertEquals("{\"type\":\"record\",\"name\":\"datatest\",\"fields\":[{\"name\":\"array\",\"type\":{\"type\":\"array\",\"items\":\"float\"}}]}", schema.toString());
    }

    @Test
    void inferFrom_special() {
        Dataset dataset = new Dataset();
        dataset.setName("data-test");
        dataset.setNumberOfRecords(1L);
        UuidField uuidField = new UuidField("uuid", Locale.getDefault());
        DurationField durationField = new DurationField("duration", Locale.getDefault());
        DateTimeField dateTimeFieldString = new DateTimeField("dt-string", Locale.getDefault());
        dateTimeFieldString.setOutputFormat(DateTimeField.DEFAULT_OUTPUT_FORMAT);
        DateTimeField dateTimeFieldLong = new DateTimeField("dt-long", Locale.getDefault());
        dataset.addField(uuidField);
        dataset.addField(durationField);
        dataset.addField(dateTimeFieldString);
        dataset.addField(dateTimeFieldLong);

        Schema schema = InferAvroSchema.inferFrom(dataset);

        assertNotNull(schema);
        assertEquals("{\"type\":\"record\",\"name\":\"datatest\",\"fields\":[{\"name\":\"uuid\",\"type\":\"string\"},{\"name\":\"duration\",\"type\":{\"type\":\"fixed\",\"name\":\"duration\",\"size\":12}},{\"name\":\"dtstring\",\"type\":\"string\"},{\"name\":\"dtlong\",\"type\":\"long\"}]}", schema.toString());
    }

    Dataset generateDataset() {
        Dataset dataset = new Dataset();
        dataset.setName("data-test");
        dataset.setNumberOfRecords(10l);
        SequenceField sequenceField = new SequenceField("id", Locale.getDefault());
        dataset.addField(sequenceField);
        dataset.addField(new AddressField("address", Locale.getDefault()));
        Field field = new StringField("string-var", Locale.getDefault());
        field.setIsNullable(true);
        dataset.addField(field);
        dataset.addField(new FloatField("number", Locale.getDefault()));

        return dataset;
    }
}
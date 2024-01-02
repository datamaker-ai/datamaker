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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import ai.datamaker.utils.schema.AvroSchemaConverter;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.DoubleField;
import ai.datamaker.model.field.type.LongField;
import ai.datamaker.model.field.type.TextField;
import ai.datamaker.service.FieldDetectorService;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class AvroSchemaConverterTest {

    private AvroSchemaConverter converter;

    @BeforeEach
    public void setUp() {
        converter = new AvroSchemaConverter();
        FieldDetectorService fieldDetectorService = Mockito.mock(FieldDetectorService.class);
        when(fieldDetectorService.detectTypeOnName(any(), any())).thenReturn(Optional.empty());
        ReflectionTestUtils.setField(converter, "fieldDetectorService", fieldDetectorService);
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(1)).when(fieldDetectorService).findBestMatch(any(), any());
    }

    @Test
    void convertFrom() throws IOException {

        URL fileUrl = getClass().getClassLoader().getResource("userdata1.avro");

        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>();

        DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(new File(fileUrl.getFile()), datumReader);

        Schema schema = dataFileReader.getSchema();

        assertNotNull(schema);

        Dataset dataset = converter.convertFrom(schema, Locale.getDefault());
        assertNotNull(dataset);
        assertThat(dataset.getFields())
            .hasSize(13)
            .extracting("name")
            .contains("registration_dttm",
                "id",
                "first_name",
                "last_name",
                "email",
                "gender",
                "ip_address",
                "cc",
                "country",
                "birthdate",
                "salary",
                "title",
                "comments");

        assertThat(dataset.getFields())
            .extractingResultOf("getClass")
            .contains(TextField.class,
                LongField.class,
                TextField.class,
                TextField.class,
                TextField.class,
                TextField.class,
                TextField.class,
                LongField.class,
                TextField.class,
                TextField.class,
                DoubleField.class,
                TextField.class,
                TextField.class);
    }

    @Test
    void getDatasetForGoogleAvro() throws Exception {
        URL fileUrl = getClass().getClassLoader().getResource("ga.avro");

        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>();

        DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(new File(fileUrl.getFile()), datumReader);

        Schema schema = dataFileReader.getSchema();

        assertNotNull(schema);

        Dataset dataset = converter.convertFrom(schema, Locale.getDefault());
        assertNotNull(dataset);
        assertThat(dataset.getFields())
                .hasSize(16)
                .extracting("name")
                .containsExactly("visitorId",
                                 "visitNumber",
                                 "visitId",
                                 "visitStartTime",
                                 "date",
                                 "totals",
                                 "trafficSource",
                                 "device",
                                 "geoNetwork",
                                 "customDimensions",
                                 "hits",
                                 "fullVisitorId",
                                 "userId",
                                 "clientId",
                                 "channelGrouping",
                                 "socialEngagementType");

        assertThat(dataset.getFields())
                .hasSize(16)
                .extractingResultOf("getClass")
                .containsExactly(LongField.class,
                                 LongField.class,
                                 LongField.class,
                                 LongField.class,
                                 TextField.class,
                                 ComplexField.class,
                                 ComplexField.class,
                                 ComplexField.class,
                                 ComplexField.class,
                                 ArrayField.class,
                                 ArrayField.class,
                                 TextField.class,
                                 TextField.class,
                                 TextField.class,
                                 TextField.class,
                                 TextField.class);
    }

    @Test
    void testMapStructure() {
        // fail("implements");
    }

}
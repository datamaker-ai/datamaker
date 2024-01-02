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

package ai.datamaker.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.SupportedMediaType;
import java.io.InputStream;
import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CsvProcessorTest extends AbstractDatasetProcessorTest {

    protected CsvProcessorTest() {
        super(new CsvProcessor());
    }

    @Test
    void process() {
        InputStream input = getClass().getClassLoader().getResourceAsStream("userdata1.csv");

        Optional<Dataset> datasetOptional = datasetProcessor.process(input);

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Collection> valuesCaptor = ArgumentCaptor.forClass(Collection.class);

        verify(fieldDetectorService, times(13)).detectTypeOnName(nameCaptor.capture(), any());
        verify(fieldDetectorService, times(13)).detectTypeOnValue(nameCaptor.capture(), any(), valuesCaptor.capture());

        assertThat(nameCaptor.getAllValues())
            .hasSize(26)
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

        assertThat(valuesCaptor.getAllValues().get(2))
            .hasSize(10)
            .contains(null,
                "Albert",
                "Evelyn",
                "Denise",
                "Carlos",
                "Kathryn",
                "Samuel",
                "Harry",
                "Jose",
                "Emily");

        assertTrue(datasetOptional.isPresent());
    }

    // Test with null values
    @Test
    void process_skipNullValues() {

    }

    @Test
    void process_noHeader() {
        InputStream input = getClass().getClassLoader().getResourceAsStream("userdata2.csv");

        JobConfig config = new JobConfig();
        config.put(CsvProcessor.CSV_HEADER_FIRST_LINE_PROPERTY.getKey(), false);

        Optional<Dataset> datasetOptional = datasetProcessor.process(input, config);

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Collection> valuesCaptor = ArgumentCaptor.forClass(Collection.class);

        verify(fieldDetectorService, times(13)).detectTypeOnValue(nameCaptor.capture(), any(), valuesCaptor.capture());

        assertThat(nameCaptor.getAllValues())
                .hasSize(13)
                .contains("column-1",
                          "column-2",
                          "column-3",
                          "column-4",
                          "column-5",
                          "column-6",
                          "column-7",
                          "column-8",
                          "column-9",
                          "column-10",
                          "column-11",
                          "column-12",
                          "column-13");

        assertThat(valuesCaptor.getAllValues().get(2))
                .hasSize(10)
                .contains(null,
                          "Albert",
                          "Evelyn",
                          "Denise",
                          "Carlos",
                          "Kathryn",
                          "Samuel",
                          "Harry",
                          "Jose",
                          "Emily");

        assertTrue(datasetOptional.isPresent());
    }

    // Test with skip lines
    @Test
    void process_skipLines() {
        InputStream input = getClass().getClassLoader().getResourceAsStream("userdata1.csv");

        JobConfig config = new JobConfig();
        config.put(CsvProcessor.CSV_PROCESS_NUMBER_LINES_PROPERTY.getKey(), 20);
        config.put(CsvProcessor.CSV_SKIP_NUMBER_LINES_PROPERTY.getKey(), 10);
        config.put(CsvProcessor.CSV_HEADER_FIRST_LINE_PROPERTY.getKey(), false);

        Optional<Dataset> datasetOptional = datasetProcessor.process(input, config);

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Collection> valuesCaptor = ArgumentCaptor.forClass(Collection.class);

        verify(fieldDetectorService, never()).detectTypeOnName(anyString(), any());
        verify(fieldDetectorService, times(13)).detectTypeOnValue(nameCaptor.capture(), any(), valuesCaptor.capture());

        assertThat(nameCaptor.getAllValues())
                .hasSize(13)
                .contains("column-1",
                          "column-2",
                          "column-3",
                          "column-4",
                          "column-5",
                          "column-6",
                          "column-7",
                          "column-8",
                          "column-9",
                          "column-10",
                          "column-11",
                          "column-12",
                          "column-13");

        assertThat(valuesCaptor.getAllValues().get(2))
                .hasSize(20)
                .contains("Emily",
                          "Susan",
                          "Alice",
                          "Justin",
                          "Kathy",
                          "Dorothy",
                          "Bruce",
                          "Emily",
                          "Stephen",
                          "Clarence",
                          "Rebecca",
                          "Diane",
                          "Lawrence",
                          "Gregory",
                          "Michelle",
                          "Rachel",
                          "Anthony",
                          "Henry",
                          "Samuel",
                          "Jacqueline");

        assertTrue(datasetOptional.isPresent());
    }

    @Test
    void supportedTypes() {
        assertTrue(datasetProcessor.supportedTypes().contains(SupportedMediaType.CSV));
        assertTrue(datasetProcessor.supportedTypes().contains(SupportedMediaType.TSV));
        assertTrue(datasetProcessor.supportedTypes().contains(SupportedMediaType.TEXT));
    }
}
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

import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.type.AgeField;
import ai.datamaker.model.field.type.DoubleField;
import ai.datamaker.model.field.type.NameField;
import ai.datamaker.model.field.type.TextField;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExcelProcessorTest extends AbstractDatasetProcessorTest {

    protected ExcelProcessorTest() {
        super(new ExcelProcessor());
    }

    @Test
    void process() {
        JobConfig config = new JobConfig();
        config.put(ExcelProcessor.EXCEL_HEADER_ROW_PROPERTY.getKey(), 0);
        InputStream input = getClass().getClassLoader().getResourceAsStream("test.xlsx");
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(1)).when(fieldDetectorService).findBestMatch(any(), any());

        Optional<Dataset> dataset = datasetProcessor.process(input, config);
        assertTrue(dataset.isPresent());
        assertThat(dataset.get().getFields())
            .hasSize(2)
            .extracting("name")
            .contains("full name", "age");

        assertThat(dataset.get().getFields())
                .hasSize(2)
                .extractingResultOf("getClass")
                .contains(TextField.class, DoubleField.class);

        verify(fieldDetectorService, times(20)).findBestMatch(any(), any());
    }

    @Test
    void process_detect_variants() {
        JobConfig config = new JobConfig();
        config.put(ExcelProcessor.EXCEL_HEADER_ROW_PROPERTY.getKey(), 0);
        InputStream input = getClass().getClassLoader().getResourceAsStream("test.xlsx");
        doAnswer(invocationOnMock -> {
            Field field = ((Optional<Field>) invocationOnMock.getArgument(1)).get();
            if (field instanceof TextField) {
                return Optional.of(new NameField("full name", Locale.ENGLISH));
            } else if (field instanceof DoubleField) {
                return Optional.of(new AgeField("age", Locale.ENGLISH));
            }

            return Optional.empty();
        }).when(fieldDetectorService).findBestMatch(any(), any());

        Optional<Dataset> dataset = datasetProcessor.process(input, config);
        assertTrue(dataset.isPresent());
        assertThat(dataset.get().getFields())
                .hasSize(2)
                .extracting("name")
                .contains("full name", "age");

        assertThat(dataset.get().getFields())
                .hasSize(2)
                .extractingResultOf("getClass")
                .contains(NameField.class, AgeField.class);

        verify(fieldDetectorService, times(20)).findBestMatch(any(), any());
    }

    @Test
    void process_skipColumns_processNumberLines() {
        JobConfig config = new JobConfig();
        config.put(ExcelProcessor.EXCEL_HEADER_ROW_PROPERTY.getKey(), 0);
        config.put(ExcelProcessor.EXCEL_PROCESS_NUMBER_LINES_PROPERTY.getKey(), 5);
        config.put(ExcelProcessor.EXCEL_SKIP_COLUMNS_PROPERTY.getKey(), Lists.newArrayList("A", "age"));

        InputStream input = getClass().getClassLoader().getResourceAsStream("test-skip-columns.xlsx");
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(1)).when(fieldDetectorService).findBestMatch(any(), any());

        Optional<Dataset> dataset = datasetProcessor.process(input, config);
        assertTrue(dataset.isPresent());
        assertThat(dataset.get().getFields())
                .hasSize(1)
                .extracting("name")
                .contains("revenue");

        assertThat(dataset.get().getFields())
                .hasSize(1)
                .extractingResultOf("getClass")
                .contains(DoubleField.class);

        verify(fieldDetectorService, times(5)).findBestMatch(any(), any());    }

    @Test
    void process_noHeaders_skipRows() {
        JobConfig config = new JobConfig();
        // config.put(Constants.EXCEL_HEADER_ROW, 0);
        config.put(ExcelProcessor.EXCEL_SKIP_ROWS_PROPERTY.getKey(), 5);

        InputStream input = getClass().getClassLoader().getResourceAsStream("test.xlsx");
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(1)).when(fieldDetectorService).findBestMatch(any(), any());

        Optional<Dataset> dataset = datasetProcessor.process(input, config);
        assertTrue(dataset.isPresent());
        assertThat(dataset.get().getFields())
                .hasSize(2)
                .extracting("name")
                .contains("column-0", "column-1");

        assertThat(dataset.get().getFields())
                .hasSize(2)
                .extractingResultOf("getClass")
                .contains(TextField.class, DoubleField.class);

        verify(fieldDetectorService, times(16)).findBestMatch(any(), any());
    }

    @Test
    void process_noData() {
        JobConfig config = new JobConfig();
        // config.put(Constants.EXCEL_HEADER_ROW, 0);
        config.put(ExcelProcessor.EXCEL_SKIP_ROWS_PROPERTY.getKey(), 15);

        InputStream input = getClass().getClassLoader().getResourceAsStream("test.xlsx");

        Optional<Dataset> dataset = datasetProcessor.process(input, config);
        assertTrue(dataset.isPresent());
        assertTrue(dataset.get().getFields().isEmpty());
        verify(fieldDetectorService, never()).findBestMatch(any(), any());
    }

    @Test
    void process_sheetName() {
        JobConfig config = new JobConfig();
        config.put(ExcelProcessor.EXCEL_HEADER_ROW_PROPERTY.getKey(), 0);
        config.put(ExcelProcessor.EXCEL_SHEET_NAME_PROPERTY.getKey(), "Excel");
        InputStream input = getClass().getClassLoader().getResourceAsStream("test.xlsx");
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(1)).when(fieldDetectorService).findBestMatch(any(), any());

        Optional<Dataset> dataset = datasetProcessor.process(input, config);
        assertTrue(dataset.isPresent());
        assertThat(dataset.get().getFields())
                .hasSize(2)
                .extracting("name")
                .contains("full name", "age");

        assertThat(dataset.get().getFields())
                .hasSize(2)
                .extractingResultOf("getClass")
                .contains(TextField.class, DoubleField.class);

        verify(fieldDetectorService, times(20)).findBestMatch(any(), any());

    }

    @Test
    void process_sheetName_notFound() {
        JobConfig config = new JobConfig();
        // config.put(Constants.EXCEL_HEADER_ROW, 0);
        config.put(ExcelProcessor.EXCEL_SHEET_NAME_PROPERTY.getKey(), "jifoewa");

        InputStream input = getClass().getClassLoader().getResourceAsStream("test.xlsx");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            datasetProcessor.process(input, config);
        });

        verify(fieldDetectorService, never()).findBestMatch(any(), any());
    }

    @Test
    void process_sheetNumber_notFound() {
        JobConfig config = new JobConfig();
        // config.put(Constants.EXCEL_HEADER_ROW, 0);
        config.put(ExcelProcessor.EXCEL_SHEET_NUMBER_PROPERTY.getKey(), 2);

        InputStream input = getClass().getClassLoader().getResourceAsStream("test.xlsx");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            datasetProcessor.process(input, config);
        });

        verify(fieldDetectorService, never()).findBestMatch(any(), any());
    }

    @Test
    void process_rowsWithUnprocessedValues() {
        JobConfig config = new JobConfig();
        config.put(ExcelProcessor.EXCEL_HEADER_ROW_PROPERTY.getKey(), 0);
        InputStream input = getClass().getClassLoader().getResourceAsStream("test-missing-values.xlsx");
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(1)).when(fieldDetectorService).findBestMatch(any(), any());

        Optional<Dataset> dataset = datasetProcessor.process(input, config);
        assertTrue(dataset.isPresent());
        assertThat(dataset.get().getFields())
                .hasSize(2)
                .extracting("name")
                .contains("full name", "age");

        assertThat(dataset.get().getFields())
                .hasSize(2)
                .extractingResultOf("getClass")
                .contains(TextField.class, DoubleField.class);

        verify(fieldDetectorService, times(13)).findBestMatch(any(), any());
    }

    @Test
    void process_noValuesButDatatypeRow() {
        JobConfig config = new JobConfig();
        //config.put(Constants.EXCEL_HEADER_ROW, 0);
        config.put(ExcelProcessor.EXCEL_DATA_TYPE_ROW_PROPERTY.getKey(), 4);
        config.put(ExcelProcessor.EXCEL_SHEET_NAME_PROPERTY.getKey(), "Datatype");
        InputStream input = getClass().getClassLoader().getResourceAsStream("test-process-columns.xlsx");
        when(fieldDetectorService.detectType(any(), any(), any())).thenReturn(Optional.of(new TextField()));
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(1)).when(fieldDetectorService).findBestMatch(any(), any());

        Optional<Dataset> dataset = datasetProcessor.process(input, config);
        assertTrue(dataset.isPresent());
        assertThat(dataset.get().getFields())
                .hasSize(7);

        assertThat(dataset.get().getFields())
                .hasSize(7)
                .extractingResultOf("getClass")
                .contains(TextField.class);

        verify(fieldDetectorService, times(7)).findBestMatch(any(), any());
        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        verify(fieldDetectorService, times(7)).detectType(typeCaptor.capture(), anyString(), any());

        assertThat(typeCaptor.getAllValues()).contains("varchar(50)", "int", "decimal(1,0)", "char 50", "datetime", "timestamp", "bool");
    }

    @Test
    void process_noValuesButDatatypeColumn() {
        JobConfig config = new JobConfig();
        config.put(ExcelProcessor.EXCEL_PROCESS_ROWS_PROPERTY.getKey(), false);
        config.put(ExcelProcessor.EXCEL_HEADER_COLUMN_PROPERTY.getKey(), 2);
        config.put(ExcelProcessor.EXCEL_DATA_TYPE_COLUMN_PROPERTY.getKey(), 3);
        //config.put(Constants.EXCEL_SKIP_ROWS, 3);
        config.put(ExcelProcessor.EXCEL_SHEET_NAME_PROPERTY.getKey(), "Columns");

        InputStream input = getClass().getClassLoader().getResourceAsStream("test-process-columns.xlsx");
        when(fieldDetectorService.detectType(any(), any(), any())).thenReturn(Optional.of(new TextField()));
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(1)).when(fieldDetectorService).findBestMatch(any(), any());

        Optional<Dataset> dataset = datasetProcessor.process(input, config);
        assertTrue(dataset.isPresent());
        assertThat(dataset.get().getFields())
                .hasSize(7);

        assertThat(dataset.get().getFields())
                .hasSize(7)
                .extractingResultOf("getClass")
                .contains(TextField.class);

        verify(fieldDetectorService, times(7)).findBestMatch(any(), any());
        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);

        verify(fieldDetectorService, times(7)).detectType(typeCaptor.capture(), nameCaptor.capture(), any());

        assertThat(typeCaptor.getAllValues()).contains("varchar(50)", "int", "decimal(1,0)", "char 50", "datetime", "timestamp", "bool");
        assertThat(nameCaptor.getAllValues()).contains("full name", "age", "revenue", "patate", "roger", "attends", "vrai");
    }

    @Test
    void process_usingColumns_noHeader() {
        JobConfig config = new JobConfig();
        config.put(ExcelProcessor.EXCEL_PROCESS_ROWS_PROPERTY.getKey(), false);
        //config.put(Constants.EXCEL_HEADER_COLUMN, 2);
        config.put(ExcelProcessor.EXCEL_DATA_TYPE_COLUMN_PROPERTY.getKey(), 3);
        //config.put(Constants.EXCEL_SKIP_ROWS, 3);
        config.put(ExcelProcessor.EXCEL_SHEET_NAME_PROPERTY.getKey(), "Columns");

        InputStream input = getClass().getClassLoader().getResourceAsStream("test-process-columns.xlsx");
        when(fieldDetectorService.detectType(any(), any(), any())).thenReturn(Optional.of(new TextField()));
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(1)).when(fieldDetectorService).findBestMatch(any(), any());

        Optional<Dataset> dataset = datasetProcessor.process(input, config);
        assertTrue(dataset.isPresent());
        assertThat(dataset.get().getFields())
                .hasSize(7);

        assertThat(dataset.get().getFields())
                .hasSize(7)
                .extractingResultOf("getClass")
                .contains(TextField.class);

        verify(fieldDetectorService, times(7)).findBestMatch(any(), any());
        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);

        verify(fieldDetectorService, times(7)).detectType(typeCaptor.capture(), nameCaptor.capture(), any());

        assertThat(typeCaptor.getAllValues()).contains("varchar(50)", "int", "decimal(1,0)", "char 50", "datetime", "timestamp", "bool");
        assertThat(nameCaptor.getAllValues()).contains("row-3", "row-4", "row-5", "row-6", "row-7", "row-8", "row-9");
    }
}
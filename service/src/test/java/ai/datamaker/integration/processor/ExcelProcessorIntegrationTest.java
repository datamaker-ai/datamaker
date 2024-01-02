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

package ai.datamaker.integration.processor;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.field.type.AgeField;
import ai.datamaker.model.field.type.BooleanField;
import ai.datamaker.model.field.type.DateTimeField;
import ai.datamaker.model.field.type.DecimalField;
import ai.datamaker.model.field.type.NameField;
import ai.datamaker.model.field.type.TextField;
import ai.datamaker.processor.ExcelProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.InputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureTestDatabase
public class ExcelProcessorIntegrationTest {

    @Autowired
    private ExcelProcessor datasetProcessor;

    @Test
    void process_noValuesButDatatypeColumn() {
        JobConfig config = new JobConfig();
        config.put(ExcelProcessor.EXCEL_PROCESS_ROWS_PROPERTY.getKey(), false);
        config.put(ExcelProcessor.EXCEL_HEADER_COLUMN_PROPERTY.getKey(), 2);
        config.put(ExcelProcessor.EXCEL_DATA_TYPE_COLUMN_PROPERTY.getKey(), 3);
        //config.put(Constants.EXCEL_SKIP_ROWS, 3);
        config.put(ExcelProcessor.EXCEL_SHEET_NAME_PROPERTY.getKey(), "Columns");

        InputStream input = getClass().getClassLoader().getResourceAsStream("test-process-columns.xlsx");

        Optional<Dataset> dataset = datasetProcessor.process(input, config);
        assertTrue(dataset.isPresent());
        assertThat(dataset.get().getFields())
                .hasSize(7);

        assertThat(dataset.get().getFields())
                .hasSize(7)
                .extractingResultOf("getClass")
                .containsExactly(NameField.class, AgeField.class, DecimalField.class, TextField.class, TextField.class, DateTimeField.class, BooleanField.class);

    }
}

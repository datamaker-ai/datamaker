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

import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

// Use POI library
@Slf4j
public class ExcelGenerator implements DataGenerator {

    public static final PropertyConfig OUTPUT_FORMAT_PROPERTY =
        new PropertyConfig("excel.generator.date.format",
            "Date format",
            PropertyConfig.ValueType.STRING,
            "yyyy/m/d h:mm",
            Collections.emptyList());

    @Override
    public void generate(Dataset dataset, OutputStream outputStream) throws Exception {
        generate(dataset, outputStream, JobConfig.EMPTY);
    }

    @Override
    public void generate(Dataset dataset, OutputStream outputStream, JobConfig config) throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CreationHelper createHelper = workbook.getCreationHelper();

            XSSFSheet sheet = workbook.createSheet(dataset.getName());
            final int[] rowNum = {0};

            if (dataset.getExportHeader()) {
                final int[] hColNum = {0};

                Row headerRow = sheet.createRow(rowNum[0]++);
                dataset.getFields().forEach(f -> {
                    Cell headerCell = headerRow.createCell(hColNum[0]++);
                    headerCell.setCellValue((String) f.getName());
                });
            }

            dataset.processAllValues(fieldValues -> {
                Row row = sheet.createRow(rowNum[0]++);
                final int[] colNum = {0};

                fieldValues.forEach(fv -> {
                    Cell cell = row.createCell(colNum[0]++);
                    Object value = fv.getValue();

                    // FIXME handle List and Map
                    // List:CSV
                    // Map: key=value,
                    if (value instanceof String) {
                        cell.setCellValue((String) value);
                    } else if (value instanceof Date) {
                        CellStyle cellStyle = workbook.createCellStyle();
                        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat((String) config.getConfigProperty(OUTPUT_FORMAT_PROPERTY)));

                        cell.setCellValue((Date) value);
                        cell.setCellStyle(cellStyle);
                    } else if (value instanceof LocalDateTime) {
                        cell.setCellValue((LocalDateTime) value);
                    } else if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    }
                });
            });
            workbook.write(outputStream);
        }
    }

    @Override
    public FormatType getDataType() {
        return FormatType.EXCEL;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(OUTPUT_FORMAT_PROPERTY);
    }
}

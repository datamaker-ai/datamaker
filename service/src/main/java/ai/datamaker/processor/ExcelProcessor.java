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
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.SupportedMediaType;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.type.BooleanField;
import ai.datamaker.model.field.type.DateTimeField;
import ai.datamaker.model.field.type.DateTimeField.DateType;
import ai.datamaker.model.field.type.DoubleField;
import ai.datamaker.model.field.type.TextField;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class ExcelProcessor extends DatasetProcessor {

    public static final PropertyConfig EXCEL_SHEET_NAME_PROPERTY =
            new PropertyConfig("excel.processor.sheet.name",
                               "Sheet name",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig EXCEL_SHEET_NUMBER_PROPERTY =
            new PropertyConfig("excel.processor.sheet.number",
                               "Sheet number",
                               PropertyConfig.ValueType.NUMERIC,
                               0,
                               Collections.emptyList());

    public static final PropertyConfig EXCEL_PROCESS_ROWS_PROPERTY =
            new PropertyConfig("excel.processor.process.rows",
                               "Process rows",
                               PropertyConfig.ValueType.BOOLEAN,
                               true,
                               Collections.emptyList());

    public static final PropertyConfig EXCEL_SKIP_COLUMNS_PROPERTY =
            new PropertyConfig("excel.processor.skip.columns",
                               "Columns to skip",
                               PropertyConfig.ValueType.LIST,
                               Collections.emptyList(),
                               Collections.emptyList());

    public static final PropertyConfig EXCEL_HEADER_ROW_PROPERTY =
            new PropertyConfig("excel.processor.header.row",
                               "Which row number to use as header",
                               PropertyConfig.ValueType.NUMERIC,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig EXCEL_DESCRIPTION_ROW_PROPERTY =
            new PropertyConfig("excel.processor.description.row",
                               "Which row number to use as comment/description",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig EXCEL_DATA_TYPE_ROW_PROPERTY =
            new PropertyConfig("excel.processor.datatype.row",
                               "Which row number to use as datatype",
                               PropertyConfig.ValueType.NUMERIC,
                               "",
                               Collections.emptyList());

    static final PropertyConfig EXCEL_SKIP_ROWS_PROPERTY =
            new PropertyConfig("excel.processor.skip.rows",
                               "Number of rows to skip",
                               PropertyConfig.ValueType.NUMERIC,
                               0,
                               Collections.emptyList());

    static final PropertyConfig EXCEL_PROCESS_NUMBER_LINES_PROPERTY =
            new PropertyConfig("excel.processor.process.lines.number",
                               "Number of lines to process",
                               PropertyConfig.ValueType.NUMERIC,
                               10,
                               Collections.emptyList());

    public static final PropertyConfig EXCEL_HEADER_COLUMN_PROPERTY =
            new PropertyConfig("excel.processor.header.column",
                               "Which column number to use as header",
                               PropertyConfig.ValueType.NUMERIC,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig EXCEL_DATA_TYPE_COLUMN_PROPERTY =
            new PropertyConfig("excel.processor.datatype.column",
                               "Which column number to use as datatype",
                               PropertyConfig.ValueType.NUMERIC,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig EXCEL_DESCRIPTION_COLUMN_PROPERTY =
            new PropertyConfig("excel.processor.description.column",
                               "Which column number to use as comment/description",
                               PropertyConfig.ValueType.NUMERIC,
                               0,
                               Collections.emptyList());

    @Override
    public Optional<Dataset> process(InputStream input, JobConfig config) {

        Locale locale = getLocale(config);

        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(input);
        } catch (IOException e) {
            log.error("Cannot read from Excel stream", e);
            throw new IllegalStateException("Cannot read from Excel stream", e);
        }

        String sheetName = (String) config.getConfigProperty(EXCEL_SHEET_NAME_PROPERTY);
        Sheet datatypeSheet = null;
        if (StringUtils.isNotEmpty(sheetName)) {
            datatypeSheet = workbook.getSheet(sheetName);
        } else if (config.containsKey(EXCEL_SHEET_NUMBER_PROPERTY.getKey())) {
            datatypeSheet = workbook.getSheetAt((Integer) config.getConfigProperty(EXCEL_SHEET_NUMBER_PROPERTY));
        } else {
            datatypeSheet = workbook.getSheetAt(0);
        }
        Assert.notNull(datatypeSheet, "Datasheet not found");

        Dataset dataset = new Dataset();
        dataset.setName(datatypeSheet.getSheetName());

        Boolean processExcelRows = (Boolean) config.getConfigProperty(EXCEL_PROCESS_ROWS_PROPERTY);

        ArrayList<Field> detectedFields = processExcelRows ? processRows(config, locale, datatypeSheet) : processColumns(config, locale, datatypeSheet);
        detectedFields.forEach(dataset::addField);

        return Optional.of(dataset);
    }

    private ArrayList<Field> processRows(JobConfig config, Locale locale, Sheet datatypeSheet) {
        List<String> skipColumns = (List<String>) config.getConfigProperty(EXCEL_SKIP_COLUMNS_PROPERTY);
        ArrayList<Field> detectedFields = new ArrayList<>();
        Map<Integer, String> headerNames = Maps.newHashMap();

        if (config.containsKey(EXCEL_HEADER_ROW_PROPERTY.getKey())) {
            int headerPos = (int) config.getConfigProperty(EXCEL_HEADER_ROW_PROPERTY);

            Row headerRow = datatypeSheet.getRow(headerPos);
            if (headerRow != null) {
                //String[] names = new String[headerRow.getLastCellNum()];

                for (Cell currentCell : headerRow) {
                    headerNames.put(currentCell.getColumnIndex(), currentCell.getStringCellValue());
                    // names[currentCell.getColumnIndex()] = currentCell.getStringCellValue();
                }
            }
        }

        if (config.containsKey(EXCEL_DATA_TYPE_ROW_PROPERTY.getKey())) {
            int dataTypePos = (int) config.getConfigProperty(EXCEL_DATA_TYPE_ROW_PROPERTY);

            Row typesRow = datatypeSheet.getRow(dataTypePos);
            if (typesRow != null) {

                for (Cell currentCell : typesRow) {
                    // names[currentCell.getColumnIndex()] = currentCell.getStringCellValue();

                    String name = headerNames.containsKey(currentCell.getColumnIndex())
                            ? headerNames.get(currentCell.getColumnIndex())
                            : "column-" + currentCell.getColumnIndex();

                    if (skipColumns.contains(CellReference.convertNumToColString(currentCell.getColumnIndex())) ||
                            skipColumns.contains(String.valueOf(currentCell.getColumnIndex())) ||
                            skipColumns.contains(name)) {
                        continue;
                    }

                    Optional<Field> fieldDetectedUsingName = name.startsWith("column-") ? Optional.empty() : fieldDetectorService.detectTypeOnName(name, locale);
                    Optional<Field> detectedField = fieldDetectorService.detectType(currentCell.getStringCellValue(), name, locale);
                    Optional<Field> bestFieldMatch = fieldDetectorService.findBestMatch(fieldDetectedUsingName, detectedField);

                    bestFieldMatch.ifPresent(detectedFields::add);
                }
            }

            return detectedFields;
        }

        // Skip rows
        int startRow = 0;
        if (config.containsKey(EXCEL_HEADER_ROW_PROPERTY.getKey())) {
            startRow += (Integer) config.getConfigProperty(EXCEL_HEADER_ROW_PROPERTY);
            startRow += 1;
        }
        startRow += (Integer) config.getConfigProperty(EXCEL_SKIP_ROWS_PROPERTY);

        Iterable<Row> rows = Iterables.skip(datatypeSheet, startRow);

        int rowProcessed = 0;
        Integer linesToProcess = (Integer) config.getConfigProperty(EXCEL_PROCESS_NUMBER_LINES_PROPERTY);

        for (int rowNum = startRow; rowNum <= datatypeSheet.getLastRowNum(); rowNum++) {

            if (++rowProcessed > linesToProcess) {
                break;
            }

            int currentIndex = 0;
            Row currentRow = datatypeSheet.getRow(rowNum);

            if (currentRow != null) {

                for (Cell currentCell : currentRow) {

                    //getCellTypeEnum shown as deprecated for version 3.15
                    //getCellTypeEnum ill be renamed to getCellType starting from version 4.0

                    Field detectField = null;

                    String name = headerNames.containsKey(currentCell.getColumnIndex())
                            ? headerNames.get(currentCell.getColumnIndex())
                            : "column-" + currentCell.getColumnIndex();

                    if (skipColumns.contains(CellReference.convertNumToColString(currentCell.getColumnIndex())) ||
                            skipColumns.contains(String.valueOf(currentCell.getColumnIndex())) ||
                            skipColumns.contains(name)) {
                        // names[currentCell.getColumnIndex()] = currentCell.getStringCellValue();
                        continue;
                    }

                    switch (currentCell.getCellType()) {
                        case NUMERIC:
                            // Try to detect
                            if (DateUtil.isCellDateFormatted(currentCell)) {
                                // date field
                                DateTimeField dateTimeField = new DateTimeField(name, locale);
                                dateTimeField.setType(DateType.PAST);

                                detectField = dateTimeField;
                            } else {
                                detectField = new DoubleField(name, locale);
                            }
                            break;
                        case STRING:
                            // Detect
                            if (StringUtils.isNotBlank(currentCell.getStringCellValue())) {
                                detectField = new TextField(name, locale);
                            }
                            break;
                        case BOOLEAN:
                            detectField = new BooleanField(name, locale);
                            break;
                        case FORMULA:
                            switch (currentCell.getCachedFormulaResultType()) {
                                case NUMERIC:
                                    detectField = new DoubleField(name, locale);
                                    break;
                                case STRING:
                                    if (StringUtils.isNotBlank(currentCell.getStringCellValue())) {
                                        detectField = new TextField(name, locale);
                                    }
                                    break;
                            }
                    }

                    Optional<Field> fieldDetectedUsingName = name.startsWith("column-") ? Optional.empty() : fieldDetectorService.detectTypeOnName(name, locale);
                    Optional<Field> bestFieldMatch = fieldDetectorService.findBestMatch(fieldDetectedUsingName, Optional.ofNullable(detectField));

                    if (bestFieldMatch.isPresent()) {
                        try {
                            detectedFields.set(currentIndex, bestFieldMatch.get());
                        } catch (IndexOutOfBoundsException e) {
                            detectedFields.add(currentIndex, bestFieldMatch.get());
                        }
                        currentIndex += 1;
                    }

                }
            }
        }
        return detectedFields;
    }
    private ArrayList<Field> processColumns(JobConfig config, Locale locale, Sheet datatypeSheet) {
        ArrayList<Field> detectedFields = new ArrayList<>();
        Map<Integer, String> headerNames = Maps.newHashMap();

        if (config.containsKey(EXCEL_HEADER_COLUMN_PROPERTY.getKey())) {
            int headerPos = (int) config.getConfigProperty(EXCEL_HEADER_COLUMN_PROPERTY);
            int startRow = (Integer) config.getConfigProperty(EXCEL_SKIP_ROWS_PROPERTY);

            for (int rowNum = startRow; rowNum <= datatypeSheet.getLastRowNum(); rowNum++) {
                Row row = datatypeSheet.getRow(rowNum);
                if (row != null) {
                    Cell cell = row.getCell(headerPos);
                    headerNames.put(rowNum, StringUtils.isBlank(cell.getStringCellValue()) ? "row-" + rowNum : cell.getStringCellValue());
                }
            }
        }

        if (config.containsKey(EXCEL_DATA_TYPE_COLUMN_PROPERTY.getKey())) {
            int dataTypePos = (int) config.getConfigProperty(EXCEL_DATA_TYPE_COLUMN_PROPERTY);
            int startRow = (Integer) config.getConfigProperty(EXCEL_SKIP_ROWS_PROPERTY);

            for (int rowNum = startRow; rowNum <= datatypeSheet.getLastRowNum(); rowNum++) {
                Row row = datatypeSheet.getRow(rowNum);
                if (row != null) {
                    Cell currentCell = row.getCell(dataTypePos);
                    int currentRow = row.getRowNum();

                    String name = headerNames.containsKey(currentRow) ? headerNames.get(currentRow) : "row-" + currentRow;

                    Optional<Field> fieldDetectedUsingName = name.startsWith(
                            "row-") ? Optional.empty() : fieldDetectorService.detectTypeOnName(name, locale);
                    Optional<Field> detectedField = fieldDetectorService.detectType(currentCell.getStringCellValue(),
                                                                                    name, locale);
                    Optional<Field> bestFieldMatch = fieldDetectorService.findBestMatch(fieldDetectedUsingName,
                                                                                        detectedField);

                    bestFieldMatch.ifPresent(f -> {
                        if (config.containsKey(EXCEL_DESCRIPTION_COLUMN_PROPERTY.getKey())) {
                            int descriptionPos = (int) config.getConfigProperty(EXCEL_DESCRIPTION_COLUMN_PROPERTY);
                            Cell description = row.getCell(descriptionPos);
                            if (description != null) {
                                f.setDescription(description.getStringCellValue());
                            }
                        }
                        detectedFields.add(f);
                    });
                }
            }
        }

        return detectedFields;
    }


    @Override
    public Set<SupportedMediaType> supportedTypes() {
        return Sets.newHashSet(SupportedMediaType.EXCEL);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(LOCALE_PROPERTY,
                                  EXCEL_SHEET_NAME_PROPERTY,
                                  EXCEL_SHEET_NUMBER_PROPERTY,
                                  EXCEL_PROCESS_ROWS_PROPERTY,
                                  EXCEL_SKIP_COLUMNS_PROPERTY,
                                  EXCEL_HEADER_ROW_PROPERTY,
                                  EXCEL_DATA_TYPE_ROW_PROPERTY,
                                  EXCEL_SKIP_ROWS_PROPERTY,
                                  EXCEL_PROCESS_NUMBER_LINES_PROPERTY,
                                  EXCEL_HEADER_COLUMN_PROPERTY,
                                  EXCEL_DATA_TYPE_COLUMN_PROPERTY,
                                  EXCEL_DESCRIPTION_COLUMN_PROPERTY);
    }
}

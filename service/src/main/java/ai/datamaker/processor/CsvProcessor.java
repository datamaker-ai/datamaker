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
import ai.datamaker.model.field.type.EmptyField;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVParser;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Detect datatype based on values, trial and error.
 * Guess range using first 100 lines.
 * Guess format based on data.
 */
@Slf4j
@Component
public class CsvProcessor extends DatasetProcessor {

    static final PropertyConfig CSV_SKIP_NUMBER_LINES_PROPERTY =
            new PropertyConfig("csv.processor.skip.lines.number",
                               "Number of lines to skip",
                               PropertyConfig.ValueType.NUMERIC,
                               0,
                               Collections.emptyList());

    static final PropertyConfig CSV_PROCESS_NUMBER_LINES_PROPERTY =
            new PropertyConfig("csv.processor.process.lines.number",
                               "Number of lines to process",
                               PropertyConfig.ValueType.NUMERIC,
                               10,
                               Collections.emptyList());

    static final PropertyConfig CSV_CHARSET_PROPERTY =
            new PropertyConfig("csv.processor.file.charset",
                               "File encoding",
                               PropertyConfig.ValueType.STRING,
                               StandardCharsets.UTF_8.name(),
                               Collections.emptyList());

    static final PropertyConfig CSV_SEPARATOR_PROPERTY =
            new PropertyConfig("csv.processor.separator",
                               "File separator",
                               PropertyConfig.ValueType.STRING,
                               ",",
                               Collections.emptyList());

    static final PropertyConfig CSV_HEADER_FIRST_LINE_PROPERTY =
            new PropertyConfig("csv.processor.first.line.header",
                               "Treat first line as header",
                               PropertyConfig.ValueType.BOOLEAN,
                               true,
                               Lists.newArrayList(true, false));

    @Override
    public Optional<Dataset> process(InputStream input, JobConfig config) {
        Integer skipLines = (Integer) config.getConfigProperty(CSV_SKIP_NUMBER_LINES_PROPERTY);
        Integer linesToProcess = (Integer) config.getConfigProperty(CSV_PROCESS_NUMBER_LINES_PROPERTY);
        String charset = (String) config.getConfigProperty(CSV_CHARSET_PROPERTY);
        String separator = (String) config.getConfigProperty(CSV_SEPARATOR_PROPERTY);
        Boolean header = (Boolean) config.getConfigProperty(CSV_HEADER_FIRST_LINE_PROPERTY);
        Locale locale = getLocale(config);
        String datasetName = (String) config.getConfigProperty(INPUT_FILENAME_PROPERTY);

        // TODO parse per column
        // column for name, for type, for value

        Dataset dataset = new Dataset(datasetName, locale);

        final CSVParser parser =
                new CSVParserBuilder()
                        .withSeparator(separator.charAt(0))
                        //.withIgnoreQuotations(true)
                        .withQuoteChar(ICSVParser.DEFAULT_QUOTE_CHARACTER)
                        .withEscapeChar(ICSVParser.DEFAULT_ESCAPE_CHARACTER)
                        .withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                        .build();
        String[] names = null;
        String[][] values = null;

        try (CSVReader csvReader =
                     new CSVReaderBuilder(new InputStreamReader(input, charset))
                             .withSkipLines(skipLines)
                             .withFieldAsNull(CSVReaderNullFieldIndicator.BOTH)
                             .withCSVParser(parser)
                             .build()) {

            String[] line = null;
            while ((line = csvReader.readNext()) != null) {
                long lineNumber = csvReader.getLinesRead() - skipLines - (header ? 1 : 0);
                if (header && names == null) {
                    // We receive the header
                    names = line;
                    values = new String[linesToProcess][line.length];
                } else if (lineNumber <= linesToProcess) {
                    if (names == null) {
                        names = new String[line.length];
                        for (int i = 0; i < line.length; i++) {
                            names[i] = "column-" + (i + 1);
                        }
                        values = new String[linesToProcess][line.length];
                    }

                    values[(int) (lineNumber - 1)] = line;
                } else {
                    // Number of lines to process reached
                    break;
                }
            }

        } catch (Exception e) {
            log.warn("Error while parsing CSV", e);
            throw new IllegalStateException("Error while parsing CSV", e);
        }

        if (names == null || values == null) {
            throw new IllegalArgumentException("no values found in csv file");
        }

        // If no header, use column - index
        for (int i = 0; i < names.length; i++) {
            String[] columnArray = new String[values.length];
            for (int j = 0; j < values.length; j++) {
                String value = values[j][i];
                columnArray[j] = value;
                //log.debug(value);
            }

            //Optional<Field> detectedField = fieldDetectorService.detect(names[i], locale, Arrays.asList(columnArray));
            // Try with name first
            Optional<Field> fieldFoundName = header ? fieldDetectorService.detectTypeOnName(names[i],
                                                                                            locale) : Optional.empty();

            // Try best match with all values
            Optional<Field> fieldFoundValue = fieldDetectorService.detectTypeOnValue(names[i], locale,
                                                                                     Arrays.asList(columnArray));

            final String name = names[i];
            fieldDetectorService
                .findBestMatch(fieldFoundName, fieldFoundValue)
                .ifPresentOrElse(dataset::addField, () -> dataset.addField(new EmptyField(name, locale)));

//            if (fieldFoundName.isPresent()) {
//                dataset.addField(fieldFoundName.get());
//            } else if (fieldFoundValue.isPresent()) {
//                fieldFoundValue.ifPresent(dataset::addField);
//            } else {
//                dataset.addField(new EmptyField(names[i], locale));
//            }

        }

        return Optional.of(dataset);


    }

    private void parseTypeRow() {
        // FIXME implements
    }

    @Override
    public Set<SupportedMediaType> supportedTypes() {
        return Sets.newHashSet(SupportedMediaType.CSV, SupportedMediaType.TSV, SupportedMediaType.TEXT);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(CSV_SKIP_NUMBER_LINES_PROPERTY,
                                  CSV_PROCESS_NUMBER_LINES_PROPERTY,
                                  CSV_CHARSET_PROPERTY,
                                  CSV_SEPARATOR_PROPERTY,
                                  CSV_HEADER_FIRST_LINE_PROPERTY,
                                  INPUT_FILENAME_PROPERTY,
                                  LOCALE_PROPERTY);
    }
}

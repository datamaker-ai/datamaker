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
import ai.datamaker.model.field.Field;
import com.google.common.collect.Lists;
import com.opencsv.CSVWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CsvGenerator implements DataGenerator {

    static final PropertyConfig CSV_FILE_ENCODING =
            new PropertyConfig("csv.generator.file.encoding",
                               "File encoding",
                               PropertyConfig.ValueType.STRING,
                               "UTF-8",
                               Collections.emptyList());

    static final PropertyConfig CSV_DELIMITER =
            new PropertyConfig("csv.generator.delimiter",
                               "Delimiter",
                               PropertyConfig.ValueType.STRING,
                               ",",
                               Collections.emptyList());


    static final PropertyConfig CSV_QUOTE_CHARACTER =
            new PropertyConfig("csv.generator.quote.char",
                               "Quote char",
                               PropertyConfig.ValueType.STRING,
                               "\"",
                               Collections.emptyList());

    static final PropertyConfig CSV_ESCAPE_CHARACTER =
            new PropertyConfig("csv.generator.escape.char",
                               "Escape char",
                               PropertyConfig.ValueType.STRING,
                               "\"",
                               Collections.emptyList());

    static final PropertyConfig CSV_END_OF_LINE =
            new PropertyConfig("csv.generator.line.ending",
                               "Line ending",
                               PropertyConfig.ValueType.STRING,
                               "\n",
                               Collections.emptyList());

    static final PropertyConfig CSV_NULL_VALUE =
            new PropertyConfig("csv.generator.null.value",
                               "Null value",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    static final PropertyConfig CSV_QUOTES_ALL =
            new PropertyConfig("csv.generator.quote.all",
                               "Quote all",
                               PropertyConfig.ValueType.BOOLEAN,
                               false,
                               Arrays.asList(true, false));

    @Override
    public List<PropertyConfig> getConfigProperties() {
        List<PropertyConfig> properties = Lists.newArrayList();
        properties.add(CSV_FILE_ENCODING);
        properties.add(CSV_DELIMITER);
        properties.add(CSV_QUOTE_CHARACTER);
        properties.add(CSV_ESCAPE_CHARACTER);
        properties.add(CSV_END_OF_LINE);
        properties.add(CSV_NULL_VALUE);
        properties.add(CSV_QUOTES_ALL);
        return properties;
    }

    @Override
    public void generate(Dataset dataset, OutputStream outputStream) throws Exception {
        generate(dataset, outputStream, JobConfig.EMPTY);
    }

    @Override
    public void generate(Dataset dataset, OutputStream outputStream, JobConfig config) throws Exception {
        try (CSVWriter writer = new CSVWriter(
            new OutputStreamWriter(outputStream, (String) config.getConfigProperty(CSV_FILE_ENCODING)),
            ((String) config.getConfigProperty(CSV_DELIMITER)).charAt(0),
            ((String) config.getConfigProperty(CSV_QUOTE_CHARACTER)).charAt(0),
            ((String) config.getConfigProperty(CSV_ESCAPE_CHARACTER)).charAt(0),
            (String) config.getConfigProperty(CSV_END_OF_LINE))) {

            boolean applyQuotesToAll = (boolean) config.getConfigProperty(CSV_QUOTES_ALL);

            // Add header on first line
            if (dataset.getExportHeader()) {
                String[] line = dataset.getFields().stream().map(Field::getName).toArray(String[]::new);
                writer.writeNext(line, applyQuotesToAll);
                if (dataset.getFlushOnEveryRecord()) {
                    writer.flush();
                }
            }

            final String nullValue = (String) config.getConfigProperty(CSV_NULL_VALUE);

            dataset.processAllValues(fieldValues -> {
                String[] line = fieldValues.
                    stream()
                    .map(c -> {
                        Object o = c.getValue();
                        return o == null ? nullValue : o.toString();
                    })
                    .toArray(String[]::new);
                writer.writeNext(line, applyQuotesToAll);
                //writer.flushQuietly();
                if (dataset.getFlushOnEveryRecord()) {
                    try {
                        writer.flush();
                    } catch (IOException e) {
                        throw new IllegalStateException("Error while trying to flush writer", e);
                    }
                }
            });
        }
    }

    @Override
    public FormatType getDataType() {
        return FormatType.CSV;
    }
}

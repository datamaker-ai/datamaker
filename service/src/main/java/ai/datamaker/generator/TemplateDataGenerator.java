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

import ai.datamaker.exception.DatasetSerializationException;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.field.FieldValue;
import com.google.common.collect.Lists;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Freemarker template generator.
 */
public class TemplateDataGenerator implements DataGenerator {

    public static final Configuration FREEMARKER_CONFIGURATION = new Configuration(Configuration.VERSION_2_3_30);

    public static final String DEFAULT_TEMPLATE = "Dataset: ${dataset.name}  \n" +
            "<#list fieldValueList as fieldValueList >\n" +
            "<#list fieldValueList as fieldValue>" +
                "\t${fieldValue.field.name}: ${fieldValue.value}\n" +
            "</#list>" +
            "\n" + "=======================\n" +
            "</#list>";

    public static final PropertyConfig CUSTOM_DATA_TEMPLATE =
            new PropertyConfig("custom.generator.template",
                               "Freemarker template",
                               PropertyConfig.ValueType.STRING,
                               DEFAULT_TEMPLATE,
                               Collections.emptyList());

    @Override
    public void generate(Dataset dataset, OutputStream outputStream) throws Exception {
        generate(dataset, outputStream, JobConfig.EMPTY);
    }

    @Override
    public void generate(Dataset dataset, OutputStream outputStream, JobConfig config) throws Exception {
        List<List<FieldValue>> accumulator = Lists.newArrayList();

        String sourceCode = (String) config.getConfigProperty(CUSTOM_DATA_TEMPLATE);

        // Build the data-model
        Map<String, Object> root = new HashMap<>();
        root.put("dataset", dataset);
        root.put("fieldValueList", accumulator);
        dataset.processAllValues(accumulator::add);
        Writer w = new OutputStreamWriter(outputStream);
        try {
            Template template = new Template("custom", sourceCode, FREEMARKER_CONFIGURATION);
            template.process(root, w);
        } catch (Exception e) {
            throw new DatasetSerializationException("Error while generating template", e, dataset);
        }
    }

    @Override
    public FormatType getDataType() {
        return FormatType.CUSTOM_TEMPLATE;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(CUSTOM_DATA_TEMPLATE);
    }
}

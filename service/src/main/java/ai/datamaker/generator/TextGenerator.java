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
import ai.datamaker.model.PropertyConfig.ValueType;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

public class TextGenerator implements DataGenerator {

    static final PropertyConfig ELEMENT_SEPARATOR =
        new PropertyConfig("text.generator.element.separator",
            "Element separator",
            PropertyConfig.ValueType.STRING,
            "",
            Collections.emptyList());

    static final PropertyConfig KEY_VALUE_SEPARATOR =
        new PropertyConfig("text.generator.key.value.separator",
            "Key value separator",
            PropertyConfig.ValueType.STRING,
            "=",
            Collections.emptyList());

    static final PropertyConfig OUTPUT_KEYS =
        new PropertyConfig("text.generator.output.keys",
            "Output keys",
            ValueType.BOOLEAN,
            "false",
            Collections.emptyList());

    @Override
    public void generate(Dataset dataset, OutputStream outputStream) throws Exception {
        generate(dataset, outputStream, JobConfig.EMPTY);
    }

    @Override
    public void generate(Dataset dataset, OutputStream outputStream, JobConfig config) throws Exception {
        dataset.processAllValues(fv -> {
            fv.forEach(value -> {
                    try {
                        if (Boolean.parseBoolean(config.getConfigProperty(OUTPUT_KEYS).toString())) {
                            outputStream.write(value.getField().getName().getBytes());
                            outputStream.write(config.getConfigProperty(KEY_VALUE_SEPARATOR).toString().getBytes());
                        }
                        outputStream.write(value.getValue().toString().getBytes());
                        outputStream.write(config.getConfigProperty(ELEMENT_SEPARATOR).toString().getBytes());
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            );
        });
    }

    @Override
    public FormatType getDataType() {
        return FormatType.TEXT;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(ELEMENT_SEPARATOR, KEY_VALUE_SEPARATOR, OUTPUT_KEYS);
    }
}

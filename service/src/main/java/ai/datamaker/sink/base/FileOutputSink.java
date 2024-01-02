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

package ai.datamaker.sink.base;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.DataOutputSinkType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.PropertyConfig.ValueType;
import ai.datamaker.model.job.JobExecution;
import ai.datamaker.sink.DataOutputSink;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * Will generate a downloadable file.
 */
@Slf4j
@DataOutputSinkType(compressed = true, encrypted = true)
public class FileOutputSink implements DataOutputSink {

    public static final PropertyConfig FILE_OUTPUT_PATH_PROPERTY
        = new PropertyConfig(
        "file.sink.output.filename",
        "Output file path",
        ValueType.EXPRESSION,
        "\"/tmp/\" + #dataset.name + \"-\" + T(java.lang.System).currentTimeMillis() + \".\" + #dataJob.generator.dataType.name().toLowerCase()",
        Collections.emptyList());

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(FILE_OUTPUT_PATH_PROPERTY);
    }

    public OutputStream getOutputStream(JobConfig config) throws Exception {
        JobExecution jobExecution = config.getJobExecution();

        String path = (String) config.getConfigProperty(FILE_OUTPUT_PATH_PROPERTY);

        jobExecution.getResults().add(path);

        return new FileOutputStream(new File(path));
    }
}

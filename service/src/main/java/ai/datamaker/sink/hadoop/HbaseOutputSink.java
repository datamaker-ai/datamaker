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

package ai.datamaker.sink.hadoop;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.DataOutputSink;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * Support partitions.
 */
@Deprecated
public class HbaseOutputSink implements DataOutputSink {

    private String configurationPath;
    private String principal;
    private String keytab;
    private String tableName;
    private String rowKey;
    private String columnFamily;
    private String columnName;

    @Override
    public boolean accept(FormatType type) {
        return type == FormatType.JSON || type == FormatType.CSV;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {
        return null;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Collections.emptyList();
    }

}

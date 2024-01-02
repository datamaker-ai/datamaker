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

package ai.datamaker.sink.solr;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.DataOutputSink;
import ai.datamaker.sink.base.HttpOutputSink;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

public class SolrOutputSink extends HttpOutputSink implements DataOutputSink {

    public static final PropertyConfig SOLR_SPNEGO =
            new PropertyConfig("solr.sink.spnego",
                               "Username",
                               PropertyConfig.ValueType.STRING,
                               "solr",
                               Collections.emptyList());

    @Override
    public boolean accept(FormatType type) {
        return type == FormatType.JSON;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {
        // TODO supports Kerberos and HTTPS
        return null;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Collections.emptyList();
    }
}

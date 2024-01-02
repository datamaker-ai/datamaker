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
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.DataOutputSink;
import com.google.common.collect.Sets;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Copy the data in a {@link String}
 */
public class StringOutputSink implements DataOutputSink {

    private static final Set<FormatType> ACCEPTED_TYPES = Sets.newHashSet(FormatType.JSON,
                                                                          FormatType.CSV,
                                                                          FormatType.SQL,
                                                                          FormatType.XML,
                                                                          FormatType.TEXT);

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    @Override
    public boolean accept(FormatType type) {
        return ACCEPTED_TYPES.contains(type);
    }

    @Override
    public OutputStream getOutputStream() throws Exception {
        return outputStream;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {
        return outputStream;
    }

    public String getOutput() {
        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Collections.emptyList();
    }
}

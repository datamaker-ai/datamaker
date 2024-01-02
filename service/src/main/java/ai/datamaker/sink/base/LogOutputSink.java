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
import ai.datamaker.model.PropertyConfig.ValueType;
import ai.datamaker.sink.DataOutputSink;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

@Slf4j
public class LogOutputSink implements DataOutputSink {

    public static final PropertyConfig LOG_PREFIX = new PropertyConfig(
                                "log.sink.prefix",
                                "Prefix",
                                ValueType.STRING,
                                "DATA: ",
                                Collections.emptyList());

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {

        final String prefix = (String) config.getConfigProperty(LOG_PREFIX);

        return new OutputStream() {

            private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            @Override
            public void write(int b) throws IOException {
                byteArrayOutputStream.write(b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                byteArrayOutputStream.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                byteArrayOutputStream.write(b, off, len);
            }

            @Override
            public void flush() throws IOException {
                if (byteArrayOutputStream.size() > 0) {
                    log.info("{} {}", prefix, byteArrayOutputStream.toString());
                }
                byteArrayOutputStream.reset();
            }

            @Override
            public void close() throws IOException {
                if (byteArrayOutputStream.size() > 0) {
                    log.info("{} {}", prefix, byteArrayOutputStream.toString());
                }
                byteArrayOutputStream.reset();
            }
        };
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(LOG_PREFIX);
    }
}

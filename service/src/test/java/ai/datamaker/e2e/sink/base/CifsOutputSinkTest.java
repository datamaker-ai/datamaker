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

package ai.datamaker.e2e.sink.base;

import ai.datamaker.model.JobConfig;
import ai.datamaker.sink.base.CifsOutputSink;
import ai.datamaker.sink.filter.CompressFilter;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;

@Disabled
class CifsOutputSinkTest {
    private CifsOutputSink sink = new CifsOutputSink();

    @Test
    void accept() {
    }

    @Test
    void getOutputStream() throws Exception {
        JobConfig config = new JobConfig();
        config.put(CompressFilter.COMPRESSION_FORMAT.getKey(), "ZIP");
        config.put(CifsOutputSink.CIFS_HOSTNAME.getKey(), "192.168.1.10");
        config.put(CifsOutputSink.FILE_OUTPUT_PATH_PROPERTY.getKey(), "'/Downloads/test.zip'");

        try (OutputStream outputStream = sink.getOutputStream(config)) {
            outputStream.write("hello, world".getBytes());
        }
    }

    @Test
    void getConfigProperties() {
    }
}
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

package ai.datamaker.sink;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.sink.base.SocketOutputSink;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SocketOutputSinkTest {

    private SocketOutputSink sink = new SocketOutputSink();

    @Test
    void accept() {
        Arrays.stream(FormatType.values()).forEach(f -> assertTrue(sink.accept(f)));
    }

//    @Test
//    void getOutputStream() throws Exception {
//        Properties props = new Properties();
//        props.put(Constants.PORT_NUMBER, 1);
//        props.put(Constants.HOST_NAME, "localhost");
//
//        OutputStream outputStream = sink.getOutputStream(props);
//
//        assertNotNull(outputStream);
//    }

    @Test
    void getOutputStream_invalid() {
        // Invalid port number
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            JobConfig config = new JobConfig();
            config.put(SocketOutputSink.PORT_NUMBER.getKey(), 43242342);

            sink.getOutputStream(config);
        }, "Invalid port number");
    }
}
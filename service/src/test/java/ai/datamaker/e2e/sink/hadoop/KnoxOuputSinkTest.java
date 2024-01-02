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

package ai.datamaker.e2e.sink.hadoop;

import ai.datamaker.model.JobConfig;
import ai.datamaker.sink.hadoop.KnoxOuputSink;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;

@Disabled
class KnoxOuputSinkTest {

    private KnoxOuputSink sink = new KnoxOuputSink();

    @Test
    void accept() {
    }

    @Test
    void getOutputStream() throws Exception {
        System.setProperty("javax.net.ssl.trustStore", "C:\\Users\\DVT6131\\workspace\\keystore\\cacerts");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

        // https://de-lachdp-mm11.azure.mvtdesjardins.com:50470/webhdfs/v1/?op=LISTSTATUS
        JobConfig config = new JobConfig();
        config.put(KnoxOuputSink.HTTP_ENDPOINT.getKey(), "https://de-lachdp-g8.azure.mvtdesjardins.com:8443/gateway/knox/webhdfs/v1");
        config.put(KnoxOuputSink.BASIC_AUTH_USERNAME, "sdatamakernprod");
        config.put(KnoxOuputSink.BASIC_AUTH_PASSWORD, "C%t7aG6^1_Ejz8?LA=b09K*es");
        config.put(KnoxOuputSink.FILE_OUTPUT_PATH_PROPERTY, "'/tmp/test-write-knox.csv'");
        try (OutputStream stream = sink.getOutputStream(config)) {
            stream.write("Hello, world!".getBytes());
        }
    }

    @Test
    void getConfigProperties() {
    }
}
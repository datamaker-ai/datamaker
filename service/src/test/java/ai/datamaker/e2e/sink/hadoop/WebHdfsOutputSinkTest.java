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
import ai.datamaker.sink.hadoop.WebHdfsOutputSink;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;

@Disabled
class WebHdfsOutputSinkTest {

    private WebHdfsOutputSink sink = new WebHdfsOutputSink();

    @Test
    void accept() {
    }

    @Test
    void getOutputStream() throws Exception {
        JobConfig config = new JobConfig();
        try (OutputStream stream = sink.getOutputStream(config)) {
            //stream.write("hello world".getBytes());
        }
    }

    @Test
    void getOutputStream_secure() throws Exception {
        System.setProperty("javax.net.ssl.trustStore", "C:\\Users\\DVT6131\\workspace\\keystore\\cacerts");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

        // https://de-lachdp-mm11.azure.mvtdesjardins.com:50470/webhdfs/v1/?op=LISTSTATUS
        JobConfig config = new JobConfig();
        config.put(WebHdfsOutputSink.SECURED_CLUSTER.getKey(), true);
        config.put(WebHdfsOutputSink.HTTP_ENDPOINT.getKey(), "https://de-lachdp-mm11.azure.mvtdesjardins.com:50470/webhdfs/v1");
        config.put(WebHdfsOutputSink.HADOOP_HDFS_PRINCIPAL, "sallsightnprod@AZURE.MVTDESJARDINS.COM");
        config.put(WebHdfsOutputSink.HADOOP_HDFS_KEYTAB, "C:\\Users\\DVT6131\\workspace\\cdp-tranform\\conf\\sallsightnprod_spark.keytab");
        config.put(WebHdfsOutputSink.FILE_OUTPUT_PATH_PROPERTY, "'/tmp/test-write'");
        try (OutputStream stream = sink.getOutputStream(config)) {
            stream.write("Hello, world!".getBytes());
        }
    }

    @Test
    void getConfigProperties() {
    }
}
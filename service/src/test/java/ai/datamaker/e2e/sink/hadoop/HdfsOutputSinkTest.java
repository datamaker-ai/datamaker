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
import ai.datamaker.sink.hadoop.HdfsOutputSink;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.nio.ByteBuffer;

@Disabled
class HdfsOutputSinkTest {

    private HdfsOutputSink sink = new HdfsOutputSink();

    @Test
    void accept() {
    }

    @Test
    void getOutputStream() throws Exception {
        JobConfig config = new JobConfig();
        try (OutputStream stream = sink.getOutputStream(config)) {
            stream.write("hello,world,!".getBytes());
        }
    }

    @Test
    void getOutputStream_secured() throws Exception {
        System.setProperty("javax.net.ssl.trustStore", "C:\\Users\\DVT6131\\workspace\\keystore\\cacerts");
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

        // https://de-lachdp-mm11.azure.mvtdesjardins.com:50470/webhdfs/v1/?op=LISTSTATUS
        JobConfig config = new JobConfig();
        config.put(HdfsOutputSink.SECURED_CLUSTER.getKey(), true);
//        config.put(HdfsOutputSink.HTTP_ENDPOINT.getKey(), "https://de-lachdp-mm11.azure.mvtdesjardins.com:50470/webhdfs/v1");
        config.put(HdfsOutputSink.HADOOP_HDFS_PRINCIPAL, "sallsightnprod@AZURE.MVTDESJARDINS.COM");
        config.put(HdfsOutputSink.HADOOP_HDFS_KEYTAB, "C:\\Users\\DVT6131\\workspace\\cdp-tranform\\conf\\sallsightnprod_spark.keytab");
        config.put(HdfsOutputSink.FILENAME_PROPERTY, "'test-write-hadoop.csv'");
        try (OutputStream stream = sink.getOutputStream(config)) {
            stream.write("Hello, world!".getBytes());
            byte[] bytes = new byte[8];

            stream.write(ByteBuffer.wrap(bytes).putDouble(125.53538d).array());
        }
    }

    @Test
    void getConfigProperties() {
    }
}
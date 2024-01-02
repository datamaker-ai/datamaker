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

package ai.datamaker.e2e.sink.amazon;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.sink.amazon.AmazonKinesisOutputSink;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

import java.io.OutputStream;
import java.util.Locale;

@Disabled
class AmazonKinesisOutputSinkTest {

    private AmazonKinesisOutputSink sink = new AmazonKinesisOutputSink();

    @Test
    void getConfigProperties() {
    }

    @Test
    void accept() {
    }

    @Test
    void getOutputStream() throws Exception {
        JobConfig config = new JobConfig();
        Dataset dataset = new Dataset("test", Locale.ENGLISH);
        config.setDataset(dataset);

        config.put(AmazonKinesisOutputSink.KINESIS_STREAM_NAME.getKey(), "datamaker");
        config.put(AmazonKinesisOutputSink.AMAZON_AWS_REGION.getKey(), Region.CA_CENTRAL_1.toString());
        config.put(AmazonKinesisOutputSink.AMAZON_AWS_ACCESS_KEY_ID.getKey(), "AKIAJBR2ON5J4F3CHCJQ");
        config.put(AmazonKinesisOutputSink.AMAZON_AWS_SECRET_ACCESS_KEY.getKey(), "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");

        try (OutputStream stream = sink.getOutputStream(config)) {
            stream.write(("{\"name\": \"Hello world! " + System.currentTimeMillis() + "\"}").getBytes());
        }
    }
}
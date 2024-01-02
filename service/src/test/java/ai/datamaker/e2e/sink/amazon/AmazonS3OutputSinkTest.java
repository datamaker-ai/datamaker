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

import ai.datamaker.model.JobConfig;
import ai.datamaker.sink.amazon.AmazonAwsCommon;
import ai.datamaker.sink.amazon.AmazonS3OutputSink;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;

import java.io.OutputStream;

@Disabled
class AmazonS3OutputSinkTest {

    private AmazonS3OutputSink sink = new AmazonS3OutputSink();

    @Test
    void getConfigProperties() {
    }

    @Test
    void accept() {
    }

    @Test
    void getOutputStream() throws Exception {
        JobConfig config = new JobConfig();
        config.put(AmazonS3OutputSink.S3_BUCKET_NAME_PROPERTY.getKey(), "elasticbeanstalk-datamaker");
        config.put(AmazonS3OutputSink.S3_FILE_NAME_PATTERN_PROPERTY.getKey(), "\"datamaker.test\"");
        config.put(AmazonS3OutputSink.AMAZON_AWS_REGION.getKey(), Region.CA_CENTRAL_1.toString());
        config.put(AmazonAwsCommon.AMAZON_AWS_ACCESS_KEY_ID.getKey(), "AKIA557JKSZV5JKNR6WQ");
        config.put(AmazonAwsCommon.AMAZON_AWS_SECRET_ACCESS_KEY.getKey(), "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");
        //config.put(AmazonS3OutputSink.S3_STORAGE_CLASS.getKey(), "");

        try (OutputStream outputStream = sink.getOutputStream(config)) {
            outputStream.write("allo".getBytes());
        }
    }
}
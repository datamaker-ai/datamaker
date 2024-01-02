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

package ai.datamaker.sink.amazon;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.DataOutputSink;
import ai.datamaker.utils.stream.SendDataOutputStream;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamResponse;
import software.amazon.awssdk.services.kinesis.model.KinesisException;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class AmazonKinesisOutputSink implements DataOutputSink, AmazonAwsCommon {

    public static final PropertyConfig KINESIS_STREAM_NAME
            = new PropertyConfig(
            "amazon.kinesis.sink.stream.name",
            "The name of the stream to put the data record into",
            PropertyConfig.ValueType.STRING,
            "",
            Collections.emptyList());

    public static final PropertyConfig KINESIS_PARTITION_KEY
            = new PropertyConfig(
            "amazon.kinesis.sink.partition.key",
            "Determines which shard in the stream the data record is assigned to. Partition keys are Unicode strings with a maximum length limit of 256 characters for each key",
            PropertyConfig.ValueType.EXPRESSION,
            "#dataset.name",
            Collections.emptyList());

    public static final PropertyConfig KINESIS_EXPLICIT_HASH_KEY
            = new PropertyConfig(
            "amazon.kinesis.sink.explicit.hash.key",
            "The hash value used to explicitly determine the shard the data record is assigned to by overriding the partition key hash.",
            PropertyConfig.ValueType.STRING,
            null,
            Collections.emptyList());

    public static final PropertyConfig KINESIS_SEQUENCE_ORDERING
            = new PropertyConfig(
            "amazon.kinesis.sink.sequence.ordering",
            "Guarantees strictly increasing sequence numbers, for puts from the same client and to the same partition key.",
            PropertyConfig.ValueType.STRING,
            null,
            Collections.emptyList());


    private void validateStream(KinesisAsyncClient kinesisClient, String streamName) throws Exception {
        try {
            DescribeStreamRequest describeStreamRequest = DescribeStreamRequest.builder().streamName(streamName).build();
            CompletableFuture<DescribeStreamResponse> describeStreamResponse = kinesisClient.describeStream(describeStreamRequest);

            if (!describeStreamResponse.get().streamDescription().streamStatus().toString().equals("ACTIVE")) {
                log.error("Stream {} is not active. Please wait a few moments and try again.", streamName);
                throw new IllegalStateException(String.format("Stream %s is not active", streamName));
            }
        } catch (KinesisException e) {
            log.error("Error found while describing the stream {}", streamName, e);
            throw e;
        }
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return addDefaultProperties(Lists.newArrayList(KINESIS_STREAM_NAME,
                                                       KINESIS_PARTITION_KEY,
                                                       KINESIS_EXPLICIT_HASH_KEY,
                                                       KINESIS_SEQUENCE_ORDERING));
    }

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @VisibleForTesting
    KinesisAsyncClient getKinesisClient(JobConfig config) {
        Region region = Region.of((String) config.getConfigProperty(AMAZON_AWS_REGION));

        return KinesisAsyncClient
                .builder()
                .credentialsProvider(getCredentials(config))
                .region(region)
                .build();
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {
        String streamName = (String) config.getConfigProperty(KINESIS_STREAM_NAME);
        KinesisAsyncClient kinesisClient = getKinesisClient(config);

        validateStream(kinesisClient, streamName);

        return new SendDataOutputStream(bytes -> {

            PutRecordRequest request = PutRecordRequest
                    .builder()
                    .partitionKey((String) config.getConfigProperty(KINESIS_PARTITION_KEY))
                    .sequenceNumberForOrdering((String) config.getConfigProperty(KINESIS_SEQUENCE_ORDERING))
                    .explicitHashKey((String) config.getConfigProperty(KINESIS_EXPLICIT_HASH_KEY))
                    .streamName(streamName)
                    .data(SdkBytes.fromByteArray(bytes))
                    .build();

            kinesisClient.putRecord(request);
        });
    }
}

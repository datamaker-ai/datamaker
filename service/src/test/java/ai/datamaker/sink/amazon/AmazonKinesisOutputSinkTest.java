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
import ai.datamaker.sink.amazon.AmazonKinesisOutputSink;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamRequest;
import software.amazon.awssdk.services.kinesis.model.DescribeStreamResponse;
import software.amazon.awssdk.services.kinesis.model.KinesisException;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordResponse;
import software.amazon.awssdk.services.kinesis.model.StreamDescription;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AmazonKinesisOutputSinkTest {

    private final AmazonKinesisOutputSink sink = Mockito.spy(AmazonKinesisOutputSink.class);

    @Test
    void getConfigProperties() {
        assertEquals(7, sink.getConfigProperties().size());
    }

    @Test
    void accept() {
        Arrays.stream(FormatType.values()).forEach(ft -> assertTrue(sink.accept(ft)));
    }

    @Test
    void getOutputStream_inactive_stream() throws Exception {
        KinesisAsyncClient kinesis = Mockito.mock(KinesisAsyncClient.class);

        doReturn(kinesis).when(sink).getKinesisClient(any(JobConfig.class));
        CompletableFuture<DescribeStreamResponse> completableFutureStream = Mockito.mock(CompletableFuture.class);
        when(completableFutureStream.get()).thenReturn(DescribeStreamResponse.builder().streamDescription(StreamDescription.builder().streamStatus("CREATING").build()).build());
        when(kinesis.describeStream(any(DescribeStreamRequest.class))).thenReturn(completableFutureStream);

        JobConfig config = new JobConfig();
        config.put(AmazonKinesisOutputSink.AMAZON_AWS_REGION, "ca-central-1");
        config.put(AmazonKinesisOutputSink.KINESIS_PARTITION_KEY, "'name'");

        sink.getConfigProperties().forEach(cp -> {
                                               if (cp.getType() == PropertyConfig.ValueType.STRING) {
                                                   config.put(cp,
                                                              "abc");
                                               }
                                               if (cp.getType() == PropertyConfig.ValueType.LIST) {
                                                   config.put(cp,
                                                              Lists.newArrayList("'abc'"));
                                               }
                                           }
        );

        assertThrows(IllegalStateException.class, () -> {
            try (OutputStream output = sink.getOutputStream(config)) {
                output.write("hello".getBytes(StandardCharsets.UTF_8));
            }
        });
    }

    @Test
    void getOutputStream_describe_error() throws Exception {
        KinesisAsyncClient kinesis = Mockito.mock(KinesisAsyncClient.class);

        doReturn(kinesis).when(sink).getKinesisClient(any(JobConfig.class));
        when(kinesis.describeStream(any(DescribeStreamRequest.class))).thenThrow(KinesisException.builder().message("error").build());

        JobConfig config = new JobConfig();

        sink.getConfigProperties().forEach(cp -> {
            if (cp.getType() == PropertyConfig.ValueType.STRING) {
                config.put(cp,
                                                              "abc");
            }
            if (cp.getType() == PropertyConfig.ValueType.LIST) {
                config.put(cp,
                                                              Lists.newArrayList("'abc'"));
            }
                                           }
        );
        config.put(AmazonKinesisOutputSink.AMAZON_AWS_REGION, "ca-central-1");
        config.put(AmazonKinesisOutputSink.KINESIS_PARTITION_KEY, "'name'");

        assertThrows(KinesisException.class, () -> {
            try (OutputStream output = sink.getOutputStream(config)) {
                output.write("hello".getBytes(StandardCharsets.UTF_8));
            }
        });
    }

    @Test
    void getOutputStream() throws Exception {

        AmazonKinesisOutputSink sink = Mockito.spy(AmazonKinesisOutputSink.class);
        KinesisAsyncClient kinesis = Mockito.mock(KinesisAsyncClient.class);

        doReturn(kinesis).when(sink).getKinesisClient(any(JobConfig.class));
        CompletableFuture<DescribeStreamResponse> completableFutureStream = Mockito.mock(CompletableFuture.class);
        when(completableFutureStream.get()).thenReturn(DescribeStreamResponse.builder().streamDescription(StreamDescription.builder().streamStatus("ACTIVE").build()).build());
        when(kinesis.describeStream(any(DescribeStreamRequest.class))).thenReturn(completableFutureStream);

        CompletableFuture<PutRecordResponse> completableFutureRecord = Mockito.mock(CompletableFuture.class);
        when(completableFutureRecord.get()).thenReturn(PutRecordResponse.builder().build());
        when(kinesis.putRecord(any(PutRecordRequest.class))).thenReturn(completableFutureRecord);
        // when(sink).getClient(any(JobConfig.class))).thenReturn(s3);

        JobConfig config = new JobConfig();

        sink.getConfigProperties().forEach(cp -> {
                                               if (cp.getType() == PropertyConfig.ValueType.STRING) {
                                                   config.put(cp,
                                                              "abc");
                                               }
                                               if (cp.getType() == PropertyConfig.ValueType.LIST) {
                                                   config.put(cp,
                                                              Lists.newArrayList("'abc'"));
                                               }
                                           }
        );
        config.put(AmazonKinesisOutputSink.AMAZON_AWS_REGION.getKey(), "ca-central-1");
        config.put(AmazonKinesisOutputSink.KINESIS_PARTITION_KEY.getKey(), "'name'");

        try (OutputStream output = sink.getOutputStream(config)) {
            output.write("hello".getBytes(StandardCharsets.UTF_8));
        }
        ArgumentCaptor<PutRecordRequest> putObjectRequestArgumentCaptor = ArgumentCaptor.forClass(PutRecordRequest.class);

        verify(kinesis).putRecord(putObjectRequestArgumentCaptor.capture());

        PutRecordRequest request = putObjectRequestArgumentCaptor.getValue();
        assertEquals("hello", request.getValueForField("Data", software.amazon.awssdk.core.SdkBytes.class).orElseThrow().asUtf8String());
        assertEquals("abc", request.streamName());
        assertEquals("name", request.partitionKey());
        assertEquals("abc", request.explicitHashKey());
        assertEquals("abc", request.sequenceNumberForOrdering());
    }
}
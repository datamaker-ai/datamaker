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

package ai.datamaker.sink.google;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.DataOutputSink;
import ai.datamaker.utils.stream.SendDataOutputStream;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.gax.batching.BatchingSettings;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.threeten.bp.Duration;
import software.amazon.awssdk.utils.StringInputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GooglePubSubOutputSink implements DataOutputSink, GcpCommon {

    private static final ThreadPoolExecutor EXECUTOR = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

    public static final PropertyConfig GOOGLE_PUBSUB_TOPIC_NAME =
            new PropertyConfig("google.pubsub.sink.topic.name",
                               "Topic name",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {

        final Publisher publisher = getPublisher(config);
        final ApiFutureCallback<String> apiFutureCallback = new ApiFutureCallback<>() {
            public void onSuccess(String messageId) {
                log.debug("published with message id: " + messageId);
            }

            public void onFailure(Throwable t) {
                log.error("failed to publish: " + t);
            }
        };

        return new SendDataOutputStream(bytes -> {
            ByteString data = ByteString.copyFrom(bytes);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
//            ApiFutures.addCallback(messageIdFuture, apiFutureCallback, MoreExecutors.directExecutor());
            try {
                // FIXME rework, close reject tasks
                log.debug("Message published: {}", messageIdFuture.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }) {
            @SneakyThrows
            @Override
            public void close() throws IOException {
                super.close();
                //EXECUTOR.shutdown();
                publisher.shutdown();
                publisher.awaitTermination(1, TimeUnit.MINUTES);
            }
        };
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(GOOGLE_PUBSUB_TOPIC_NAME,
                                  GOOGLE_CLOUD_PROJECT_ID,
                                  GOOGLE_CLOUD_AUTHENTICATION_TYPE,
                                  GOOGLE_CLOUD_JSON_KEY,
                                  GOOGLE_CLOUD_OAUTH_TOKEN_VALUE);
    }

    @VisibleForTesting
    Publisher getPublisher(JobConfig config) throws IOException {
        String authenticationType = (String) config.getConfigProperty(GOOGLE_CLOUD_AUTHENTICATION_TYPE);
        String topicName = (String) config.getConfigProperty(GOOGLE_PUBSUB_TOPIC_NAME);
        String projectId = (String) config.getConfigProperty(GOOGLE_CLOUD_PROJECT_ID);

        long requestBytesThreshold = 5000L; // default : 1 byte
        long messageCountBatchSize = 10L; // default : 1 message

        Duration publishDelayThreshold = Duration.ofMillis(100); // default : 1 ms

        // Publish request get triggered based on request size, messages count & time since last
        // publish, whichever condition is met first.
        BatchingSettings batchingSettings =
                BatchingSettings.newBuilder()
                        .setElementCountThreshold(messageCountBatchSize)
                        .setRequestByteThreshold(requestBytesThreshold)
                        .setDelayThreshold(publishDelayThreshold)
                        .build();

        Publisher.Builder builder = Publisher.newBuilder(String.format("projects/%s/topics/%s", projectId, topicName));

        switch (authenticationType) {
            // When using Google Cloud libraries from a Google Cloud Platform environment such as Compute Engine, Kubernetes Engine,
            // or App Engine, no additional authentication steps are necessary.
            case "SERVICE_ACCOUNT":
                String jsonKey = (String) config.getConfigProperty(GOOGLE_CLOUD_JSON_KEY);
                builder.setCredentialsProvider(FixedCredentialsProvider.create(GoogleCredentials.fromStream(new StringInputStream(jsonKey))));
                break;
            case "OAUTH_TOKEN":
                String accessToken = (String) config.getConfigProperty(GOOGLE_CLOUD_OAUTH_TOKEN_VALUE);
                Credentials credentials = GoogleCredentials.create(new AccessToken(accessToken, new Date()));
                builder.setCredentialsProvider(FixedCredentialsProvider.create(credentials));
                break;
            case "PLATFORM":
        }

        return builder.build();
    }
}

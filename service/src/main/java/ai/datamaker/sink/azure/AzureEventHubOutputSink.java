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

package ai.datamaker.sink.azure;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.DataOutputSink;
import ai.datamaker.utils.stream.SendDataOutputStream;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class AzureEventHubOutputSink implements DataOutputSink {
    private static final String EVENT_NUMBER = "EVENT_NUMBER";

    public static final PropertyConfig AZURE_EVENT_HUB_CONNECTION_STRING = new PropertyConfig("azure.eventhub.sink.connection.string",
                                                                                              "Connection string",
                                                                                              PropertyConfig.ValueType.STRING,
                                                                                              "",
                                                                                              Collections.emptyList());
    public static final PropertyConfig AZURE_EVENT_HUB_PARTITION_KEY = new PropertyConfig("azure.eventhub.sink.partition.key",
                                                                                          "The partition hashing key to associate with the event or batch of events.",
                                                                                          PropertyConfig.ValueType.STRING,
                                                                                          null,
                                                                                          Collections.emptyList());

    public static final PropertyConfig AZURE_EVENT_HUB_PARTITION_ID = new PropertyConfig("azure.eventhub.sink.partition.id",
                                                                                         "The identifier of the Event Hub partition that the batch's events will be sent to. " + "null or an empty string if Event Hubs service is responsible for routing events.",
                                                                                         PropertyConfig.ValueType.STRING,
                                                                                         null,
                                                                                         Collections.emptyList());
    public static final PropertyConfig AZURE_EVENT_HUB_BATCH_SIZE = new PropertyConfig("azure.eventhub.sink.batch.size",
                                                                                       "Batch size (bytes)",
                                                                                       PropertyConfig.ValueType.NUMERIC,
                                                                                       1024,
                                                                                       Collections.emptyList());

    public static final PropertyConfig AZURE_EVENT_HUB_BATCH_DURATION = new PropertyConfig("azure.eventhub.sink.batch.duration",
                                                                                           "Batch duration (seconds)",
                                                                                           PropertyConfig.ValueType.NUMERIC,
                                                                                           1,
                                                                                           Collections.emptyList());


    public static final PropertyConfig AZURE_EVENT_HUB_PROPERTIES_NAME =
            new PropertyConfig("azure.eventhub.sink.properties.name",
                               "Properties names",
                               PropertyConfig.ValueType.LIST,
                               Collections.emptyList(),
                               Collections.emptyList());

    public static final PropertyConfig AZURE_EVENT_HUB_PROPERTIES_VALUE =
            new PropertyConfig("azure.eventhub.sink.properties.values",
                               "Properties values (support expression)",
                               PropertyConfig.ValueType.LIST,
                               Collections.emptyList(),
                               Collections.emptyList());

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(AZURE_EVENT_HUB_CONNECTION_STRING,
                                  AZURE_EVENT_HUB_PARTITION_KEY,
                                  AZURE_EVENT_HUB_PARTITION_ID,
                                  AZURE_EVENT_HUB_BATCH_SIZE,
                                  AZURE_EVENT_HUB_BATCH_DURATION);
    }

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {
        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        // String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};" + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        String connectionString = (String) config.getConfigProperty(AZURE_EVENT_HUB_CONNECTION_STRING);

        // Limit the size of the batches to 1024 bytes instead of using the default max size.
        final CreateBatchOptions batchOptions = new CreateBatchOptions()
                .setMaximumSizeInBytes((Integer) config.getConfigProperty(AZURE_EVENT_HUB_BATCH_SIZE))
                .setPartitionKey((String) config.getConfigProperty(AZURE_EVENT_HUB_PARTITION_KEY))
                .setPartitionId((String) config.getConfigProperty(AZURE_EVENT_HUB_PARTITION_ID));
        final CustomPublisher publisher = new CustomPublisher(getClient(connectionString), Duration.ofSeconds(1), batchOptions);

        // This represents a stream of events that we want to publish.
        //final DirectProcessor<EventData> events = DirectProcessor.create();
        //final Flux<EventData> events =  Sinks.many().unicast().<EventData>onBackpressureBuffer().asFlux();
        final Sinks.Many<EventData> events = Sinks.many().multicast().directBestEffort();

//        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
//
        log.debug("Publishing events...");
        publisher.publish(events.asFlux()).subscribe(unused -> log.debug("Completed."),
                                            error -> log.error("Error sending events: " + error),
                                            () -> log.debug("Completed sending events."));

        return new SendDataOutputStream((bytes -> {
            final EventData event = new EventData(bytes);
            // event.getProperties().put(EVENT_NUMBER, String.valueOf(i));
            Sinks.EmitResult result = events.tryEmitNext(event);
            log.debug("SEND: {}", result.toString());

            //sink.next(event);
        })) {
            @Override
            public void close() throws IOException {
                super.close();
                Sinks.EmitResult result = events.tryEmitComplete();
                log.debug("END: {}", result.toString());
                publisher.close();
            }
        };
    }

    @VisibleForTesting
    EventHubProducerAsyncClient getClient(String connectionString) {
        return new EventHubClientBuilder()
                .connectionString(connectionString)
                .buildAsyncProducerClient();
    }

    /**
     * Aggregates events into batches based on the given {@link CreateBatchOptions} and sends the batches to the Event
     * Hubs service when either:
     *
     * <ul>
     *     <li>The batch has reached its max size and no more events can be added to it.</li>
     *     <li>The timeout window has elapsed.</li>
     * </ul>
     */
    private static class CustomPublisher implements AutoCloseable {
        private final Logger logger = LoggerFactory.getLogger(CustomPublisher.class);
        private final AtomicBoolean isDisposed = new AtomicBoolean();
        private final AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>();
        private final EventHubProducerAsyncClient producer;
        private final CreateBatchOptions batchOptions;
        private final Duration windowDuration;

        /**
         * Creates a new instance of {@link CustomPublisher}.
         *
         * @param producer Event Hub producer.
         * @param windowDuration Intervals to check for an available {@link EventDataBatch} to send.
         * @param batchOptions Options to use when creating the {@link EventDataBatch}. If {@code null}, the default
         * batch options are used.
         */
        CustomPublisher(EventHubProducerAsyncClient producer, Duration windowDuration, CreateBatchOptions batchOptions) {
            this.producer = producer;

            this.batchOptions = batchOptions != null ? batchOptions : new CreateBatchOptions();
            this.windowDuration = Objects.requireNonNull(windowDuration, "'windowDuration' cannot be null.");
        }

        /**
         * Subscribes to a stream of events and publishes event batches when:
         *
         * <ul>
         * <li>The {@link EventDataBatch} is full.</li>
         * <li>Timeout window has elapsed and there is a batch.</li>
         * </ul>
         *
         * @param events Events to publish to the service.
         *
         * @return Mono that completes when all the events have been published.
         */
        Mono<Void> publish(Flux<EventData> events) {
            final Flux<EventDataBatch> fullBatchFlux = Flux.<EventDataBatch>create(sink -> {
                events.subscribe(event -> {
                    // For each event, tries to add it to the current batch. If the current batch is full, then we emit
                    // that. Otherwise an empty Mono is emitted.
                    // Blocking at the very end of the method to ensure that the event is added to a batch before
                    // requesting another event.
                    getOrCreateBatch()
                            .flatMap(batch -> batch.tryAdd(event) ? Mono.empty() : Mono.just(batch))
                            .flatMap(fullBatch -> {
                                sink.next(fullBatch);

                                return createBatch().map(newBatch -> {
                                    if (!newBatch.tryAdd(event)) {
                                        sink.error(new IllegalArgumentException(String.format(
                                                "Event is too large for an empty batch. Max size: %s. Event: %s",
                                                newBatch.getMaxSizeInBytes(), event.getBodyAsString())));
                                    }
                                    return newBatch;
                                });
                            }).block();
                }, error -> {
                    sink.error(new RuntimeException("Error fetching next event.", error));
                }, () -> {
                    final EventDataBatch lastBatch = currentBatch.getAndSet(null);
                    if (lastBatch != null) {
                        sink.next(lastBatch);
                    }

                    sink.complete();
                });
            }).publish().autoConnect();

            // Periodically checks to see if there is an available, not full, batch to send. If there is, it sends that
            // batch. It keeps checking for an available batch until the last event in `events` has been published.
            final Flux<EventDataBatch> emitAtIntervals = Flux.interval(windowDuration)
                    .takeUntilOther(events.then())
                    .flatMap(v -> {
                        final EventDataBatch batch = currentBatch.getAndSet(null);
                        logger.debug("Interval check. Has items? {}", batch != null && batch.getCount() > 0);
                        return batch != null ? Mono.just(batch) : Mono.empty();
                    });

            // Merge the two fluxes together so the results
            return Flux.merge(fullBatchFlux, emitAtIntervals)
                    .flatMap(batchToSend -> {
                        logger.debug("Sending batch with {} events. Size: {} bytes. Event numbers in batch [{}]",
                                    batchToSend.getCount(), batchToSend.getSizeInBytes(), batchToSend.getCount());

                        return producer.send(batchToSend);
                    })
                    .then();
        }

        private Mono<EventDataBatch> getOrCreateBatch() {
            final EventDataBatch current = currentBatch.get();
            return current != null
                    ? Mono.just(current)
                    : createBatch();
        }

        private Mono<EventDataBatch> createBatch() {
            return producer.createBatch(batchOptions).map(batch -> {
                currentBatch.set(batch);
                return batch;
            });
        }

        public void close() {
            if (isDisposed.getAndSet(true)) {
                return;
            }

            final EventDataBatch batch = currentBatch.getAndSet(null);
            if (batch != null) {
                producer.send(batch).block(Duration.ofSeconds(30));
            }

            producer.close();
        }
    }

}

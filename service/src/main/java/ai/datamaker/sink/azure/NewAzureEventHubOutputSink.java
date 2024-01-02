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
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Slf4j
public class NewAzureEventHubOutputSink implements DataOutputSink {
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

    public static final PropertyConfig AZURE_EVENT_HUB_PROPERTIES_NAME = new PropertyConfig("azure.eventhub.sink.properties.name",
                                                                                            "Properties names",
                                                                                            PropertyConfig.ValueType.LIST,
                                                                                            Collections.emptyList(),
                                                                                            Collections.emptyList());

    public static final PropertyConfig AZURE_EVENT_HUB_PROPERTIES_VALUE = new PropertyConfig("azure.eventhub.sink.properties.values",
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

        // Limit the size of the batches to 1024 bytes instead of using the default max size.
        final CreateBatchOptions batchOptions = new CreateBatchOptions()
                .setMaximumSizeInBytes((Integer) config.getConfigProperty(AZURE_EVENT_HUB_BATCH_SIZE))
                .setPartitionKey((String) config.getConfigProperty(AZURE_EVENT_HUB_PARTITION_KEY))
                .setPartitionId((String) config.getConfigProperty(AZURE_EVENT_HUB_PARTITION_ID));

        final EventHubProducerClient producer = getClient(config);

        // create a batch
        final EventDataBatch[] eventDataBatch = new EventDataBatch[]{producer.createBatch(batchOptions)};

        return new SendDataOutputStream((bytes -> {
            final EventData eventData = new EventData(bytes);
            if (!eventDataBatch[0].tryAdd(eventData)) {
                // if the batch is full, send it and then create a new batch
                producer.send(eventDataBatch[0]);
                eventDataBatch[0] = producer.createBatch();

                // Try to add that event that couldn't fit before.
                if (!eventDataBatch[0].tryAdd(eventData)) {
                    throw new IllegalArgumentException("Event is too large for an empty batch. Max size: " + eventDataBatch[0].getMaxSizeInBytes());
                }
        }})) {
            @Override
            public void close() throws IOException {
                super.close();
                if (eventDataBatch[0].getCount() > 0) {
                    producer.send(eventDataBatch[0]);
                }
                producer.close();
            }
        };
    }

    @VisibleForTesting
    EventHubProducerClient getClient(JobConfig config) {
        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        // String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};" + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";
        String connectionString = (String) config.getConfigProperty(AZURE_EVENT_HUB_CONNECTION_STRING);

        return new EventHubClientBuilder().connectionString(connectionString).buildProducerClient();
    }

    public static void main(String[] args) throws Exception {
        NewAzureEventHubOutputSink sink = new NewAzureEventHubOutputSink();
        JobConfig config = new JobConfig();
        config.put(AZURE_EVENT_HUB_CONNECTION_STRING.getKey(), "Endpoint=sb://breakpoints.servicebus.windows.net/;SharedAccessKeyName=test;SharedAccessKey=ccNApVI4Th2P/mUhYcyN520+7d201aeHPkJ0iEfvdj8=;EntityPath=test");
        try (OutputStream out = sink.getOutputStream(config);) {
            out.write("hello world".getBytes(StandardCharsets.UTF_8));
            out.flush();
        }
    }

}

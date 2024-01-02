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
import ai.datamaker.sink.azure.AzureEventHubOutputSink;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AzureEventHubOutputSinkTest {

    private final AzureEventHubOutputSink sink = Mockito.spy(AzureEventHubOutputSink.class);

    @Test
    void accept() {
        Arrays.stream(FormatType.values()).forEach(ft -> assertTrue(sink.accept(ft)));
    }

    @Test
    void getOutputStream() throws Exception {
        EventHubProducerAsyncClient client = Mockito.mock(EventHubProducerAsyncClient.class);
        doReturn(client).when(sink).getClient(anyString());
        Mono<EventDataBatch> eventDataBatchMono = Mono.just(Mockito.mock(EventDataBatch.class));
        when(client.createBatch(any(CreateBatchOptions.class))).thenReturn(eventDataBatchMono);

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
        config.put(AzureEventHubOutputSink.AZURE_EVENT_HUB_CONNECTION_STRING, "'eventhub'");
        config.put(AzureEventHubOutputSink.AZURE_EVENT_HUB_BATCH_SIZE, 5);
//        config.remove(AzureEventHubOutputSink.AZURE_EVENT_HUB_PARTITION_KEY.getKey());

        try (OutputStream output = sink.getOutputStream(config);) {

            output.write("hello".getBytes(StandardCharsets.UTF_8));
            output.flush();
        }

        verify(client, times(1)).send(any(EventDataBatch.class));
    }

    @Test
    void getConfigProperties() {
        assertEquals(5, sink.getConfigProperties().size());
    }
}
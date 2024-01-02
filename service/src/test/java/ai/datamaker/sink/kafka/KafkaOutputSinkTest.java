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

package ai.datamaker.sink.kafka;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.sink.kafka.KafkaOutputSink;
import ai.datamaker.service.KafkaProducerService;
import org.apache.kafka.clients.producer.Callback;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class KafkaOutputSinkTest {

    private KafkaOutputSink sink = Mockito.spy(KafkaOutputSink.class);

    @Test
    void accept() {
        assertTrue(sink.accept(FormatType.JSON));
        assertTrue(sink.accept(FormatType.AVRO));
    }

    @Test
    void getOutputStream() throws Exception {
        KafkaProducerService<Long, byte[]> kafkaProducerService = Mockito.mock(KafkaProducerService.class);
        doReturn(kafkaProducerService).when(sink).getKafkaProducerService(any());

        JobConfig config = new JobConfig();
        config.put(KafkaOutputSink.KAFKA_BOOTSTRAP_SERVERS.getKey(), "localhost:8002");
        config.put(KafkaOutputSink.KAFKA_TOPIC_NAME.getKey(), "test-produce");
        config.put(KafkaOutputSink.KAFKA_CLIENT_ID.getKey(), "test-client");
        config.put(KafkaOutputSink.KAFKA_SYNC_SEND.getKey(), true);

        OutputStream outputStream = sink.getOutputStream(config);
        outputStream.write(65);
        outputStream.flush();

        verify(kafkaProducerService, times(1)).send(anyString(), anyInt(), anyLong(), eq(new byte[]{65}), any(Callback.class), anyMap());
    }

    @Test
    void getOutputStream_noKafkaBrokers() throws Exception {
        KafkaProducerService<Long, byte[]> kafkaProducerService = Mockito.mock(KafkaProducerService.class);
        doReturn(kafkaProducerService).when(sink).getKafkaProducerService(any());

        JobConfig config = new JobConfig();
        config.put(KafkaOutputSink.KAFKA_BOOTSTRAP_SERVERS.getKey(), "localhost:8002");
        config.put(KafkaOutputSink.KAFKA_TOPIC_NAME.getKey(), "test-produce");
        config.put(KafkaOutputSink.KAFKA_CLIENT_ID.getKey(), "test-client");
        config.put(KafkaOutputSink.KAFKA_SYNC_SEND.getKey(), true);

        OutputStream outputStream = sink.getOutputStream(config);
        outputStream.write(65);
        outputStream.flush();

        verify(kafkaProducerService, times(1)).send(anyString(), anyInt(), anyLong(), eq(new byte[]{65}), any(Callback.class), anyMap());
    }


    @Test
    void getOutputStream_invalid() {
        // No Kafka brokers
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            JobConfig config = new JobConfig();
            config.put(KafkaOutputSink.KAFKA_TOPIC_NAME.getKey(), "test-produce");
            config.put(KafkaOutputSink.KAFKA_CLIENT_ID.getKey(), "test-client");

            sink.getOutputStream(config);
        }, "No kafka brokers");

        // No topic name
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            JobConfig config = new JobConfig();
            config.put(KafkaOutputSink.KAFKA_BOOTSTRAP_SERVERS.getKey(), "localhost:8002");
            config.put(KafkaOutputSink.KAFKA_CLIENT_ID.getKey(), "test-client");

            sink.getOutputStream(config);
        }, "No topic name");
    }
}
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

package ai.datamaker.integration.sink;

import ai.datamaker.model.JobConfig;
import ai.datamaker.sink.kafka.KafkaOutputSink;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.OutputStream;
import java.util.Map;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
//@SpringBootTest
@EmbeddedKafka//(brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092", "log.dir=logs"})
class KafkaOutputSinkIntegrationTest {

    private KafkaOutputSink sink = new KafkaOutputSink();

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    @Timeout(30)
    void getOutputStream() throws Exception {
        JobConfig config = new JobConfig();

        config.put(KafkaOutputSink.KAFKA_BOOTSTRAP_SERVERS, embeddedKafkaBroker.getBrokersAsString());
        config.put(KafkaOutputSink.KAFKA_TOPIC_NAME, "test-produce");
        config.put(KafkaOutputSink.KAFKA_CLIENT_ID, "test-client");
        config.put(KafkaOutputSink.KAFKA_SYNC_SEND, true);

        try (OutputStream outputStream = sink.getOutputStream(config)) {
            outputStream.write("{\"name\":\"value\"}".getBytes());
            outputStream.flush();

            Map<String, Object> conf = KafkaTestUtils.consumerProps("consumer", "true", embeddedKafkaBroker);
            conf.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            conf.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
            conf.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

            Consumer<Long, byte[]> consumer = new KafkaConsumer<>(conf);
            consumer.subscribe(singleton("test-produce"));
            //consumer.poll(Duration.of(10,
            // ChronoUnit.SECONDS));

            ConsumerRecord<Long, byte[]> singleRecord = KafkaTestUtils.getSingleRecord(consumer, "test-produce", 10 * 1000);
            assertThat(singleRecord).isNotNull();
            assertThat(singleRecord.key()).isEqualTo(1);
            assertThat(singleRecord.value()).isEqualTo("{\"name\":\"value\"}".getBytes());
            consumer.close();
        }
    }

}
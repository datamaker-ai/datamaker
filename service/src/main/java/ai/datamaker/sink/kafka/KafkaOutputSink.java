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
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.PropertyConfig.ValueType;
import ai.datamaker.sink.DataOutputSink;
import ai.datamaker.service.KafkaProducerService;
import ai.datamaker.sink.SslCommon;
import ai.datamaker.utils.stream.SendDataOutputStream;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Send data to a Kafka topic.
 */
@Slf4j
public class KafkaOutputSink implements DataOutputSink, SslCommon {

    public static final PropertyConfig KAFKA_SECURITY_PROTOCOLS =
            new PropertyConfig("kafka.sink.security.protocols",
                               "Security protocols",
                               ValueType.STRING,
                               "PLAINTEXT",
                               Lists.newArrayList("PLAINTEXT", "SSL", "SASL_PLAINTEXT", "SASL_SSL"));

    public static final PropertyConfig KAFKA_PRINCIPAL =
            new PropertyConfig("kafka.sink.principal",
                               "Kerberos principal",
                               ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig KAFKA_KEYTAB =
            new PropertyConfig("kafka.sink.keytab",
                               "Kerberos keytab",
                               ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig KAFKA_SYNC_SEND =
            new PropertyConfig("kafka.sink.synchronous.send",
                                "Kafka synchronous send",
                                ValueType.BOOLEAN,
                                false,
                                Collections.emptyList());

    public static final PropertyConfig KAFKA_TOPIC_NAME =
            new PropertyConfig("kafka.sink.topic.name",
                                "Kafka topic name",
                                ValueType.STRING,
                                "",
                                Collections.emptyList());

    public static final PropertyConfig KAFKA_BOOTSTRAP_SERVERS =
            new PropertyConfig("kafka.sink.bootstrap.servers",
                                "Kafka bootstrap servers (<code>host1:port1,host2:port2,...</code>)",
                                ValueType.STRING,
                                "",
                                Collections.emptyList());

    public static final PropertyConfig KAFKA_CLIENT_ID =
        new PropertyConfig("kafka.sink.client.id",
                                "Kafka client id",
                                ValueType.STRING,
                                "",
                                Collections.emptyList());

    public static final PropertyConfig KAFKA_HEADERS_NAME =
            new PropertyConfig("kafka.sink.headers.name",
                               "Kafka header names",
                               ValueType.LIST,
                               Collections.emptyList(),
                               Collections.emptyList());

    public static final PropertyConfig KAFKA_HEADERS_VALUE =
            new PropertyConfig("kafka.sink.headers.values",
                               "Kafka header values (support expression)",
                               ValueType.LIST,
                               Collections.emptyList(),
                               Collections.emptyList());

    @VisibleForTesting
    <K extends Serializable, V extends Serializable> KafkaProducerService<K, V> getKafkaProducerService(JobConfig config) {
        final boolean syncSend = (boolean) config.getConfigProperty(KAFKA_SYNC_SEND);

        return new KafkaProducerService<>(createProducerConfig(config), syncSend);
    }

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    public static Properties createProducerConfig(JobConfig config) {

        String bootstrapServers = (String) config.getConfigProperty(KAFKA_BOOTSTRAP_SERVERS);
        Assert.isTrue(StringUtils.isNotBlank(bootstrapServers), "Kafka brokers cannot be null");

        String clientId = (String) config.getConfigProperty(KAFKA_CLIENT_ID);
        Assert.notNull(clientId, "Kafka client id cannot be null");

        String securityProtocol = (String) config.getConfigProperty(KAFKA_SECURITY_PROTOCOLS);

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, securityProtocol);

        String serviceName = "kafka";
        String principal = (String) config.getConfigProperty(KAFKA_PRINCIPAL);
        String keytab = (String) config.getConfigProperty(KAFKA_KEYTAB);
        props.put(SaslConfigs.SASL_JAAS_CONFIG, "com.sun.security.auth.module.Krb5LoginModule required "
                + "useTicketCache=false "
                + "renewTicket=true "
                + "serviceName=\"" + serviceName + "\" "
                + "useKeyTab=true "
                + "keyTab=\"" + keytab + "\" "
                + "principal=\"" + principal + "\";");
        return props;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return addDefaultProperties(Lists.newArrayList(
            KAFKA_SYNC_SEND,
            KAFKA_TOPIC_NAME,
            KAFKA_BOOTSTRAP_SERVERS,
            KAFKA_CLIENT_ID,
            KAFKA_SECURITY_PROTOCOLS,
            KAFKA_KEYTAB,
            KAFKA_PRINCIPAL,
            KAFKA_HEADERS_NAME,
            KAFKA_HEADERS_VALUE
        ));
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {

        String topicName = (String) config.getConfigProperty(KAFKA_TOPIC_NAME);
        Assert.isTrue(StringUtils.isNotBlank(topicName), "Kafka topic cannot be null");

        AtomicLong kafkaKey = new AtomicLong(1);
        KafkaProducerService<Long, byte[]> producer = getKafkaProducerService(config);

        final Map<String, String> headers = Maps.newHashMap();
        if (config.containsKey(KAFKA_HEADERS_NAME.getKey())) {
            List<String> headerNames = (List<String>) config.getConfigProperty(KAFKA_HEADERS_NAME);
            List<String> headerValues = (List<String>) config.getConfigProperty(KAFKA_HEADERS_VALUE);
            Assert.isTrue(headerNames.size() == headerValues.size(), "Number of header names and values should match");
            for (int i=0; i<headerNames.size(); i++) {
                headers.put(headerNames.get(i), String.valueOf(parseExpression(headerValues.get(i), config)));
            }
        }

        return new SendDataOutputStream(new Consumer<>() {
            @Override
            public void accept(byte[] bytes) {
                producer.send(topicName, -1, kafkaKey.getAndIncrement(), bytes, getCallback(), headers);
            }

            private Callback getCallback() {
                return (recordMetadata, e) -> {
                    if (e != null) {
                        producer.close();
                        throw new IllegalStateException(e);
                    } else {
                        log.trace("sent message to topic:{} partition:{}  offset:{}", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
                    }
                };
            }
        }) {
            @Override
            public void close() throws IOException {
                super.close();
                producer.close();
            }
        };
    }
}

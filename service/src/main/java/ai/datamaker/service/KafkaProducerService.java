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

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.datamaker.service;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

public class KafkaProducerService<K extends Serializable, V extends Serializable> {

    private final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    private KafkaProducer<K, V> producer;
    private boolean syncSend;
    private volatile boolean shutDown = false;

    public KafkaProducerService(Properties producerConfig) {
        this(producerConfig, true);
    }

    public KafkaProducerService(Properties producerConfig, boolean syncSend) {
        this.syncSend = syncSend;
        this.producer = new KafkaProducer<>(producerConfig);
        logger.debug("Started Producer.  sync  : {}", syncSend);
    }

    public void send(String topic, V v) {
        send(topic, -1, null, v, new DummyCallback(), Collections.emptyMap());
    }

    public void send(String topic, K k, V v) {
        send(topic, -1, k, v, new DummyCallback(), Collections.emptyMap());
    }

    public void send(String topic, int partition, V v) {
        send(topic, partition, null, v, new DummyCallback(), Collections.emptyMap());
    }

    public void send(String topic, int partition, K k, V v) {
        send(topic, partition, k, v, new DummyCallback(), Collections.emptyMap());
    }

    public void send(String topic, int partition, K key, V value, Callback callback, Map<String, String> headers) {
//        if (shutDown) {
//            throw new RuntimeException("Producer is closed.");
//        }

        if (!shutDown) {
            try {
                ProducerRecord<K, V> record;
                if (partition < 0) {
                    record = new ProducerRecord<>(topic, key, value);
                }
                else {
                    record = new ProducerRecord<>(topic, partition, key, value);
                }
                headers.forEach((key1, value1) -> record.headers().add(key1, value1.getBytes()));

                Future<RecordMetadata> future = producer.send(record, callback);
                if (syncSend) {
                    future.get();
                }
            } catch (Exception e) {
                logger.error("Error while producing event for topic : {}", topic, e);
                throw new IllegalStateException(e);
            }
        }
    }

    public void close() {
        shutDown = true;
        try {
            producer.close(Duration.ofSeconds(10));
        } catch (Exception e) {
            logger.error("Exception occurred while stopping the producer", e);
        }
    }

    private class DummyCallback implements Callback {
        @Override
        public void onCompletion(RecordMetadata recordMetadata, Exception e) {
            if (e != null) {
                logger.error("Error while producing message to topic : {}", recordMetadata != null ? recordMetadata.topic() : "", e);
                throw new IllegalStateException(e);
            } else
                logger.trace("sent message to topic:{} partition:{}  offset:{}", recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
        }
    }
}
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

package ai.datamaker.e2e.sink.base;

import ai.datamaker.model.JobConfig;
import ai.datamaker.sink.base.JmsOutputSink;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;

@Disabled
class JmsOutputSinkTest {

    private JmsOutputSink sink = new JmsOutputSink();

    @Test
    void accept() {
    }

    @Test
    void getOutputStream() throws Exception {
        JobConfig config = new JobConfig();
        config.put(JmsOutputSink.JMS_DESTINATION_NAME.getKey(), "messages");
        // config.put(JmsOutputSink.JMS_DESTINATION_TYPE.getKey(), "ZIP");

        config.put(JmsOutputSink.JMS_BROKER_URI.getKey(), "amqp://guest:guest@localhost:5672");
        config.put(JmsOutputSink.JMS_CONNECTION_FACTORY_CLASSNAME.getKey(), "com.rabbitmq.jms.admin.RMQConnectionFactory");

        try (OutputStream outputStream = sink.getOutputStream(config)) {
            outputStream.write("hello, world".getBytes());
        }
    }
}
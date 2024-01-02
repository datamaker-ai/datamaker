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

package ai.datamaker.e2e.sink.google;

import ai.datamaker.model.JobConfig;
import ai.datamaker.sink.google.GooglePubSubOutputSink;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

@Disabled
class GooglePubSubOutputSinkTest {

    private GooglePubSubOutputSink sink = new GooglePubSubOutputSink();

    @Test
    void accept() {
    }

    @Test
    void getOutputStream() throws Exception {
        JobConfig config = new JobConfig();
        config.put(GooglePubSubOutputSink.GOOGLE_PUBSUB_TOPIC_NAME.getKey(), "datamaker");
        config.put(GooglePubSubOutputSink.GOOGLE_CLOUD_PROJECT_ID.getKey(), "api-project-1234");

        config.put(GooglePubSubOutputSink.GOOGLE_CLOUD_JSON_KEY.getKey(), "{\n" + "  \"type\": \"service_account\",\n" + "  \"project_id\": \"api-project-1234\",\n" + "}\n");
        try (OutputStream stream = sink.getOutputStream(config)) {
            for (int i=0; i<100; i++) {
                stream.write(("{\"name\": \"Hello world! " + System.currentTimeMillis() + "\"}").getBytes());
                stream.flush();
            }
        }
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException ignored) {
        }
    }

    @Test
    void getConfigProperties() {
    }
}
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

package ai.datamaker.e2e.sink.azure;

import ai.datamaker.model.JobConfig;
import ai.datamaker.sink.azure.AzureEventHubOutputSink;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

@Disabled
class AzureEventHubOutputSinkTest {

    private AzureEventHubOutputSink sink = new AzureEventHubOutputSink();

    @Test
    void getConfigProperties() {
    }

    @Test
    void accept() {
    }

    @Test
    void getOutputStream() throws Exception {
        String connectionString = "Endpoint=sb://datamaker.servicebus.windows.net/;SharedAccessKeyName=ReadEvents;SharedAccessKey=17t2Xl4cufR32+OOMXH0CeLGAarE/JPFQBjor5471Tg=;EntityPath=datamaker-events";

        JobConfig config = new JobConfig();
        config.put(AzureEventHubOutputSink.AZURE_EVENT_HUB_CONNECTION_STRING.getKey(), connectionString);
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
}
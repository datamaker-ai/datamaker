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
import com.google.common.collect.Lists;

import java.io.OutputStream;
import java.util.List;

public class AzureStorageQueueOutputSink implements DataOutputSink, AzureCommon {

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {
        // Build Queue Client using SAS Token
//        String queueServiceURL = String.format("https://%s.queue.core.windows.net/%s", ACCOUNT_NAME, SAS_TOKEN);
//        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL).buildClient();
//
//        // Create a queue client
//        QueueClient queueClient = queueServiceClient.createQueue(generateRandomName("enqueue", 16));
//        for (int i = 0; i < 3; i++) {
//            queueClient.sendMessage("Hello World");
//        }

        return null;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList();
    }
}

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
import ai.datamaker.sink.azure.AzureDatalakeOutputSink;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;

@Disabled
class AzureDatalakeOutputSinkTest {

    private final AzureDatalakeOutputSink sink = new AzureDatalakeOutputSink();

    @Test
    void accept() {
    }

    @Test
    void getOutputStream() throws Exception {
        JobConfig config = new JobConfig();
        config.put(AzureDatalakeOutputSink.AZURE_FILENAME, "\"test\"");
        config.put(AzureDatalakeOutputSink.AZURE_FILESYSTEM_NAME, "datalakev2");
        config.put(AzureDatalakeOutputSink.AZURE_STORAGE_ACCOUNT_NAME, "testdatamaker");
        config.put(AzureDatalakeOutputSink.AZURE_STORAGE_SAS_TOKEN, "?sv=2019-10-10&ss=bfqt&srt=co&sp=rwdlacupx&se=2020-07-04T01:30:06Z&st=2020-07-03T17:30:06Z&spr=https&sig=FZm0CXr1Frtms5i0wIhdflWQuZeZvH21YFkzFCfzS4Y%3D");
        try (OutputStream outputStream = sink.getOutputStream(config)) {
            outputStream.write("hell world".getBytes());
        }
    }

    @Test
    void getConfigProperties() {
    }
}
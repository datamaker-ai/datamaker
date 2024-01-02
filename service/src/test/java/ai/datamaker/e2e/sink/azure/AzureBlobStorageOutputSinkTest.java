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

import ai.datamaker.sink.azure.AzureBlobStorageOutputSink;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;

@Disabled
class AzureBlobStorageOutputSinkTest {

    private AzureBlobStorageOutputSink sink = new AzureBlobStorageOutputSink();

    @Test
    void accept() {
    }

    @Test
    void getOutputStream() throws Exception {
        try (OutputStream outputStream = sink.getOutputStream()) {
            outputStream.write("allo".getBytes());
        }
    }

    @Test
    void getConfigProperties() {
    }
}
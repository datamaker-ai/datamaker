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

package ai.datamaker.sink;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

@Slf4j
@Deprecated
public class RunForeverSink implements DataOutputSink {

    private volatile boolean shouldStop = false;

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                while (!shouldStop) {
                    try {
                        log.trace("Running...");
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        shouldStop = true;
                        break;
                    }
                }
            }
        };
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Collections.emptyList();
    }
}

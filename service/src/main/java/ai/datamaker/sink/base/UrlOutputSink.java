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

package ai.datamaker.sink.base;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.DataOutputSink;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * Generates a {@link java.net.URL} to download the file.
 */
@Deprecated
public class UrlOutputSink implements DataOutputSink {

  @Override
  public boolean accept(FormatType type) {
    return false;
  }

  @Override
  public OutputStream getOutputStream() throws Exception {
    return null;
  }

  @Override
  public OutputStream getOutputStream(JobConfig config) throws Exception {
    return null;
  }

  @Override
  public List<PropertyConfig> getConfigProperties() {
    return Collections.emptyList();
  }
}

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

package ai.datamaker.utils.stream;

import java.io.IOException;
import java.io.OutputStream;

public final class TeeOutputStream extends OutputStream {

  private final OutputStream out;
  private final OutputStream tee;

  public TeeOutputStream(OutputStream out, OutputStream tee) {
    if (out == null || tee == null) {
      throw new NullPointerException();
    }
    this.out = out;
    this.tee = tee;
  }

  @Override
  public void write(int b) throws IOException {
    out.write(b);
    tee.write(b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    out.write(b);
    tee.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    out.write(b, off, len);
    tee.write(b, off, len);
  }

  @Override
  public void flush() throws IOException {
    out.flush();
    tee.flush();
  }

  @Override
  public void close() throws IOException {
    try {
      out.close();
    } finally {
      tee.close();
    }
  }
}

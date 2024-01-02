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

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A class for keeping track of wrapped output streams and closing them when this stream is closed.
 * This is required because GPG wrapping of streams does not propagate the close.
 */
public class ClosingWrapperOutputStream extends OutputStream {
    private final OutputStream[] outputStreams;
    private final OutputStream firstStream;

    /**
     * Creates an output stream that writes to the first {@link OutputStream} and closes all of the {@link OutputStream}s
     * when close() is called
     *
     * @param outputStreams list of {@link OutputStream}s where the first one is written to and the rest are tracked
     *                      for closing.
     */
    public ClosingWrapperOutputStream(OutputStream... outputStreams) {
        Preconditions.checkArgument(outputStreams.length >= 1);

        this.outputStreams = outputStreams;
        this.firstStream = outputStreams[0];
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        this.firstStream.write(bytes);
    }

    @Override
    public void write(byte[] bytes, int offset, int length) throws IOException {
        this.firstStream.write(bytes, offset, length);
    }

    @Override
    public void write(int b) throws IOException {
        this.firstStream.write(b);
    }

    public void flush() throws IOException {
        for (OutputStream os : this.outputStreams) {
            os.flush();
        }
    }

    public void close() throws IOException {
        for (OutputStream os : this.outputStreams) {
            os.close();
        }
    }
}
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * Accumulate data in a {@link ByteArrayOutputStream} and call the consumer at close or flush time.
 */
public class SendDataOutputStream extends OutputStream {

    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private final Consumer<byte[]> consumer;
    private boolean closed = false;
    private final boolean respectFlush;

    public SendDataOutputStream(Consumer<byte[]> consumer) {
        this.consumer = consumer;
        this.respectFlush = true;
    }

    public SendDataOutputStream(Consumer<byte[]> consumer, boolean respectFlush) {
        this.consumer = consumer;
        this.respectFlush = respectFlush;
    }

    @Override
    public void write(int b) throws IOException {
        baos.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        baos.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        baos.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        if (!closed && respectFlush) {
            sendData();
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed && baos.size() > 0) {
            sendData();
        }
        closed = true;
    }

    private void sendData() {
        consumer.accept(baos.toByteArray());
        baos.reset();
    }
}

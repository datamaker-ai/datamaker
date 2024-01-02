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

/**
 * OutputFile.java
 * <p>
 * Copyright May 2018 Tideworks Technology
 * Author: Roger D. Voss
 * MIT License
 */
package ai.datamaker.utils.stream;

import org.apache.parquet.io.PositionOutputStream;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public final class OutputFile {
    private static final int IO_BUF_SIZE = 16 * 1024;

    public static org.apache.parquet.io.OutputFile nioPathToOutputFile(Path file) {
        //noinspection ConstantConditions
        assert file != null;
        return new org.apache.parquet.io.OutputFile() {
            @Override
            public PositionOutputStream create(long blockSizeHint) throws IOException {
                return makePositionOutputStream(file, IO_BUF_SIZE, false);
            }

            @Override
            public PositionOutputStream createOrOverwrite(long blockSizeHint) throws IOException {
                return makePositionOutputStream(file, IO_BUF_SIZE, true);
            }

            @Override
            public boolean supportsBlockSize() {
                return false;
            }

            @Override
            public long defaultBlockSize() {
                return 0;
            }
        };
    }

    private static PositionOutputStream makePositionOutputStream(Path file, int ioBufSize, boolean trunc)
            throws IOException {
        final OutputStream output = new BufferedOutputStream(
                Files.newOutputStream(file, CREATE, trunc ? TRUNCATE_EXISTING : APPEND), ioBufSize);

        return new PositionOutputStream() {
            private long position = 0;

            @Override
            public void write(int b) throws IOException {
                output.write(b);
                position++;
            }

            @Override
            public void write(byte[] b) throws IOException {
                output.write(b);
                position += b.length;
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                output.write(b, off, len);
                position += len;
            }

            @Override
            public void flush() throws IOException {
                output.flush();
            }

            @Override
            public void close() throws IOException {
                output.close();
            }

            @Override
            public long getPos() throws IOException {
                return position;
            }
        };
    }
}

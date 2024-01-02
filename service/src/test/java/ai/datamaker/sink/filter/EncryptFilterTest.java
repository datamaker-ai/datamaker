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

package ai.datamaker.sink.filter;

import ai.datamaker.model.JobConfig;
import ai.datamaker.sink.filter.CompressFilter;
import ai.datamaker.sink.filter.EncryptFilter;
import ai.datamaker.utils.crypto.PgpHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Disabled
class EncryptFilterTest {

    @Test
    void getEncryptedStream() throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream("/tmp/file.aes");

        OutputStream encryptedStream = EncryptFilter.getEncryptedStream(fileOutputStream);
        encryptedStream.write("hello world".getBytes());

        encryptedStream.flush();
        encryptedStream.close();
    }

    @Test
    void getKey() throws Exception {
        System.out.println(EncryptFilter.generateSecretKey("AES"));
    }

    @Test
    void encrypt() throws Exception {
        FileOutputStream fileOutputStream = new FileOutputStream("/tmp/file.pgp");
        OutputStream outputStream = EncryptFilter.getPgpEncryptedStream(fileOutputStream, "/tmp/pub.dat", false);
        outputStream.write("hello, world!".getBytes());
        outputStream.close();
    }

    @Test
    void decrypt() throws Exception {
        FileInputStream fileInputStream = new FileInputStream("/tmp/file.pgp");
        FileInputStream keyIn = new FileInputStream("/tmp/secret.dat");
        PgpHelper.getInstance().decryptFile(fileInputStream, System.out, keyIn, "******".toCharArray());
    }

    @Test
    void encryptZip() throws Exception {
        JobConfig config = new JobConfig();
        config.put(CompressFilter.COMPRESSION_FORMAT.getKey(), "ZIP");
        config.put(EncryptFilter.ENCRYPTION_ALGORITHM.getKey(), "PGP");
        config.put(EncryptFilter.PGP_PUBLIC_KEY_PATH.getKey(), "/tmp/pub.dat");

        FileOutputStream fileOutputStream = new FileOutputStream("/tmp/file.zip");
        OutputStream compressedStream = CompressFilter.getCompressedStream(config, fileOutputStream);

        OutputStream encryptStream = new OutputStream() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStream stream = EncryptFilter.getPgpEncryptedStream(baos, "/tmp/pub.dat", false);
            @Override
            public void write(int b) throws IOException {
                stream.write(b);
            }

            @Override
            public void flush() throws IOException {
            }

            @Override
            public void close() throws IOException {
                stream.flush();
                stream.close();
                compressedStream.write(baos.toByteArray());
                compressedStream.close();
            }
        };

        encryptStream.write("hello, world!".getBytes());
        encryptStream.flush();
        encryptStream.close();
    }
}
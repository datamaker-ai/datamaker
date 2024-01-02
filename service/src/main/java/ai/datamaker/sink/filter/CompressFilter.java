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
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.utils.stream.SendDataOutputStream;
import com.google.common.collect.Lists;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.deflate.DeflateCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.xerial.snappy.SnappyOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.apache.commons.compress.archivers.tar.TarConstants.LF_NORMAL;

/**
 * Zip, Bzip, Gzip content on the fly.
 */
public class CompressFilter {

    public static final PropertyConfig UNCOMPRESSED_FILENAME
            = new PropertyConfig(
            "compress.filter.filename",
            "Original filename",
            PropertyConfig.ValueType.EXPRESSION,
            "#dataset.name + \"-\" + T(java.lang.System).currentTimeMillis() + \".\" + #dataJob.generator.dataType.name().toLowerCase()",
            Collections.emptyList());

    public static final PropertyConfig COMPRESSION_FORMAT
            = new PropertyConfig("compress.filter.format",
                                 "Compression format",
                                 PropertyConfig.ValueType.STRING,
                                 "NONE",
                                 Lists.newArrayList("BZIP2",
                                                    "DEFLATE",
                                                    "GZIP",
                                                    "JAR",
                                                    "NONE",
                                                    "SNAPPY",
                                                    "TAR",
                                                    "TAR_BZIP2",
                                                    "TAR_GZIP",
                                                    "TGZ",
                                                    "ZIP"));

    public static OutputStream getCompressedStream(JobConfig config, OutputStream outputStream) throws IOException {
        String format = (String) config.getConfigProperty(CompressFilter.COMPRESSION_FORMAT);
        String filename = (String) config.getConfigProperty(CompressFilter.UNCOMPRESSED_FILENAME);

        switch (format) {
            case "BZIP":
            case "BZIP2":
                return new BZip2CompressorOutputStream(outputStream);
            case "DEFLATE":
                return new DeflateCompressorOutputStream(outputStream);
            case "GZIP":
                GzipParameters gzipParameters = new GzipParameters();
                gzipParameters.setFilename(filename);
                return new GzipCompressorOutputStream(outputStream, gzipParameters);
            case "JAR":
                return new JarArchiveOutputStream(outputStream);
            case "NONE":
                return outputStream;
            case "SNAPPY":
                return new SnappyOutputStream(outputStream);
            case "TAR":
                return new SendDataOutputStream(bytes -> {
                    TarArchiveOutputStream taos = new TarArchiveOutputStream(outputStream);
                    TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(filename, LF_NORMAL);
                    tarArchiveEntry.setSize(bytes.length);
                    try {
                        taos.putArchiveEntry(tarArchiveEntry);
                        taos.write(bytes);
                        taos.closeArchiveEntry();
                        taos.close();
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });
            case "TAR_BZIP2":
            return new SendDataOutputStream(bytes -> {
                try {
                    TarArchiveOutputStream taos = new TarArchiveOutputStream(new BZip2CompressorOutputStream(outputStream));
                    TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(filename, LF_NORMAL);
                    tarArchiveEntry.setSize(bytes.length);
                    taos.putArchiveEntry(tarArchiveEntry);
                    taos.write(bytes);
                    taos.closeArchiveEntry();
                    taos.close();
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });
            case "TAR_GZIP":
            case "TGZ":
                return new SendDataOutputStream(bytes -> {
                    GzipParameters tgZipParameters = new GzipParameters();
                    tgZipParameters.setFilename(filename);

                    try {
                        TarArchiveOutputStream taos = new TarArchiveOutputStream(new GzipCompressorOutputStream(outputStream, tgZipParameters));
                        TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(filename, LF_NORMAL);
                        tarArchiveEntry.setSize(bytes.length);
                        taos.putArchiveEntry(tarArchiveEntry);
                        taos.write(bytes);
                        taos.closeArchiveEntry();
                        taos.close();
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });
            case "ZIP":
                ZipOutputStream zaos = new ZipOutputStream(outputStream);
                zaos.putNextEntry(new ZipEntry(filename));
                return new OutputStream() {
                    @Override
                    public void write(int b) throws IOException {
                        zaos.write(b);
                    }

                    @Override
                    public void write(byte[] b) throws IOException {
                        zaos.write(b);
                    }

                    @Override
                    public void write(byte[] b, int off, int len) throws IOException {
                        zaos.write(b, off, len);
                    }

                    @Override
                    public void close() throws IOException {
                        zaos.closeEntry();
                        super.close();
                        zaos.close();
                    }
                };
        }
        return outputStream;
    }

    private void createTarArchive() {
//                TarArchiveOutputStream taos = new TarArchiveOutputStream(outputStream);
//                TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(filename, LF_NORMAL);
//                tarArchiveEntry.setSize(TarConstants.MAXSIZE - 1);
//                taos.putArchiveEntry(tarArchiveEntry);
//                AtomicLong counter = new AtomicLong();
//                return new OutputStream() {
//                    @Override
//                    public void write(int b) throws IOException {
//                        taos.write(b);
//                        counter.incrementAndGet();
//                    }
//
//                    @Override
//                    public void write(byte[] b) throws IOException {
//                        taos.write(b);
//                        counter.addAndGet(b.length);
//                    }
//
//                    @Override
//                    public void write(byte[] b, int off, int len) throws IOException {
//                        taos.write(b, off, len);
//                        counter.addAndGet(len);
//                    }
//
//                    @SneakyThrows
//                    @Override
//                    public void close() throws IOException {
//                        tarArchiveEntry.setSize(counter.get());
//                        ReflectionUtils.setField(TarArchiveOutputStream.class.getDeclaredField("currSize"), taos, counter.get());
//                        taos.closeArchiveEntry();
//                        super.close();
//                        taos.close();
//                    }
//                };
    }

    public static String getCompressionPrefix(String format) {
        switch (format) {
            case "BZIP":
                return "bz2:";
            case "GZIP":
                return "gz:";
            case "JAR":
                return "jar:";
            case "NONE":
                return "";
            case "TAR":
                return "tar:";
            case "TAR_BZIP2":
                return "tag:bz2:";
            case "TAR_GZIP":
                return "tar:gz:";
            case "TGZ":
                return "tgz:";
            case "ZIP":
                return "zip:";
        }
        return "";
    }
}

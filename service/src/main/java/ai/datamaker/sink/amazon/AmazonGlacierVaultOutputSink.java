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

package ai.datamaker.sink.amazon;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.DataOutputSink;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.glacier.GlacierClient;
import software.amazon.awssdk.services.glacier.model.UploadArchiveRequest;
import software.amazon.awssdk.services.glacier.model.UploadArchiveResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

@Slf4j
public class AmazonGlacierVaultOutputSink implements DataOutputSink, AmazonAwsCommon {

    static final int ONE_MB = 1024 * 1024;

    public static final PropertyConfig GLACIER_VAULT_NAME_PROPERTY = new PropertyConfig("amazon.glacier.sink.vault.name",
                                                                                    "Vault name",
                                                                                    PropertyConfig.ValueType.STRING,
                                                                                    "",
                                                                                    Collections.emptyList());

    public static final PropertyConfig GLACIER_FILE_NAME_PATTERN_PROPERTY = new PropertyConfig("amazon.glacier.sink.name.pattern",
                                                                                          "Filename",
                                                                                          PropertyConfig.ValueType.EXPRESSION,
                                                                                          "#dataset.name + \"-\" + T(java.lang.System).currentTimeMillis() + \".\" + #dataJob.generator.dataType.name().toLowerCase()",
                                                                                          Collections.emptyList());

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {

        final Region region = Region.of((String) config.getConfigProperty(AMAZON_AWS_REGION));
        final GlacierClient glacierClient = GlacierClient.builder().credentialsProvider(getCredentials(config)).region(region).build();

        return new OutputStream() {

            private File tempFile = File.createTempFile("glacier-", ".txt");
            private FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
            private boolean isClosed = false;

            @Override
            public void write(int b) throws IOException {
                fileOutputStream.write(b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                fileOutputStream.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                fileOutputStream.write(b, off, len);
            }

            @Override
            public void flush() throws IOException {
            }

            @SneakyThrows
            @Override
            public void close() throws IOException {
                if (!isClosed) {
                    // fileOutputStream.flush();
                    fileOutputStream.close();
                    UploadArchiveRequest uploadArchiveRequest = UploadArchiveRequest
                            .builder()
                            .vaultName(config.getProperty(GLACIER_VAULT_NAME_PROPERTY.getKey()))
                            .accountId("-")
                            .checksum(toHex(computeSHA256TreeHash(tempFile)))
                            .build();

                    UploadArchiveResponse response = glacierClient.uploadArchive(uploadArchiveRequest, tempFile.toPath());
                    log.debug(response.toString());
                }
                isClosed = true;
                boolean result = tempFile.delete();
            }
        };
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        List<PropertyConfig> properties = Lists.newArrayList(GLACIER_VAULT_NAME_PROPERTY, GLACIER_FILE_NAME_PATTERN_PROPERTY);
        addDefaultProperties(properties);
        return properties;
    }

    /**
     * Computes the SHA-256 tree hash for the given file
     *
     * @param inputFile
     *            a File to compute the SHA-256 tree hash for
     * @return a byte[] containing the SHA-256 tree hash
     * @throws IOException
     *             Thrown if there's an issue reading the input file
     * @throws NoSuchAlgorithmException
     */
    public static byte[] computeSHA256TreeHash(File inputFile) throws IOException, NoSuchAlgorithmException {

        byte[][] chunkSHA256Hashes = getChunkSHA256Hashes(inputFile);
        return computeSHA256TreeHash(chunkSHA256Hashes);
    }

    /**
     * Computes a SHA256 checksum for each 1 MB chunk of the input file. This
     * includes the checksum for the last chunk even if it is smaller than 1 MB.
     *
     * @param file
     *            A file to compute checksums on
     * @return a byte[][] containing the checksums of each 1 MB chunk
     * @throws IOException
     *             Thrown if there's an IOException when reading the file
     * @throws NoSuchAlgorithmException
     *             Thrown if SHA-256 MessageDigest can't be found
     */
    public static byte[][] getChunkSHA256Hashes(File file) throws IOException,
            NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        long numChunks = file.length() / ONE_MB;
        if (file.length() % ONE_MB > 0) {
            numChunks++;
        }

        if (numChunks == 0) {
            return new byte[][] { md.digest() };
        }

        byte[][] chunkSHA256Hashes = new byte[(int) numChunks][];
        FileInputStream fileStream = null;

        try {
            fileStream = new FileInputStream(file);
            byte[] buff = new byte[ONE_MB];

            int bytesRead;
            int idx = 0;

            while ((bytesRead = fileStream.read(buff, 0, ONE_MB)) > 0) {
                md.reset();
                md.update(buff, 0, bytesRead);
                chunkSHA256Hashes[idx++] = md.digest();
            }

            return chunkSHA256Hashes;

        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException ioe) {
                    System.err.printf("Exception while closing %s.\n %s", file.getName(),
                                      ioe.getMessage());
                }
            }
        }
    }

    /**
     * Computes the SHA-256 tree hash for the passed array of 1 MB chunk
     * checksums.
     *
     * This method uses a pair of arrays to iteratively compute the tree hash
     * level by level. Each iteration takes two adjacent elements from the
     * previous level source array, computes the SHA-256 hash on their
     * concatenated value and places the result in the next level's destination
     * array. At the end of an iteration, the destination array becomes the
     * source array for the next level.
     *
     * @param chunkSHA256Hashes
     *            An array of SHA-256 checksums
     * @return A byte[] containing the SHA-256 tree hash for the input chunks
     * @throws NoSuchAlgorithmException
     *             Thrown if SHA-256 MessageDigest can't be found
     */
    public static byte[] computeSHA256TreeHash(byte[][] chunkSHA256Hashes)
            throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        byte[][] prevLvlHashes = chunkSHA256Hashes;

        while (prevLvlHashes.length > 1) {

            int len = prevLvlHashes.length / 2;
            if (prevLvlHashes.length % 2 != 0) {
                len++;
            }

            byte[][] currLvlHashes = new byte[len][];

            int j = 0;
            for (int i = 0; i < prevLvlHashes.length; i = i + 2, j++) {

                // If there are at least two elements remaining
                if (prevLvlHashes.length - i > 1) {

                    // Calculate a digest of the concatenated nodes
                    md.reset();
                    md.update(prevLvlHashes[i]);
                    md.update(prevLvlHashes[i + 1]);
                    currLvlHashes[j] = md.digest();

                } else { // Take care of remaining odd chunk
                    currLvlHashes[j] = prevLvlHashes[i];
                }
            }

            prevLvlHashes = currLvlHashes;
        }

        return prevLvlHashes[0];
    }

    /**
     * Returns the hexadecimal representation of the input byte array
     *
     * @param data
     *            a byte[] to convert to Hex characters
     * @return A String containing Hex characters
     */
    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);

        for (int i = 0; i < data.length; i++) {
            String hex = Integer.toHexString(data[i] & 0xFF);

            if (hex.length() == 1) {
                // Append leading zero.
                sb.append("0");
            }
            sb.append(hex);
        }
        return sb.toString().toLowerCase();
    }

}

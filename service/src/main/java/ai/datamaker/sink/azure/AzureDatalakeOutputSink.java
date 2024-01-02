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

package ai.datamaker.sink.azure;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.DataOutputSinkType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.DataOutputSink;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.azure.storage.file.datalake.DataLakeFileSystemClient;
import com.azure.storage.file.datalake.DataLakeServiceClient;
import com.azure.storage.file.datalake.DataLakeServiceClientBuilder;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@DataOutputSinkType(compressed = true, encrypted = true)
public class AzureDatalakeOutputSink implements DataOutputSink, AzureCommon {

    public static final PropertyConfig AZURE_FILESYSTEM_NAME =
            new PropertyConfig("azure.storage.sink.filesystem.name",
                               "Filesystem name",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig AZURE_FILENAME =
            new PropertyConfig("azure.storage.sink.filename",
                               "File name",
                               PropertyConfig.ValueType.EXPRESSION,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig AZURE_STORAGE_BUFFER =
            new PropertyConfig("azure.storage.sink.buffer",
                               "Storage buffer",
                               PropertyConfig.ValueType.NUMERIC,
                               1024 * 1024,
                               Collections.emptyList());

    public static final PropertyConfig AZURE_DATALAKE_OVERWRITE =
            new PropertyConfig("azure.storage.sink.overwrite",
                               "Overwrite flag",
                               PropertyConfig.ValueType.BOOLEAN,
                               true,
                               Collections.emptyList());


    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @VisibleForTesting
    DataLakeFileClient getClient(JobConfig config) {
        String accountName = (String) config.getConfigProperty(AZURE_STORAGE_ACCOUNT_NAME);
        String accountKey = (String) config.getConfigProperty(AZURE_STORAGE_ACCOUNT_KEY);
        String sasToken = (String) config.getConfigProperty(AZURE_STORAGE_SAS_TOKEN);
        String fileName = (String) config.getConfigProperty(AZURE_FILENAME);
        String fileSystemName = (String) config.getConfigProperty(AZURE_FILESYSTEM_NAME);

        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

        DataLakeServiceClientBuilder dataLakeServiceClientBuilder = new DataLakeServiceClientBuilder();
        dataLakeServiceClientBuilder.endpoint(endpoint);

        if (StringUtils.isNotBlank(sasToken)) {
            dataLakeServiceClientBuilder.sasToken(sasToken);
        } else {
            StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
            dataLakeServiceClientBuilder.credential(credential);
        }

        DataLakeServiceClient storageClient = dataLakeServiceClientBuilder.buildClient();
        DataLakeFileSystemClient dataLakeFileSystemClient = storageClient.getFileSystemClient(fileSystemName);

        log.debug("Container: {}", dataLakeFileSystemClient.getProperties());

        // TODO create if empty
        //dataLakeFileSystemClient.create();

        // TODO implement async

//        DataLakeFileAsyncClient asyncClient = new DataLakeServiceClientBuilder().endpoint(endpoint).credential(credential).buildAsyncClient().getFileSystemAsyncClient(containerName).getFileAsyncClient("sourceFile");
//
//        asyncClient.upload(Flux.just(), null).block();
//
        DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient(fileName);
        fileClient.create(true);
        // fileClient.setAccessControlList();
        // fileClient.setMetadata();
        return fileClient;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {
        final int bufferSize = (int) config.getConfigProperty(AZURE_STORAGE_BUFFER);
        final DataLakeFileClient fileClient = getClient(config);

        return new OutputStream() {

            private ByteArrayOutputStream baos = new ByteArrayOutputStream();
            private boolean isClosed = false;
            private AtomicLong offset = new AtomicLong();

            @Override
            public void write(int b) throws IOException {
                baos.write(b);
                if (baos.size() > bufferSize) {
                    sendData();
                }
            }

            @Override
            public void write(byte[] b) throws IOException {
                baos.write(b);
                if (baos.size() > bufferSize) {
                    sendData();
                }
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                baos.write(b, off, len);
                if (baos.size() > bufferSize) {
                    sendData();
                }
            }

            @Override
            public void flush() throws IOException {
            }

            @Override
            public void close() throws IOException {
                if (!isClosed && baos.size() > 0) {
                    sendData();
                }

                isClosed = true;
            }

            private void sendData() throws IOException {
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                fileClient.append(bais, offset.get(), baos.toByteArray().length);
                bais.close();
                long position = offset.addAndGet(baos.toByteArray().length);
                fileClient.flush(position);
                baos.reset();
            }
        };
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return addDefaultProperties(
                Lists.newArrayList(AZURE_FILESYSTEM_NAME,
                                   AZURE_FILENAME,
                                   AZURE_STORAGE_BUFFER,
                                   AZURE_DATALAKE_OVERWRITE));
    }
}

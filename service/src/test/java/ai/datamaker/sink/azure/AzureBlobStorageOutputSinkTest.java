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
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.azure.AzureBlobStorageOutputSink;
import ai.datamaker.sink.azure.AzureCommon;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlobOutputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AzureBlobStorageOutputSinkTest {

    private final AzureBlobStorageOutputSink sink = Mockito.spy(AzureBlobStorageOutputSink.class);

    @Test
    void accept() {
        Arrays.stream(FormatType.values()).forEach(ft -> assertTrue(sink.accept(ft)));
    }

    @Test
    void getOutputStream() throws Exception {

        BlobContainerClient client = Mockito.mock(BlobContainerClient.class);
        doReturn(client).when(sink).getClient(any(JobConfig.class));
        BlobClient blobClient = Mockito.mock(BlobClient.class);
        BlockBlobClient blockBlobClient = Mockito.mock(BlockBlobClient.class);
        when(client.getBlobClient(eq("blob"))).thenReturn(blobClient);
        when(blobClient.getBlockBlobClient()).thenReturn(blockBlobClient);

        BlobOutputStream blobOutputStream = Mockito.mock(BlobOutputStream.class);
        when(blockBlobClient.getBlobOutputStream(eq(true))).thenReturn(blobOutputStream);

        JobConfig config = new JobConfig();
        //config.put(AzureBlobStorageOutputSink.S3_FILE_NAME_PATTERN_PROPERTY, "");
        sink.getConfigProperties().forEach(cp -> {
                                               if (cp.getType() == PropertyConfig.ValueType.STRING) {
                                                   config.put(cp,
                                                              "abc");
                                               }
                                               if (cp.getType() == PropertyConfig.ValueType.LIST) {
                                                   config.put(cp,
                                                              Lists.newArrayList("'abc'"));
                                               }
                                           }
        );
        config.put(AzureBlobStorageOutputSink.AZURE_BLOB_NAME, "'blob'");
        config.put(AzureCommon.AZURE_STORAGE_ACCOUNT_KEY, "key");
        config.remove(AzureBlobStorageOutputSink.AZURE_STORAGE_SAS_TOKEN.getKey());

        try (OutputStream output = sink.getOutputStream(config)) {
            output.write("hello".getBytes(StandardCharsets.UTF_8));
        }

        verify(blobOutputStream, times(1)).write(any(byte[].class));
    }

    @Test
    void getOutputStream_page() throws Exception {

        BlobContainerClient client = Mockito.mock(BlobContainerClient.class);
        doReturn(client).when(sink).getClient(any(JobConfig.class));
        BlobClient blobClient = Mockito.mock(BlobClient.class);
        PageBlobClient pageBlobClient = Mockito.mock(PageBlobClient.class);
        when(client.getBlobClient(eq("blob"))).thenReturn(blobClient);
        when(blobClient.getPageBlobClient()).thenReturn(pageBlobClient);

        BlobOutputStream blobOutputStream = Mockito.mock(BlobOutputStream.class);
        when(pageBlobClient.getBlobOutputStream(any(PageRange.class))).thenReturn(blobOutputStream);

        JobConfig config = new JobConfig();
        //config.put(AzureBlobStorageOutputSink.S3_FILE_NAME_PATTERN_PROPERTY, "");
        sink.getConfigProperties().forEach(cp -> {
                                               if (cp.getType() == PropertyConfig.ValueType.STRING) {
                                                   config.put(cp,
                                                              "abc");
                                               }
                                               if (cp.getType() == PropertyConfig.ValueType.LIST) {
                                                   config.put(cp,
                                                              Lists.newArrayList("'abc'"));
                                               }
                                           }
        );
        config.put(AzureBlobStorageOutputSink.AZURE_BLOB_NAME, "'blob'");
        config.put(AzureCommon.AZURE_STORAGE_ACCOUNT_KEY, "key");
        config.remove(AzureBlobStorageOutputSink.AZURE_STORAGE_SAS_TOKEN.getKey());
        config.put(AzureBlobStorageOutputSink.AZURE_BLOB_TYPE, "PAGE");
        config.put(AzureBlobStorageOutputSink.AZURE_BLOB_PAGE_RANGE_START, 1);
        config.put(AzureBlobStorageOutputSink.AZURE_BLOB_PAGE_RANGE_END, 6);

        try (OutputStream output = sink.getOutputStream(config)) {
            output.write("hello".getBytes(StandardCharsets.UTF_8));
        }

        ArgumentCaptor<PageRange> pageRangeArgumentCaptor = ArgumentCaptor.forClass(PageRange.class);
        verify(pageBlobClient, times(1)).getBlobOutputStream(pageRangeArgumentCaptor.capture());
        assertEquals(1, pageRangeArgumentCaptor.getValue().getStart());
        assertEquals(6, pageRangeArgumentCaptor.getValue().getEnd());
        verify(blobOutputStream, times(1)).write(any(byte[].class));
    }

    @Test
    void getOutputStream_snapshot() throws Exception {

        BlobContainerClient client = Mockito.mock(BlobContainerClient.class);
        doReturn(client).when(sink).getClient(any(JobConfig.class));

        BlobClient snapshotClient = Mockito.mock(BlobClient.class);
        when(client.getBlobClient(eq("blob"))).thenReturn(snapshotClient);
        BlockBlobClient blockBlobClient = Mockito.mock(BlockBlobClient.class);

        when(snapshotClient.getSnapshotClient(anyString())).thenReturn(snapshotClient);
        when(snapshotClient.getBlockBlobClient()).thenReturn(blockBlobClient);

        BlobOutputStream blobOutputStream = Mockito.mock(BlobOutputStream.class);
        when(blockBlobClient.getBlobOutputStream(eq(true))).thenReturn(blobOutputStream);

        JobConfig config = new JobConfig();
        //config.put(AzureBlobStorageOutputSink.S3_FILE_NAME_PATTERN_PROPERTY, "");
        sink.getConfigProperties().forEach(cp -> {
                                               if (cp.getType() == PropertyConfig.ValueType.STRING) {
                                                   config.put(cp,
                                                              "abc");
                                               }
                                               if (cp.getType() == PropertyConfig.ValueType.LIST) {
                                                   config.put(cp,
                                                              Lists.newArrayList("'abc'"));
                                               }
                                           }
        );
        config.put(AzureBlobStorageOutputSink.AZURE_BLOB_NAME, "'blob'");
        config.put(AzureCommon.AZURE_STORAGE_ACCOUNT_KEY, "key");
        config.put(AzureBlobStorageOutputSink.AZURE_BLOB_TYPE, "SNAPSHOT");
        config.put(AzureBlobStorageOutputSink.AZURE_SNAPSHOT_ID, "feaw");
        config.remove(AzureBlobStorageOutputSink.AZURE_STORAGE_SAS_TOKEN.getKey());

        try (OutputStream output = sink.getOutputStream(config)) {
            output.write("hello".getBytes(StandardCharsets.UTF_8));
        }

        verify(blobOutputStream, times(1)).write(any(byte[].class));
    }

    @Test
    void getOutputStream_append() throws Exception {

        BlobContainerClient client = Mockito.mock(BlobContainerClient.class);
        doReturn(client).when(sink).getClient(any(JobConfig.class));
        BlobClient blobClient = Mockito.mock(BlobClient.class);
        AppendBlobClient appendBlobClient = Mockito.mock(AppendBlobClient.class);
        when(client.getBlobClient(eq("blob"))).thenReturn(blobClient);
        when(blobClient.getAppendBlobClient()).thenReturn(appendBlobClient);

        BlobOutputStream blobOutputStream = Mockito.mock(BlobOutputStream.class);
        when(appendBlobClient.getBlobOutputStream()).thenReturn(blobOutputStream);

        JobConfig config = new JobConfig();
        //config.put(AzureBlobStorageOutputSink.S3_FILE_NAME_PATTERN_PROPERTY, "");
        sink.getConfigProperties().forEach(cp -> {
                                               if (cp.getType() == PropertyConfig.ValueType.STRING) {
                                                   config.put(cp,
                                                              "abc");
                                               }
                                               if (cp.getType() == PropertyConfig.ValueType.LIST) {
                                                   config.put(cp,
                                                              Lists.newArrayList("'abc'"));
                                               }
                                           }
        );
        config.put(AzureBlobStorageOutputSink.AZURE_BLOB_NAME, "'blob'");
        config.put(AzureBlobStorageOutputSink.AZURE_BLOB_TYPE, "APPEND");
        config.put(AzureCommon.AZURE_STORAGE_ACCOUNT_KEY, "key");
        config.remove(AzureBlobStorageOutputSink.AZURE_STORAGE_SAS_TOKEN.getKey());

        try (OutputStream output = sink.getOutputStream(config)) {
            output.write("hello".getBytes(StandardCharsets.UTF_8));
        }

        verify(blobOutputStream, times(1)).write(any(byte[].class));
    }

    @Test
    void getConfigProperties() {
        assertEquals(11, sink.getConfigProperties().size());
    }
}
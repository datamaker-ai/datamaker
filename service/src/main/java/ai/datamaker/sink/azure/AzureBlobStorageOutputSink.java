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
import ai.datamaker.model.PropertyConfig.ValueType;
import ai.datamaker.sink.DataOutputSink;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.specialized.AppendBlobClient;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.blob.specialized.PageBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Support Blob storage / datalake gen2
 */
@DataOutputSinkType(compressed = true, encrypted = true)
public class AzureBlobStorageOutputSink implements DataOutputSink, AzureCommon {

    public static final PropertyConfig AZURE_BLOB_TYPE =
            new PropertyConfig("azure.storage.sink.blob.type",
                               "Blob type",
                               ValueType.STRING,
                               "BLOCK",
                               Lists.newArrayList("BLOCK", "APPEND", "PAGE", "SNAPSHOT"));

    public static final PropertyConfig AZURE_CONTAINER_NAME =
            new PropertyConfig("azure.storage.sink.container.name",
                               "Container name",
                               ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig AZURE_BLOB_NAME =
            new PropertyConfig("azure.storage.sink.blob.name",
                               "Blob name",
                               ValueType.EXPRESSION,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig AZURE_SNAPSHOT_ID =
            new PropertyConfig("azure.storage.sink.snapshot",
                               "Snapshot ID",
                               ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig AZURE_BLOB_PAGE_RANGE_START =
            new PropertyConfig("azure.storage.sink.blob.page.range.start",
                               "Page range start",
                               ValueType.NUMERIC,
                               0,
                               Collections.emptyList());

    public static final PropertyConfig AZURE_BLOB_PAGE_RANGE_END =
            new PropertyConfig("azure.storage.sink.blob.page.range.end",
                               "Page range end",
                               ValueType.NUMERIC,
                               0,
                               Collections.emptyList());

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @VisibleForTesting
    BlobContainerClient getClient(JobConfig config) {
        String accountName = (String) config.getConfigProperty(AZURE_STORAGE_ACCOUNT_NAME);
        String accountKey = (String) config.getConfigProperty(AZURE_STORAGE_ACCOUNT_KEY);
        String sasToken = (String) config.getConfigProperty(AZURE_STORAGE_SAS_TOKEN);
        String containerName = (String) config.getConfigProperty(AZURE_CONTAINER_NAME);

        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

        BlobServiceClientBuilder blobServiceClientBuilder = new BlobServiceClientBuilder();
        blobServiceClientBuilder.endpoint(endpoint);

        if (StringUtils.isNotBlank(sasToken)) {
            blobServiceClientBuilder.sasToken(sasToken);
        } else {
            StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
            blobServiceClientBuilder.credential(credential);
        }
        BlobServiceClient storageClient = blobServiceClientBuilder.buildClient();

        BlobContainerClient blobContainerClient = storageClient.getBlobContainerClient(containerName);
        blobContainerClient.create();

        return blobContainerClient;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {

        // TODO queue, table
        BlobContainerClient blobContainerClient = getClient(config);
        String blobName = (String) config.getConfigProperty(AZURE_BLOB_NAME);
        String blobType = (String) config.getConfigProperty(AZURE_BLOB_TYPE);

        switch (blobType) {
            case "PAGE":
                PageBlobClient pageBlobClient = blobContainerClient.getBlobClient(blobName).getPageBlobClient();
                PageRange pageRange = new PageRange();
                Integer pageStart = (Integer) config.getConfigProperty(AZURE_BLOB_PAGE_RANGE_START);
                Integer pageEnd = (Integer) config.getConfigProperty(AZURE_BLOB_PAGE_RANGE_END);

                pageRange.setEnd(pageEnd).setStart(pageStart);
                return pageBlobClient.getBlobOutputStream(pageRange);
            case "SNAPSHOT":
                BlobClient snapshotClient = blobContainerClient.getBlobClient(blobName).getSnapshotClient((String) config.getConfigProperty(AZURE_SNAPSHOT_ID));
                return snapshotClient.getBlockBlobClient().getBlobOutputStream(true);
            case "APPEND":
                AppendBlobClient appendBlobClient = blobContainerClient.getBlobClient(blobName).getAppendBlobClient();
                return appendBlobClient.getBlobOutputStream();
            default:
                BlockBlobClient blobClient = blobContainerClient.getBlobClient(blobName).getBlockBlobClient();
                return blobClient.getBlobOutputStream(true);
        }
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return addDefaultProperties(Lists.newArrayList(
            AZURE_CONTAINER_NAME,
            AZURE_BLOB_NAME,
            AZURE_BLOB_TYPE,
            AZURE_BLOB_PAGE_RANGE_START,
            AZURE_BLOB_PAGE_RANGE_END,
            AZURE_SNAPSHOT_ID
        ));
    }
}

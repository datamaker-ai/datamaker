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

package ai.datamaker.sink.google;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.DataOutputSinkType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.DataOutputSink;
import com.google.auth.Credentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.utils.StringInputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@DataOutputSinkType(compressed = true, encrypted = true)
public class GoogleCloudStorageOutputSink implements DataOutputSink, GcpCommon {

    public static final PropertyConfig GOOGLE_STORAGE_BUCKET_NAME =
            new PropertyConfig("google.storage.sink.bucket.name",
                               "Container name",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig GOOGLE_STORAGE_OBJECT_NAME =
            new PropertyConfig("google.storage.sink.object.name",
                               "Object name",
                               PropertyConfig.ValueType.EXPRESSION,
                               "",
                               Collections.emptyList());

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return addDefaultProperties(
                Lists.newArrayList(GOOGLE_STORAGE_BUCKET_NAME,
                                  GOOGLE_STORAGE_OBJECT_NAME));
    }

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {

        String bucketName = (String) config.getConfigProperty(GOOGLE_STORAGE_BUCKET_NAME);
        String objectName = (String) config.getConfigProperty(GOOGLE_STORAGE_OBJECT_NAME);

        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        Storage storage = getStorage(config);

        return Channels.newOutputStream(storage.create(blobInfo).writer());
    }

    @VisibleForTesting
    Storage getStorage(JobConfig config) throws IOException {
        String authenticationType = (String) config.getConfigProperty(GOOGLE_CLOUD_AUTHENTICATION_TYPE);

        StorageOptions.Builder builder = StorageOptions.newBuilder();

        String projectId = (String) config.getConfigProperty(GOOGLE_CLOUD_PROJECT_ID);
        if (StringUtils.isNotBlank(projectId)) {
            builder.setProjectId(projectId);
        }
        switch (authenticationType) {
            // When using Google Cloud libraries from a Google Cloud Platform environment such as Compute Engine, Kubernetes Engine,
            // or App Engine, no additional authentication steps are necessary.
            case "SERVICE_ACCOUNT":
                String jsonKey = (String) config.getConfigProperty(GOOGLE_CLOUD_JSON_KEY);
                builder.setCredentials(GoogleCredentials.fromStream(new StringInputStream(jsonKey)));
                break;
            case "OAUTH_TOKEN":
                String accessToken = (String) config.getConfigProperty(GOOGLE_CLOUD_OAUTH_TOKEN_VALUE);
                Credentials credentials = GoogleCredentials.create(new AccessToken(accessToken, new Date()));
                builder.setCredentials(credentials);
                break;
            case "PLATFORM":
        }

        return builder.build().getService();
    }
}

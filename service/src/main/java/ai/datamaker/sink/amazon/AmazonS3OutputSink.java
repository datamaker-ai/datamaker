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
import ai.datamaker.model.DataOutputSinkType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.DataOutputSink;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.ObjectLockMode;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.model.StorageClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@DataOutputSinkType(compressed = true, encrypted = true)
public class AmazonS3OutputSink implements AmazonAwsCommon, DataOutputSink {

    public static final PropertyConfig S3_BUCKET_NAME_PROPERTY = new PropertyConfig("amazon.s3.sink.bucket.name",
                                                                                    "S3 Bucket name",
                                                                                    PropertyConfig.ValueType.STRING,
                                                                                    "",
                                                                                    Collections.emptyList());

    public static final PropertyConfig S3_FILE_NAME_PATTERN_PROPERTY = new PropertyConfig("amazon.s3.sink.name.pattern",
                                                                                          "S3 Object key",
                                                                                          PropertyConfig.ValueType.EXPRESSION,
                                                                                          "#dataset.name + \"-\" + T(java.lang.System).currentTimeMillis() + \".\" + #dataJob.generator.dataType.name().toLowerCase()",
                                                                                          Collections.emptyList());

    public static final PropertyConfig S3_METADATA_KEYS = new PropertyConfig("amazon.s3.sink.metadata.keys",
                                                                             "A map of metadata to store with the object in S3",
                                                                             PropertyConfig.ValueType.LIST,
                                                                             Collections.emptyList(),
                                                                             Collections.emptyList());

    public static final PropertyConfig S3_METADATA_VALUES = new PropertyConfig("amazon.s3.sink.metadata.values",
                                                                               "A map of metadata to store with the object in S3 (support expression)",
                                                                               PropertyConfig.ValueType.LIST,
                                                                               Collections.emptyList(),
                                                                               Collections.emptyList());

    public static final PropertyConfig S3_ACL = new PropertyConfig("amazon.s3.sink.acl",
                                                                   "The canned ACL to apply to the object. For more information, see Canned ACL.",
                                                                   PropertyConfig.ValueType.STRING,
                                                                   null,
                                                                   ObjectCannedACL.knownValues().stream().map(ObjectCannedACL::toString).collect(Collectors.toList()));

    public static final PropertyConfig S3_CACHE_CONTROL = new PropertyConfig("amazon.s3.sink.cache.control",
                                                                   "Can be used to specify caching behavior along the request/reply chain. For more information, see http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9",
                                                                   PropertyConfig.ValueType.STRING,
                                                                   null,
                                                                   Collections.emptyList());

    public static final PropertyConfig S3_CONTENT_TYPE = new PropertyConfig("amazon.s3.sink.content.type",
                                                                             "A standard MIME type describing the format of the contents. For more information, see http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.17",
                                                                             PropertyConfig.ValueType.STRING,
                                                                             null,
                                                                             Collections.emptyList());

    public static final PropertyConfig S3_CONTENT_DISPOSITION = new PropertyConfig("amazon.s3.sink.content.disposition",
                                                                            "Specifies presentational information for the object. For more information, see http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html#sec19.5.1",
                                                                            PropertyConfig.ValueType.STRING,
                                                                            null,
                                                                            Collections.emptyList());

    public static final PropertyConfig S3_CONTENT_ENCODING = new PropertyConfig("amazon.s3.sink.content.encoding",
                                                                            "Specifies what content encodings have been applied to the object and thus what decoding mechanisms must be applied to obtain the media-type referenced by the Content-Type header field. For more information, see http://www.w3.org/Protocols/rfc2616/ rfc2616-sec14.html#sec14.11",
                                                                            PropertyConfig.ValueType.STRING,
                                                                            null,
                                                                            Collections.emptyList());

    public static final PropertyConfig S3_CONTENT_LANGUAGE = new PropertyConfig("amazon.s3.sink.content.language",
                                                                                "The language the content is in",
                                                                                PropertyConfig.ValueType.STRING,
                                                                                null,
                                                                                Collections.emptyList());

    public static final PropertyConfig S3_EXPIRES = new PropertyConfig("amazon.s3.sink.expires",
                                                                                "The date and time at which the object is no longer cacheable. For more information, see http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21",
                                                                                PropertyConfig.ValueType.STRING,
                                                                                null,
                                                                                Collections.emptyList());

    public static final PropertyConfig S3_BUCKET_OWNER = new PropertyConfig("amazon.s3.sink.bucket.owner",
                                                                                "",
                                                                                PropertyConfig.ValueType.STRING,
                                                                                null,
                                                                                Collections.emptyList());

    public static final PropertyConfig S3_GRANT_FULL_CONTROL = new PropertyConfig("amazon.s3.sink.grant.fullcontrol",
                                                                                "Gives the grantee READ, READ_ACP, and WRITE_ACP permissions on the object.",
                                                                                PropertyConfig.ValueType.STRING,
                                                                                null,
                                                                                Collections.emptyList());

    public static final PropertyConfig S3_GRANT_READ = new PropertyConfig("amazon.s3.sink.grant.read",
                                                                                "Allows grantee to read the object data and its metadata",
                                                                                PropertyConfig.ValueType.STRING,
                                                                                null,
                                                                                Collections.emptyList());

    public static final PropertyConfig S3_GRANT_READ_ACP = new PropertyConfig("amazon.s3.sink.grant.read.acp",
                                                                                "Allows grantee to read the object AC",
                                                                                PropertyConfig.ValueType.STRING,
                                                                                null,
                                                                                Collections.emptyList());

    public static final PropertyConfig S3_GRANT_WRITE_ACP = new PropertyConfig("amazon.s3.sink.grant.write.acp",
                                                                                "Allows grantee to write the ACL for the applicable object",
                                                                                PropertyConfig.ValueType.STRING,
                                                                                null,
                                                                                Collections.emptyList());

    public static final PropertyConfig S3_REQUEST_PAYER = new PropertyConfig("amazon.s3.sink.request.payer",
                                                                                "Sets the value of the RequestPayer property for this object",
                                                                                PropertyConfig.ValueType.STRING,
                                                                                null,
                                                                                Lists.newArrayList("requester"));

    public static final PropertyConfig S3_OBJECT_LOCK_MODE = new PropertyConfig("amazon.s3.sink.object.lock.mode",
                                                                                "The Object Lock mode that you want to apply to this object.",
                                                                                PropertyConfig.ValueType.STRING,
                                                                                null,
                                                                                ObjectLockMode.knownValues().stream().map(ObjectLockMode::toString).collect(Collectors.toList()));

    public static final PropertyConfig S3_TAGGING = new PropertyConfig("amazon.s3.sink.tagging",
                                                                                "The tag-set for the object. The tag-set must be encoded as URL Query parameters. (For example, \"Key1=Value1\")",
                                                                                PropertyConfig.ValueType.STRING,
                                                                                null,
                                                                                Collections.emptyList());

    public static final PropertyConfig S3_SERVER_SIDE_ENCRYPTION = new PropertyConfig("amazon.s3.sink.server.side.encryption",
                                                                                      "The server-side encryption algorithm used when storing this object in Amazon S3 (for example, AES256, aws:kms)",
                                                                                      PropertyConfig.ValueType.STRING,
                                                                                      null,
                                                                                      ServerSideEncryption.knownValues().stream().map(ServerSideEncryption::toString).collect(Collectors.toList()));

    public static final PropertyConfig S3_STORAGE_CLASS = new PropertyConfig("amazon.s3.sink.storage.class",
                                                                                "By default, Amazon S3 uses the STANDARD Storage Class to store newly created objects. The STANDARD storage class provides high durability and high availability. Depending on performance needs, you can specify a different Storage Class. Amazon S3 on Outposts only uses the OUTPOSTS Storage Class. For more information, see Storage Classes  in the Amazon S3 User Guide.",
                                                                                PropertyConfig.ValueType.STRING,
                                                                                null,
                                                                                StorageClass.knownValues().stream().map(StorageClass::toString).collect(Collectors.toList()));

    public static final PropertyConfig S3_SSE_KMS_KEY_ID = new PropertyConfig("amazon.s3.sink.sse.kms.keyid",
                                                                                "If x-amz-server-side-encryption is present and has the value of aws:kms, this header specifies the ID of the Amazon Web Services Key Management Service (Amazon Web Services KMS) symmetrical customer managed key that was used for the object. If you specify x-amz-server-side-encryption:aws:kms, but do not provide x-amz-server-side-encryption-aws-kms-key-id, Amazon S3 uses the Amazon Web Services managed key to protect the data. If the KMS key does not exist in the same account issuing the command, you must use the full ARN and not just the ID.",
                                                                                PropertyConfig.ValueType.STRING,
                                                                                null,
                                                                                Collections.emptyList());

    public static final PropertyConfig S3_SSE_CUSTOMER_ALGORITHM = new PropertyConfig("amazon.s3.sink.sse.customer.algorithm",
                                                                                "Specifies the algorithm to use to when encrypting the object (for example, AES256)",
                                                                                PropertyConfig.ValueType.STRING,
                                                                                null,
                                                                                Collections.emptyList());

    public static final PropertyConfig S3_SSE_CUSTOMER_KEY = new PropertyConfig("amazon.s3.sink.sse.customer.key",
                                                                                "Specifies the customer-provided encryption key for Amazon S3 to use in encrypting data. This value is used to store the object and then it is discarded; Amazon S3 does not store the encryption key. The key must be appropriate for use with the algorithm specified in the x-amz-server-side-encryption-customer-algorithm header.",
                                                                                PropertyConfig.ValueType.STRING,
                                                                                null,
                                                                                Collections.emptyList());

    public static final PropertyConfig S3_SSE_CUSTOMER_KEY_MD5 = new PropertyConfig("amazon.s3.sink.sse.customer.key.md5",
                                                                                "Specifies the 128-bit MD5 digest of the encryption key according to RFC 1321. Amazon S3 uses this header for a message integrity check to ensure that the encryption key was transmitted without error",
                                                                                PropertyConfig.ValueType.STRING,
                                                                                null,
                                                                                Collections.emptyList());

    public static final PropertyConfig S3_KMS_ENCRYPTION_CONTEXT = new PropertyConfig("amazon.s3.sink.kms.encryption.context",
                                                                                "Specifies the Amazon Web Services KMS Encryption Context to use for object encryption. The value of this header is a base64-encoded UTF-8 string holding JSON with the encryption context key-value pairs.",
                                                                                PropertyConfig.ValueType.STRING,
                                                                                null,
                                                                                Collections.emptyList());

    // Support for Amazon structure
    @Override
    public List<PropertyConfig> getConfigProperties() {
        return addDefaultProperties(Lists.newArrayList(S3_BUCKET_NAME_PROPERTY,
                                                       S3_FILE_NAME_PATTERN_PROPERTY,
                                                       S3_STORAGE_CLASS,
                                                       S3_METADATA_KEYS,
                                                       S3_METADATA_VALUES,
                                                       S3_ACL,
                                                       S3_CACHE_CONTROL,
                                                       S3_CONTENT_TYPE,
                                                       S3_CONTENT_DISPOSITION,
                                                       S3_CONTENT_ENCODING,
                                                       S3_CONTENT_LANGUAGE,
                                                       S3_EXPIRES,
                                                       S3_BUCKET_OWNER,
                                                       S3_GRANT_FULL_CONTROL,
                                                       S3_GRANT_READ,
                                                       S3_GRANT_READ_ACP,
                                                       S3_GRANT_WRITE_ACP,
                                                       S3_REQUEST_PAYER,
                                                       S3_OBJECT_LOCK_MODE,
                                                       S3_TAGGING,
                                                       S3_SERVER_SIDE_ENCRYPTION,
                                                       S3_SSE_KMS_KEY_ID,
                                                       S3_SSE_CUSTOMER_ALGORITHM,
                                                       S3_SSE_CUSTOMER_KEY,
                                                       S3_SSE_CUSTOMER_KEY_MD5,
                                                       S3_KMS_ENCRYPTION_CONTEXT));
    }

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @VisibleForTesting
    S3Client getClient(JobConfig config) {
        Region region = Region.of((String) config.getConfigProperty(AMAZON_AWS_REGION));
        return S3Client
                .builder()
                .credentialsProvider(getCredentials(config))
                .region(region)
                .build();
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {

        final S3Client s3 = getClient(config);

        final Map<String, String> metadata = Maps.newHashMap();
        if (config.containsKey(S3_METADATA_KEYS.getKey())) {
            List<String> headerNames = (List<String>) config.getConfigProperty(S3_METADATA_KEYS);
            List<String> headerValues = (List<String>) config.getConfigProperty(S3_METADATA_VALUES);
            Assert.isTrue(headerNames.size() == headerValues.size(), "Number of header names and values should match");
            for (int i=0; i<headerNames.size(); i++) {
                metadata.put(headerNames.get(i), String.valueOf(parseExpression(headerValues.get(i), config)));
            }
        }

        // TODO use file if large dataset
        // config.getDataset().getNumberOfRecords()

        return new OutputStream() {

            private ByteArrayOutputStream baos = new ByteArrayOutputStream();
            private boolean isClosed = false;

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
            }

            @Override
            public void close() throws IOException {
                if (!isClosed && baos.size() > 0) {
                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket((String) config.getConfigProperty(S3_BUCKET_NAME_PROPERTY))
                            .key((String) config.getConfigProperty(S3_FILE_NAME_PATTERN_PROPERTY))
                            .metadata(metadata)
                            .acl((String) config.getConfigProperty(S3_ACL))
                            .cacheControl((String) config.getConfigProperty(S3_CACHE_CONTROL))
                            .contentType((String) config.getConfigProperty(S3_CONTENT_TYPE))
                            .contentEncoding((String) config.getConfigProperty(S3_CONTENT_ENCODING))
                            .contentDisposition((String) config.getConfigProperty(S3_CONTENT_DISPOSITION))
                            .contentLanguage((String) config.getConfigProperty(S3_CONTENT_LANGUAGE))
                            .expires(config.containsKey(S3_EXPIRES.getKey()) ? Instant.parse(config.getProperty(S3_EXPIRES.getKey())) : null)
                            .expectedBucketOwner((String) config.getConfigProperty(S3_BUCKET_OWNER))
                            .grantFullControl((String) config.getConfigProperty(S3_GRANT_FULL_CONTROL))
                            .grantRead((String) config.getConfigProperty(S3_GRANT_READ))
                            .grantReadACP((String) config.getConfigProperty(S3_GRANT_READ_ACP))
                            .grantWriteACP((String) config.getConfigProperty(S3_GRANT_WRITE_ACP))
                            .requestPayer((String) config.getConfigProperty(S3_REQUEST_PAYER))
                            .objectLockMode((String) config.getConfigProperty(S3_OBJECT_LOCK_MODE))
                            .tagging((String) config.getConfigProperty(S3_TAGGING))
                            .serverSideEncryption((String) config.getConfigProperty(S3_SERVER_SIDE_ENCRYPTION))
                            .storageClass((String) config.getConfigProperty(S3_STORAGE_CLASS))
                            .ssekmsKeyId((String) config.getConfigProperty(S3_SSE_KMS_KEY_ID))
                            .sseCustomerAlgorithm((String) config.getConfigProperty(S3_SSE_CUSTOMER_ALGORITHM))
                            .sseCustomerKey((String) config.getConfigProperty(S3_SSE_CUSTOMER_KEY))
                            .sseCustomerKeyMD5((String) config.getConfigProperty(S3_SSE_CUSTOMER_KEY_MD5))
                            .ssekmsEncryptionContext((String) config.getConfigProperty(S3_KMS_ENCRYPTION_CONTEXT))
                            .build();

                    PutObjectResponse response = s3.putObject(putObjectRequest,
                                                              RequestBody.fromBytes(baos.toByteArray()));

                    log.debug(response.eTag());
                }
                isClosed = true;
            }
        };
    }

}

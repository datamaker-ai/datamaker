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
import ai.datamaker.sink.amazon.AmazonS3OutputSink;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AmazonS3OutputSinkTest {

    private final AmazonS3OutputSink sink = Mockito.spy(AmazonS3OutputSink.class);


    @Test
    void getConfigProperties() {
        assertEquals(29, sink.getConfigProperties().size());
    }

    @Test
    void accept() {
        Arrays.stream(FormatType.values()).forEach(ft -> assertTrue(sink.accept(ft)));
    }

    @Test
    void getOutputStream() throws Exception {
        S3Client s3 = Mockito.mock(S3Client.class);

        doReturn(s3).when(sink).getClient(any(JobConfig.class));
        when(s3.putObject(any(PutObjectRequest.class), any(RequestBody.class))).thenReturn(PutObjectResponse.builder().eTag("success").build());
        // when(sink).getClient(any(JobConfig.class))).thenReturn(s3);

        JobConfig config = new JobConfig();
        config.put(AmazonS3OutputSink.S3_FILE_NAME_PATTERN_PROPERTY, "");
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

        config.remove(AmazonS3OutputSink.S3_EXPIRES.getKey());

        try (OutputStream output = sink.getOutputStream(config)) {
            output.write("hello".getBytes(StandardCharsets.UTF_8));
        }
        ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> requestBodyArgumentCaptor = ArgumentCaptor.forClass(RequestBody.class);

        verify(s3).putObject(putObjectRequestArgumentCaptor.capture(), requestBodyArgumentCaptor.capture());

        RequestBody requestBody = requestBodyArgumentCaptor.getValue();
        assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), requestBody.contentStreamProvider().newStream().readAllBytes());

        PutObjectRequest request = putObjectRequestArgumentCaptor.getValue();
        assertEquals("abc", request.expectedBucketOwner());
        assertEquals("abc", request.aclAsString());
        assertEquals("abc", request.contentEncoding());
        assertEquals("abc", request.contentLanguage());
        assertEquals("abc", request.cacheControl());
        assertEquals("abc", request.bucket());
        assertEquals("abc", request.contentDisposition());
        //assertEquals("abc", request.contentMD5());
        assertEquals("abc", request.contentType());
        assertEquals("abc", request.grantFullControl());
        assertEquals("abc", request.grantRead());
        assertEquals("abc", request.grantWriteACP());
        assertEquals("abc", request.grantReadACP());
        assertEquals("", request.key());
        //assertEquals("abc", request.objectLockLegalHoldStatusAsString());
        assertEquals("abc", request.objectLockModeAsString());
        assertEquals("abc", request.tagging());
        assertEquals("abc", request.serverSideEncryptionAsString());
        assertEquals("abc", request.storageClassAsString());
        assertEquals("abc", request.sseCustomerAlgorithm());
        assertEquals("abc", request.sseCustomerKeyMD5());
        assertEquals("abc", request.sseCustomerKey());
        assertEquals("abc", request.ssekmsEncryptionContext());
    }
}
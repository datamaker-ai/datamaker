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

package ai.datamaker.sink.hadoop;

import ai.datamaker.client.KerberosHttpClient;
import ai.datamaker.generator.FormatType;
import ai.datamaker.model.DataOutputSinkType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.DataOutputSink;
import ai.datamaker.sink.SslCommon;
import ai.datamaker.utils.stream.SendDataOutputStream;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

@Slf4j
@DataOutputSinkType(compressed = true, encrypted = true)
public class WebHdfsOutputSink implements DataOutputSink, SslCommon {

    public static final PropertyConfig HTTP_ENDPOINT =
            new PropertyConfig("hdfs.sink.http.endpoint",
                    "Endpoint URL",
                    PropertyConfig.ValueType.STRING,
                    "",
                    Collections.emptyList());

    public static final PropertyConfig FILE_OUTPUT_PATH_PROPERTY
            = new PropertyConfig("hdfs.sink.output.filename",
            "Output file path",
            PropertyConfig.ValueType.EXPRESSION,
            "\"/tmp\"",
            Collections.emptyList());

    public static final PropertyConfig SECURED_CLUSTER =
            new PropertyConfig("hdfs.sink.use.kerberos",
                    "Kerberized cluster",
                    PropertyConfig.ValueType.BOOLEAN,
                    false,
                    Collections.emptyList());

    public static final PropertyConfig APPEND_MODE =
            new PropertyConfig("hdfs.sink.append",
                    "Append mode",
                    PropertyConfig.ValueType.BOOLEAN,
                    false,
                    Collections.emptyList());

    public static final PropertyConfig HADOOP_HDFS_PRINCIPAL =
            new PropertyConfig("hdfs.sink.hdfs.principal",
                    "Kerberos principal",
                    PropertyConfig.ValueType.STRING,
                    "",
                    Collections.emptyList());

    public static final PropertyConfig HADOOP_HDFS_KEYTAB =
            new PropertyConfig("hdfs.sink.hdfs.keytab",
                    "Kerberos keytab",
                    PropertyConfig.ValueType.STRING,
                    "",
                    Collections.emptyList());

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {

        boolean secured = (boolean) config.getConfigProperty(SECURED_CLUSTER);
        boolean append = (boolean) config.getConfigProperty(APPEND_MODE);
        String principal = (String) config.getConfigProperty(HADOOP_HDFS_PRINCIPAL);
        String keytab = (String) config.getConfigProperty(HADOOP_HDFS_KEYTAB);
        String urlEndpoint = (String) config.getConfigProperty(HTTP_ENDPOINT);
        HttpClient httpClient = secured ? new KerberosHttpClient(principal, keytab, false) : HttpClients.createDefault();

        String path = (String) config.getConfigProperty(FILE_OUTPUT_PATH_PROPERTY);
        // /<PATH>?op=CREATE
        HttpPut request = new HttpPut(urlEndpoint + path + (append ? "?op=APPEND" : "?op=CREATE&overwrite=true"));
        final HttpResponse response = httpClient.execute(request);

        if (response.getStatusLine().getStatusCode() >= 400) {
            throw new IllegalStateException("Error while calling HDFS: " + response.getStatusLine().toString() + " " + EntityUtils.toString(response.getEntity()));
        } else {
            log.debug(EntityUtils.toString(response.getEntity()));
        }

        return new SendDataOutputStream(bytes -> {
            String redirect = response.getFirstHeader("Location").getValue();
            HttpPut writeRequest = new HttpPut(redirect);

            ByteArrayEntity byteArrayEntity = new ByteArrayEntity(bytes);
            writeRequest.setEntity(byteArrayEntity);
            try {
                final HttpResponse writeResponse = httpClient.execute(writeRequest);
                if (writeResponse.getStatusLine().getStatusCode() >= 400) {
                    throw new IllegalStateException("Error while calling HDFS: " + response.getStatusLine().toString() + " " + EntityUtils.toString(response.getEntity()));
                } else {
                    log.debug(EntityUtils.toString(writeResponse.getEntity()));
                }
            } catch (IOException ioe) {
                throw new IllegalStateException(ioe);
            }
        });
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return addDefaultProperties(
                Lists.newArrayList(HTTP_ENDPOINT,
                                  FILE_OUTPUT_PATH_PROPERTY,
                                  APPEND_MODE,
                                  SECURED_CLUSTER,
                                  HADOOP_HDFS_PRINCIPAL,
                                  HADOOP_HDFS_KEYTAB));
    }

//    Authentication
//
//    When security is off, the authenticated user is the username specified in the user.name query parameter. If the user.name parameter is not set, the server may either set the authenticated user to a default web user, if there is any, or return an error response.
//
//    When security is on, authentication is performed by either Hadoop delegation token or Kerberos SPNEGO. If a token is set in the delegation query parameter, the authenticated user is the user encoded in the token. If the delegation parameter is not set, the user is authenticated by Kerberos SPNEGO.

//    Append to a File
//
//    Step 1: Submit a HTTP POST request without automatically following redirects and without sending the file data.
//
//            curl -i -X POST "http://<HOST>:<PORT>/webhdfs/v1/<PATH>?op=APPEND[&buffersize=<INT>]"
//
//
//    The request is redirected to a datanode where the file data is to be appended:
//
//    HTTP/1.1 307 TEMPORARY_REDIRECT
//    Location: http://<DATANODE>:<PORT>/webhdfs/v1/<PATH>?op=APPEND...
//    Content-Length: 0
//
//
//    Step 2: Submit another HTTP POST request using the URL in the Location header with the file data to be appended.
//
//            curl -i -X POST -T <LOCAL_FILE> "http://<DATANODE>:<PORT>/webhdfs/v1/<PATH>?op=APPEND..."
//
//
//    The client receives a response with zero content length:
//
//    HTTP/1.1 200 OK

}

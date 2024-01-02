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
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

@Slf4j
@DataOutputSinkType(compressed = true, encrypted = true)
public class KnoxOuputSink implements DataOutputSink, SslCommon {

    public static final PropertyConfig HTTP_ENDPOINT =
            new PropertyConfig("knox.sink.http.endpoint",
                    "Knox Endpoint URL",
                    PropertyConfig.ValueType.STRING,
                    "",
                    Collections.emptyList());

    public static final PropertyConfig FILE_OUTPUT_PATH_PROPERTY
            = new PropertyConfig("hdfs.sink.output.filename",
            "Output file path",
            PropertyConfig.ValueType.EXPRESSION,
            "\"/tmp\"",
            Collections.emptyList());

    public static final PropertyConfig APPEND_MODE =
            new PropertyConfig("hdfs.sink.append",
                    "Append mode",
                    PropertyConfig.ValueType.BOOLEAN,
                    false,
                    Collections.emptyList());

    public static final PropertyConfig BASIC_AUTH_USERNAME =
            new PropertyConfig("http.sink.authentication.username",
                    "Username",
                    PropertyConfig.ValueType.STRING,
                    "elastic",
                    Collections.emptyList());

    public static final PropertyConfig BASIC_AUTH_PASSWORD =
            new PropertyConfig("http.sink.authentication.password",
                    "Password",
                    PropertyConfig.ValueType.PASSWORD,
                    "",
                    Collections.emptyList());

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {
        boolean append = (boolean) config.getConfigProperty(APPEND_MODE);
        String username = (String) config.getConfigProperty(BASIC_AUTH_USERNAME);
        String password = (String) config.getConfigProperty(BASIC_AUTH_PASSWORD);
        String urlEndpoint = (String) config.getConfigProperty(HTTP_ENDPOINT);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(username, password));

        HttpClient httpClient = HttpClients.createDefault();
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);

        String path = (String) config.getConfigProperty(FILE_OUTPUT_PATH_PROPERTY);
        // /<PATH>?op=CREATE
        HttpPut request = new HttpPut(urlEndpoint + path + (append ? "?op=APPEND" : "?op=CREATE&overwrite=true"));
        final HttpResponse response = httpClient.execute(request, context);

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
                final HttpResponse writeResponse = httpClient.execute(writeRequest, context);
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
        return addDefaultProperties(Lists.newArrayList(HTTP_ENDPOINT,
                                                       FILE_OUTPUT_PATH_PROPERTY,
                                                       APPEND_MODE,
                                                       BASIC_AUTH_USERNAME,
                                                       BASIC_AUTH_PASSWORD));
    }
}

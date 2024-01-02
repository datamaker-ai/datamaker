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

package ai.datamaker.sink.elastic;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.DataOutputSink;
import ai.datamaker.sink.SslCommon;
import ai.datamaker.utils.HttpRetryHandler;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
public class ElasticSearchOutputSink implements DataOutputSink, SslCommon {

    public static final PropertyConfig ELASTICSEARCH_ENDPOINTS =
            new PropertyConfig("elasticsearch.storage.sink.endpoints",
                               "Endpoints (scheme:host:port)",
                               PropertyConfig.ValueType.LIST,
                               Lists.newArrayList("http:localhost:9200"),
                               Collections.emptyList());

    public static final PropertyConfig ELASTICSEARCH_INDEX_NAME =
            new PropertyConfig("elasticsearch.sink.index.name",
                               "Index name",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig ELASTICSEARCH_UPDATE_DOCUMENT =
            new PropertyConfig("elasticsearch.sink.update.doc",
                               "Update document",
                               PropertyConfig.ValueType.BOOLEAN,
                               false,
                               Collections.emptyList());

    public static final PropertyConfig ELASTICSEARCH_DOCUMENT_ID =
            new PropertyConfig("elasticsearch.sink.index.name",
                               "Index name",
                               PropertyConfig.ValueType.EXPRESSION,
                               "#field.id",
                               Collections.emptyList());

    public static final PropertyConfig ELASTICSEARCH_BASIC_AUTH_USERNAME =
            new PropertyConfig("elasticsearch.sink.authentication.username",
                               "Username",
                               PropertyConfig.ValueType.STRING,
                               "elastic",
                               Collections.emptyList());

    public static final PropertyConfig ELASTICSEARCH_BASIC_AUTH_PASSWORD =
            new PropertyConfig("elasticsearch.sink.authentication.password",
                               "Password",
                               PropertyConfig.ValueType.PASSWORD,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig ELASTICSEARCH_RETRY_COUNT =
            new PropertyConfig("elasticsearch.sink.failure.retry.count",
                               "Retry count",
                               PropertyConfig.ValueType.NUMERIC,
                               5,
                               Collections.emptyList());

    @Override
    public boolean accept(FormatType type) {
        return type == FormatType.JSON;
    }

    @Override
    public OutputStream getOutputStream(final JobConfig config) throws Exception {

        String username = (String) config.getConfigProperty(ELASTICSEARCH_BASIC_AUTH_USERNAME);
        String password = (String) config.getConfigProperty(ELASTICSEARCH_BASIC_AUTH_PASSWORD);
        List<String> endpoints = (List<String>) config.getConfigProperty(ELASTICSEARCH_ENDPOINTS);
        final List<HttpHost> httpHosts = endpoints.stream().map(e -> {
            String[] configs = e.split(":");
            return new HttpHost(configs[1], Integer.parseInt(configs[2]), configs[0]);
        }).collect(Collectors.toList());

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(username, password));

        final CloseableHttpClient httpClient = HttpClients
                .custom()
                .setRetryHandler(new HttpRetryHandler())
                .build();

        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
//        context.setAuthSchemeRegistry(authRegistry);
//        context.setAuthCache(authCache);

        return new OutputStream() {

            private ByteArrayOutputStream baos = new ByteArrayOutputStream();
            private boolean closed = false;

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
                if (!closed) {
                    sendData();
                }
            }

            @Override
            public void close() throws IOException {
                if (!closed && baos.size() > 0) {
                    sendData();
                }
                httpClient.close();
                closed = true;
            }

            // TODO supports PUT
            private void sendData() throws IOException {
                String indexName = (String) config.getConfigProperty(ELASTICSEARCH_INDEX_NAME);
//                IndexRequest request = new IndexRequest(indexName, XContentType.JSON.mediaType());
                // found id in string...
                //request.id("1");
                StringEntity entity = new StringEntity(baos.toString(),
                                                       ContentType.create("application/json", Consts.UTF_8));
                String uri = httpHosts.get(ThreadLocalRandom.current().nextInt(0, httpHosts.size())).toURI();
                HttpPost httpPost = new HttpPost(uri + "/" + indexName + "/_doc/");
                httpPost.setEntity(entity);
                CloseableHttpResponse response = httpClient.execute(httpPost, context);
                if (response.getStatusLine().getStatusCode() >= 300) {
                    log.error("Error while indexing document: {}", EntityUtils.toString(response.getEntity()));
                }

                baos.reset();
            }
        };
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return addDefaultProperties(
                Lists.newArrayList(ELASTICSEARCH_ENDPOINTS,
                                  ELASTICSEARCH_INDEX_NAME,
                                  ELASTICSEARCH_BASIC_AUTH_USERNAME,
                                  ELASTICSEARCH_BASIC_AUTH_PASSWORD,
                                  ELASTICSEARCH_RETRY_COUNT));
    }

}

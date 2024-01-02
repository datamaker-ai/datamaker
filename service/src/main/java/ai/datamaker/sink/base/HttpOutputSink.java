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

package ai.datamaker.sink.base;

import ai.datamaker.client.KerberosHttpClient;
import ai.datamaker.generator.FormatType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.SupportedMediaType;
import ai.datamaker.sink.DataOutputSink;
import ai.datamaker.sink.SslCommon;
import ai.datamaker.utils.stream.SendDataOutputStream;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Support SSL
 * Support custom headers
 * Support Basic Auth
 * Support oAuth2
 */
@Slf4j
public class HttpOutputSink implements DataOutputSink, SslCommon {

    public static final PropertyConfig HTTP_ENDPOINT =
            new PropertyConfig("hdfs.sink.http.endpoint",
                               "Endpoint URL",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig HTTP_QUERY_NAME =
            new PropertyConfig("http.sink.query.name",
                               "Http query names",
                               PropertyConfig.ValueType.LIST,
                               Collections.emptyList(),
                               Collections.emptyList());

    public static final PropertyConfig HTTP_QUERY_VALUE =
            new PropertyConfig("http.sink.query.values",
                               "Http query values (support expression)",
                               PropertyConfig.ValueType.LIST,
                               Collections.emptyList(),
                               Collections.emptyList());

    public static final PropertyConfig HTTP_HEADERS_NAME =
            new PropertyConfig("http.sink.headers.name",
                               "Http header names",
                               PropertyConfig.ValueType.LIST,
                               Collections.emptyList(),
                               Collections.emptyList());

    public static final PropertyConfig HTTP_HEADERS_VALUE =
            new PropertyConfig("http.sink.headers.values",
                               "Http header values (support expression)",
                               PropertyConfig.ValueType.LIST,
                               Collections.emptyList(),
                               Collections.emptyList());

    public static final PropertyConfig HTTP_FORM_PARAMETERS_NAME =
            new PropertyConfig("http.sink.parameters.name",
                               "Form parameter names",
                               PropertyConfig.ValueType.LIST,
                               Collections.emptyList(),
                               Collections.emptyList());

    public static final PropertyConfig HTTP_FORM_PARAMETERS_VALUE =
            new PropertyConfig("http.sink.parameters.values",
                               "Form parameter values (support expression)",
                               PropertyConfig.ValueType.LIST,
                               Collections.emptyList(),
                               Collections.emptyList());

    public static final PropertyConfig HTTP_FORM_PARAMETERS_TYPE =
            new PropertyConfig("http.sink.parameters.types",
                               "Form parameter types (binary or text)",
                               PropertyConfig.ValueType.LIST,
                               Collections.emptyList(),
                               Collections.emptyList());

    public static final PropertyConfig HTTP_METHOD =
            new PropertyConfig("http.sink.method",
                               "Method",
                               PropertyConfig.ValueType.STRING,
                               "POST",
                               Lists.newArrayList("PATCH", "PUT", "POST"));

    public static final PropertyConfig HTTP_CONTENT_TYPE =
            new PropertyConfig("http.sink.content.type",
                               "Content type",
                               PropertyConfig.ValueType.EXPRESSION,
                               "",
                               Collections.EMPTY_LIST);

    public static final PropertyConfig HTTP_PAYLOAD =
            new PropertyConfig("http.sink.payload",
                               "Payload",
                               PropertyConfig.ValueType.STRING,
                               "BODY",
                               Lists.newArrayList("BODY", "MULTIPART"));

    public static final PropertyConfig HTTP_AUTHENTICATION =
            new PropertyConfig("http.sink.authentication",
                               "Authentication method",
                               PropertyConfig.ValueType.STRING,
                               "NONE",
                               Lists.newArrayList("BASIC", "KERBEROS", "NONE", "SPNEGO"));

    public static final PropertyConfig HTTP_BASIC_AUTH_USERNAME =
            new PropertyConfig("http.sink.authentication.username",
                               "Username",
                               PropertyConfig.ValueType.STRING,
                               "elastic",
                               Collections.emptyList());

    public static final PropertyConfig HTTP_BASIC_AUTH_PASSWORD =
            new PropertyConfig("http.sink.authentication.password",
                               "Password",
                               PropertyConfig.ValueType.PASSWORD,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig KERBEROS_PRINCIPAL =
            new PropertyConfig("http.sink.kerberos.principal",
                               "Kerberos principal",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig KERBEROS_KEYTAB =
            new PropertyConfig("http.sink.kerberos.keytab",
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

        final Map<String, String> params = Maps.newHashMap();
        if (config.containsKey(HTTP_QUERY_NAME.getKey())) {
            List<String> paramNames = (List<String>) config.getConfigProperty(HTTP_QUERY_NAME);
            List<String> paramValues = (List<String>) config.getConfigProperty(HTTP_QUERY_VALUE);
            Assert.isTrue(paramNames.size() == paramValues.size(), "Number of params names and values should match");
            for (int i=0; i<paramNames.size(); i++) {
                params.put(paramNames.get(i), String.valueOf(parseExpression(paramValues.get(i), config)));
            }
        }

        final Map<String, String> headers = Maps.newHashMap();
        if (config.containsKey(HTTP_HEADERS_NAME.getKey())) {
            List<String> headerNames = (List<String>) config.getConfigProperty(HTTP_HEADERS_NAME);
            List<String> headerValues = (List<String>) config.getConfigProperty(HTTP_HEADERS_VALUE);
            Assert.isTrue(headerNames.size() == headerValues.size(), "Number of header names and values should match");
            for (int i=0; i<headerNames.size(); i++) {
                headers.put(headerNames.get(i), String.valueOf(parseExpression(headerValues.get(i), config)));
            }
        }

        String urlEndpoint = (String) config.getConfigProperty(HTTP_ENDPOINT);

        String principal = (String) config.getConfigProperty(KERBEROS_PRINCIPAL);
        String keytab = (String) config.getConfigProperty(KERBEROS_KEYTAB);
        String username = (String) config.getConfigProperty(HTTP_BASIC_AUTH_USERNAME);
        String password = (String) config.getConfigProperty(HTTP_BASIC_AUTH_PASSWORD);
        String authentication = (String) config.getConfigProperty(HTTP_AUTHENTICATION);

        final HttpClient httpClient = "KERBEROS".equals(authentication) || "SPNEGO".equals(authentication)
                ? new KerberosHttpClient(principal, keytab, false)
                : HttpClients.createDefault();

        return new SendDataOutputStream(bytes -> {
            SupportedMediaType supportedMediaType = SupportedMediaType.valueOf(config.getGenerateDataJob().getGenerator().getDataType().toString());
            String contentType = (String) config.getConfigProperty(HTTP_CONTENT_TYPE);
            if (StringUtils.isBlank(contentType)) {
                contentType = supportedMediaType.getMediaTypes().iterator().next();
            }

            HttpClientContext context = null;
            if (authentication.equals("BASIC")) {

                // Create AuthCache instance
                AuthCache authCache = new BasicAuthCache();
// Generate BASIC scheme object and add it to the local auth cache
                BasicScheme basicAuth = new BasicScheme();
                HttpHost targetHost = new HttpHost("localhost", 8080, "http");
                authCache.put(targetHost, basicAuth);

                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                                                   new UsernamePasswordCredentials(username, password));

                context = HttpClientContext.create();
                context.setCredentialsProvider(credentialsProvider);
                context.setAuthCache(authCache);
            }

            String payload = (String) config.getConfigProperty(HTTP_PAYLOAD);
            HttpEntity entity = payload.equals("BODY") ? new ByteArrayEntity(bytes, ContentType.create(contentType)) : createEntity(config, bytes);
            String queryParams = "?" + params.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&"));

            String method = (String) config.getConfigProperty(HTTP_METHOD);
            HttpUriRequest request = null;
            switch (method) {
                case "PUT":
                    HttpPut httpPut = new HttpPut(urlEndpoint + queryParams);
                    httpPut.setEntity(entity);
                    headers.forEach(httpPut::addHeader);
                    request = httpPut;
                    break;
                case "PATCH":
                    HttpPatch httpPatch = new HttpPatch(urlEndpoint + queryParams);
                    httpPatch.setEntity(entity);
                    headers.forEach(httpPatch::addHeader);
                    request = httpPatch;
                    break;
                case "POST":
                    HttpPost httpPost = new HttpPost(urlEndpoint + queryParams);
                    httpPost.setEntity(entity);
                    headers.forEach(httpPost::addHeader);
                    request = httpPost;
                    break;
            }
            try {
                HttpResponse response = httpClient.execute(request, context);
                if (response.getStatusLine().getStatusCode() >= 300) {
                    log.error("Error while indexing document: {}", EntityUtils.toString(response.getEntity()));
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    private HttpEntity createEntity(JobConfig config, byte[] bytes) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        if (config.containsKey(HTTP_FORM_PARAMETERS_NAME.getKey())) {
            List<String> paramNames = (List<String>) config.getConfigProperty(HTTP_FORM_PARAMETERS_NAME);
            List<String> paramValues = (List<String>) config.getConfigProperty(HTTP_FORM_PARAMETERS_VALUE);
            List<String> paramTypes = (List<String>) config.getConfigProperty(HTTP_FORM_PARAMETERS_TYPE);

            Assert.isTrue(paramNames.size() == paramValues.size(), "Number of params names and values should match");
            for (int i=0; i<paramNames.size(); i++) {
                String type = paramTypes.get(i);
                if ("text".equals(type)) {
                    StringBody stringBody = new StringBody(String.valueOf(parseExpression(paramValues.get(i), config)),
                                                           ContentType.MULTIPART_FORM_DATA);
                    builder.addPart(paramNames.get(i), stringBody);
                } else {
                    SupportedMediaType supportedMediaType = SupportedMediaType.valueOf(config.getGenerateDataJob().getGenerator().getDataType().toString());

                    ByteArrayBody byteArrayBody = new ByteArrayBody(bytes,
                                                                    paramNames.get(i) + "." + supportedMediaType.getFileExtensions().iterator().next());
                    builder.addPart(paramNames.get(i), byteArrayBody);
                }
            }
        }

        return builder.build();
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return addDefaultProperties(
                Lists.newArrayList(HTTP_ENDPOINT,
                                   HTTP_METHOD,
                                   HTTP_PAYLOAD,
                                   HTTP_CONTENT_TYPE,
                                   HTTP_AUTHENTICATION,
                                   HTTP_QUERY_NAME,
                                   HTTP_QUERY_VALUE,
                                   HTTP_HEADERS_NAME,
                                   HTTP_HEADERS_VALUE,
                                   HTTP_FORM_PARAMETERS_NAME,
                                   HTTP_FORM_PARAMETERS_VALUE,
                                   HTTP_FORM_PARAMETERS_TYPE,
                                   HTTP_BASIC_AUTH_USERNAME,
                                   HTTP_BASIC_AUTH_PASSWORD,
                                   KERBEROS_PRINCIPAL,
                                   KERBEROS_KEYTAB));
    }
}

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

package ai.datamaker.client;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class KerberosHttpClient implements HttpClient {
    private String principal;
    private String keyTabLocation;
    private HttpClient spnegoHttpClient = buildSpnegoHttpClient();

    public KerberosHttpClient() {
    }

    public KerberosHttpClient(String principal, String keyTabLocation) {
        super();
        this.principal = principal;
        this.keyTabLocation = keyTabLocation;
    }

    public KerberosHttpClient(String principal, String keyTabLocation, String krb5Location) {
        this(principal, keyTabLocation);
        System.setProperty("java.security.krb5.conf", krb5Location);
    }

    public KerberosHttpClient(String principal, String keyTabLocation, boolean isDebug) {
        this(principal, keyTabLocation);
        if (isDebug) {
            System.setProperty("sun.security.spnego.debug", "true");
            System.setProperty("sun.security.krb5.debug", "true");
        }
    }

    public KerberosHttpClient(String principal, String keyTabLocation, String krb5Location, boolean isDebug) {
        this(principal, keyTabLocation, isDebug);        
        System.setProperty("java.security.krb5.conf", krb5Location);
    }

    private static HttpClient buildSpnegoHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        Lookup<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create().
                register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true)).build();
        builder.setDefaultAuthSchemeRegistry(authSchemeRegistry);
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(null, -1, null), new Credentials() {
            @Override
            public Principal getUserPrincipal() {
                return null;
            }
            @Override
            public String getPassword() {
                return null;
            }
        });
        builder.setDefaultCredentialsProvider(credentialsProvider);
        return builder.build();
    }

    @Override
    public HttpParams getParams() {
        return spnegoHttpClient.getParams();
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return spnegoHttpClient.getConnectionManager();
    }

    @Override
    public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
        log.debug("Calling KerberosHttpClient {} {} {}", this.principal, this.keyTabLocation, request.getURI().toString());
        Configuration config = new Configuration() {
            @SuppressWarnings("serial")
            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                return new AppConfigurationEntry[] { new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, new HashMap<>() {
                    {
                        put("useTicketCache", "false");
                        put("useKeyTab", "true");
                        put("keyTab", keyTabLocation);
                        //Krb5 in GSS API needs to be refreshed so it does not throw the error
                        //Specified version of key is not available
                        put("refreshKrb5Config", "true");
                        put("principal", principal);
                        put("storeKey", "true");
                        put("doNotPrompt", "true");
                        put("isInitiator", "true");
                        put("debug", "true");
                    }
                }) };
            }
        };
        Set<Principal> princ = new HashSet<>(1);
        princ.add(new KerberosPrincipal(principal));
        Subject sub = new Subject(false, princ, new HashSet<>(), new HashSet<>());
        try {
            LoginContext lc = new LoginContext("", sub, null, config);
            lc.login();
            Subject serviceSubject = lc.getSubject();
            return Subject.doAs(serviceSubject, new PrivilegedAction<>() {
                HttpResponse httpResponse = null;

                @Override
                public HttpResponse run() {
                    try {
                        httpResponse = spnegoHttpClient.execute(request);
                        return httpResponse;
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                    return httpResponse;
                }
            });
        } catch (LoginException le) {
            throw new IllegalStateException(le);
        }
    }

    @Override
    public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException, ClientProtocolException {
        return execute(request);
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
        return execute((HttpUriRequest) request);
    }

    @Override
    public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
        return execute((HttpUriRequest) request);
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return null;
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
        return null;
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return null;
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
        return null;
    }
}
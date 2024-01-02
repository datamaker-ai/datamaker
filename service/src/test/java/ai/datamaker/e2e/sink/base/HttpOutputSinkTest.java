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

package ai.datamaker.e2e.sink.base;

import ai.datamaker.generator.JsonGenerator;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.job.GenerateDataJob;
import ai.datamaker.sink.base.HttpOutputSink;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;

@Disabled
class HttpOutputSinkTest {

    @Test
    void accept() {
    }

    // while true; do { echo -e 'HTTP/1.1 200 OK\r\n'; } | nc -l 8080; done
    @Test
    void getOutputStream() throws Exception {
        JobConfig config = new JobConfig();
        GenerateDataJob dataJob = new GenerateDataJob();
        dataJob.setGenerator(new JsonGenerator());
        config.setGenerateDataJob(dataJob);
        HttpOutputSink sink = new HttpOutputSink();
        config.put(HttpOutputSink.HTTP_AUTHENTICATION, "BASIC");
        config.put(HttpOutputSink.HTTP_BASIC_AUTH_USERNAME, "admin");
        config.put(HttpOutputSink.HTTP_BASIC_AUTH_PASSWORD, "changeme");

        config.put(HttpOutputSink.HTTP_ENDPOINT.getKey(), "http://localhost:8080");
        config.put(HttpOutputSink.HTTP_PAYLOAD.getKey(), "MULTIPART");
        config.put(HttpOutputSink.HTTP_METHOD.getKey(), "POST");

        config.put(HttpOutputSink.HTTP_QUERY_NAME, Lists.newArrayList("query_name"));
        config.put(HttpOutputSink.HTTP_QUERY_VALUE, Lists.newArrayList("'query_value'"));

        config.put(HttpOutputSink.HTTP_FORM_PARAMETERS_VALUE, Lists.newArrayList("'test_value'"));
        config.put(HttpOutputSink.HTTP_FORM_PARAMETERS_NAME, Lists.newArrayList("test_name"));
        config.put(HttpOutputSink.HTTP_FORM_PARAMETERS_TYPE, Lists.newArrayList("file"));
        config.put(HttpOutputSink.HTTP_HEADERS_NAME.getKey(), Lists.newArrayList("header1"));
        config.put(HttpOutputSink.HTTP_HEADERS_VALUE.getKey(), Lists.newArrayList("'/test.bz2'"));

        try (OutputStream stream = sink.getOutputStream(config)) {
            stream.write("test 123".getBytes());
        }
    }

    @Test
    void getOutputStream_basic() throws Exception {
        JobConfig config = new JobConfig();
        GenerateDataJob dataJob = new GenerateDataJob();
        dataJob.setGenerator(new JsonGenerator());
        config.setGenerateDataJob(dataJob);
        HttpOutputSink sink = new HttpOutputSink();
        config.put(HttpOutputSink.HTTP_AUTHENTICATION, "BASIC");
        config.put(HttpOutputSink.HTTP_BASIC_AUTH_USERNAME, "guest");
        config.put(HttpOutputSink.HTTP_BASIC_AUTH_PASSWORD, "guest");

        config.put(HttpOutputSink.HTTP_ENDPOINT.getKey(), "http://postman-echo.com/post");
        config.put(HttpOutputSink.HTTP_PAYLOAD.getKey(), "BODY");
        config.put(HttpOutputSink.HTTP_METHOD.getKey(), "POST");

        config.put(HttpOutputSink.HTTP_QUERY_NAME, Lists.newArrayList("query_name"));
        config.put(HttpOutputSink.HTTP_QUERY_VALUE, Lists.newArrayList("'query_value'"));

        config.put(HttpOutputSink.HTTP_FORM_PARAMETERS_VALUE, Lists.newArrayList("'test_value'"));
        config.put(HttpOutputSink.HTTP_FORM_PARAMETERS_NAME, Lists.newArrayList("test_name"));
        config.put(HttpOutputSink.HTTP_FORM_PARAMETERS_TYPE, Lists.newArrayList("file"));
        config.put(HttpOutputSink.HTTP_HEADERS_NAME.getKey(), Lists.newArrayList("header1"));
        config.put(HttpOutputSink.HTTP_HEADERS_VALUE.getKey(), Lists.newArrayList("'/test.bz2'"));

        try (OutputStream stream = sink.getOutputStream(config)) {
            stream.write("test 123".getBytes());
        }
    }


}
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

package ai.datamaker.e2e.sink.elastic;

import ai.datamaker.model.JobConfig;
import ai.datamaker.sink.elastic.ElasticSearchOutputSink;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.util.List;

@Disabled
class ElasticSearchOutputSinkTest {

    private ElasticSearchOutputSink sink = new ElasticSearchOutputSink();

    @Test
    void accept() {
    }

    @Test
    void getOutputStream() throws Exception {
        JobConfig config = new JobConfig();
        List<String> endpoints = Lists.newArrayList("https:2c514a06a9b240aaab104946136f8413.northamerica-northeast1.gcp.elastic-cloud.com:9243");
        config.put(ElasticSearchOutputSink.ELASTICSEARCH_ENDPOINTS, endpoints);
        config.put(ElasticSearchOutputSink.ELASTICSEARCH_INDEX_NAME, "posts");
        config.put(ElasticSearchOutputSink.ELASTICSEARCH_BASIC_AUTH_USERNAME, "elastic");
        config.put(ElasticSearchOutputSink.ELASTICSEARCH_BASIC_AUTH_PASSWORD, "x9YGmR5xzCx75IngiptLBpJn");

        try (OutputStream outputStream = sink.getOutputStream(config)) {
            outputStream.write("{\"name\":\"Hello world!\"}".getBytes());
        }

        // wait for it...
        //Thread.currentThread().join();
    }

    @Test
    void getConfigProperties() {
    }
}
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

package ai.datamaker.model;

import static org.junit.jupiter.api.Assertions.*;

import ai.datamaker.model.Constants;
import ai.datamaker.model.JobConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class JobConfigTest {

    @Test
    void testSerDe() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        JobConfig jobConfig = new JobConfig();
        jobConfig.put(Constants.INPUT_FILENAME_KEY, "file.json");

        String value = objectMapper.writeValueAsString(jobConfig);

        JobConfig deserialized = objectMapper.readValue(value, JobConfig.class);

        assertEquals("file.json", deserialized.getProperty(Constants.INPUT_FILENAME_KEY));
    }

    @Test
    void getProperty() {
    }

    @Test
    void testGetProperty() {
    }
}
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

package ai.datamaker.integration.processor;

import ai.datamaker.generator.JsonGenerator;
import ai.datamaker.model.Dataset;
import ai.datamaker.processor.JsonSchemaProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("integration")
//@ImportAutoConfiguration(classes = {SpringTestConfiguration.class})
public class JsonSchemaIntegrationTest {

    @Autowired
    private JsonSchemaProcessor jsonSchemaProcessor;

    private JsonGenerator jsonGenerator = new JsonGenerator();

    @Test
    void testJsonProcessorGenerator() throws Exception {
        InputStream input = getClass().getClassLoader().getResourceAsStream("veggies.json");

        Optional<Dataset> datasetOptional = jsonSchemaProcessor.process(input);

        assertTrue(datasetOptional.isPresent());

        jsonGenerator.generate(datasetOptional.get(), System.out);
    }
}

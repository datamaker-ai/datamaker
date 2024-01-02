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

package ai.datamaker.processor;

import ai.datamaker.model.Dataset;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

class JsonSchemaProcessorTest extends AbstractDatasetProcessorTest {

    protected JsonSchemaProcessorTest() {
        super(new JsonSchemaProcessor());
    }

    @Test
    void process() {
        InputStream input = getClass().getClassLoader().getResourceAsStream("veggies.json");
        doAnswer(invocationOnMock -> invocationOnMock.getArgument(1)).when(fieldDetectorService).findBestMatch(any(), any());

        Optional<Dataset> datasetOptional = datasetProcessor.process(input);
        assertTrue(datasetOptional.isPresent());
        Dataset dataset = datasetOptional.get();

        System.out.println(dataset);
    }

    @Test
    void supportedTypes() {
    }

    @Test
    void getConfigProperties() {
    }
}
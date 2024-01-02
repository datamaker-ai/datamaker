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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.SupportedMediaType;
import ai.datamaker.utils.schema.AvroSchemaConverter;

import java.io.InputStream;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

class AvroProcessorTest extends AbstractDatasetProcessorTest {

    protected AvroProcessorTest() {
        super(new AvroProcessor());
    }

    @Test
    void process() {
        AvroSchemaConverter schemaConverter = Mockito.mock(AvroSchemaConverter.class);
        when(schemaConverter.convertFrom(any(), any())).thenReturn(new Dataset());

        ReflectionTestUtils.setField(datasetProcessor, "schemaConverter", schemaConverter);
        InputStream input = getClass().getClassLoader().getResourceAsStream("userdata1.avro");
        //InputStream input = getClass().getClassLoader().getResourceAsStream("file-bzip2.avro");

        Optional<Dataset> dataset = datasetProcessor.process(input);
        assertTrue(dataset.isPresent());
    }

    @Test
    void supportedTypes() {
        assertTrue(datasetProcessor.supportedTypes().contains(SupportedMediaType.AVRO));
    }
}
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

import ai.datamaker.exception.DatasetSerializationException;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.SupportedMediaType;
import ai.datamaker.model.field.type.IntegerField;
import ai.datamaker.model.field.type.StringField;
import com.google.common.collect.Sets;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/***
 * 1 byte  = record type
 * 4 bytes = record length
 * followed by record content
 */
public class BinaryProcessorTest extends DatasetProcessor {
    @Override
    public List<PropertyConfig> getConfigProperties() {
        return null;
    }

    @Override
    public Optional<Dataset> process(InputStream input, JobConfig config) {
        Locale locale = getLocale(config);
        String datasetName = (String)config.getConfigProperty(INPUT_FILENAME_PROPERTY);

        Dataset dataset = new Dataset(datasetName,locale);

        try {
            int position = 0;
            while (true) {
                int type = input.read();
                position += 1;
                byte[] lengthBuffer = new byte[4];
                int r = input.read(lengthBuffer, 0, 4);
                position += 3;
                int length = ByteBuffer.wrap(lengthBuffer).getInt();
                byte[] content = new byte[length];
                int result = input.read(content, 0, length);
                position += length;
                if (type == 1) {
                    IntegerField integerField = new IntegerField(new String(content), locale);
                    dataset.addField(integerField);
                } else if (type == 2) {
                    StringField stringField = new StringField(new String(content), locale);
                    dataset.addField(stringField);
                }
                if (result == -1) {
                    break;
                }
            }

        } catch (IOException e) {
            throw new DatasetSerializationException("invalid data", e, dataset);
        }

        return Optional.of(dataset);
    }

    @Override
    public Set<SupportedMediaType> supportedTypes() {
        return Sets.newHashSet(SupportedMediaType.BINARY);
    }

    public static void main(String[] args) {
        byte[] arr = { 1, 0, 0, 0, 2, 56, 66, 7, 8 };

        BinaryProcessorTest test = new BinaryProcessorTest();
        ByteArrayInputStream bais = new ByteArrayInputStream(arr);
        Optional<Dataset> dataset = test.process(bais);

        System.out.println(dataset.get());
    }
}

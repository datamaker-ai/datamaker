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

package ai.datamaker.generator;

import ai.datamaker.exception.DatasetSerializationException;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.PropertyConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * Generator that write values directly to the output stream as is without conversion.
 */
public class BytesGenerator implements DataGenerator {

    @Override
    public void generate(Dataset dataset, OutputStream out) throws Exception {
        dataset.processAllValues(fieldValues -> {
            fieldValues.forEach(fv -> {
                try {

                    Object value = fv.getValue();
                    if (value instanceof byte[]) {
                        out.write((byte[]) value);
                    } else {
                        out.write(String.valueOf(value).getBytes());
                    }
                    //out.write(fv.getValue());
                    if (dataset.getFlushOnEveryRecord()) {
                        out.flush();
                    }
                } catch (IOException e) {
                    throw new DatasetSerializationException("Error while serializing data", e, dataset);
                }
            });

        });

    }

    @Override
    public FormatType getDataType() {
        return FormatType.BYTES;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Collections.emptyList();
    }
}

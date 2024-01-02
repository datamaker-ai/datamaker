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
import ai.datamaker.model.field.SimpleFieldValue;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generator that send back the original dataset and generated values as is without conversion.
 */
public class PassthroughGenerator implements DataGenerator {

    @Override
    public void generate(Dataset dataset, OutputStream outputStream) throws Exception {
        List<List<SimpleFieldValue>> accumulator = Lists.newArrayList();
        dataset.processAllValues(fieldValues -> {
            try {
                List<SimpleFieldValue> record = fieldValues
                        .stream()
                        .map(fv -> SimpleFieldValue.of(fv.getField(), fv.getValue()))
                        .collect(Collectors.toList());

                if (dataset.getFlushOnEveryRecord()) {
                    ObjectOutputStream out = new ObjectOutputStream(outputStream);
                    out.writeObject(record);
                    out.flush();
                } else {
                    accumulator.add(record);
                }
            } catch (IOException e) {
                throw new DatasetSerializationException("Error while serializing data", e, dataset);
            }
        });

        if (!dataset.getFlushOnEveryRecord() && CollectionUtils.isNotEmpty(accumulator)) {
            ObjectOutputStream out = new ObjectOutputStream(outputStream);
            out.writeObject(accumulator);
        }
    }

    @Override
    public FormatType getDataType() {
        return FormatType.OBJECT;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Collections.emptyList();
    }
}

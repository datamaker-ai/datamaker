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

package ai.datamaker.sink;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.field.SimpleFieldValue;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ObjectOutputSink implements DataOutputSink {

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {
        return new OutputStream() {

            private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            @Override
            public void write(int b) throws IOException {
                byteArrayOutputStream.write(b);
            }

            @SneakyThrows
            @Override
            public void flush() throws IOException {
                InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                if (config.getDataset().getFlushOnEveryRecord()) {
                    List<SimpleFieldValue> record = getRecord(inputStream);
                    record.forEach(fieldValue -> log.debug(fieldValue.getFieldName() + ": " + fieldValue.getValue().toString()));
                }
                byteArrayOutputStream.reset();
            }

            @SneakyThrows
            @Override
            public void close() throws IOException {
                InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
                if (!config.getDataset().getFlushOnEveryRecord() && byteArrayOutputStream.size() > 0) {
                    List<List<SimpleFieldValue>> records = getRecords(inputStream);
                    records.forEach(fieldValue -> log.debug(String.valueOf(fieldValue)));
                }

                byteArrayOutputStream.reset();
            }

        };
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Collections.emptyList();
    }
}

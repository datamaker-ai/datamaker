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
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.SupportedMediaType;
import ai.datamaker.utils.schema.AvroSchemaConverter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Process Avro file. Extract header from {@link Schema} and convert it to a usable {@link Dataset}.
 */
@Component
@Slf4j
public class AvroProcessor extends DatasetProcessor {

    @Autowired
    private AvroSchemaConverter schemaConverter;

    @Override
    public Optional<Dataset> process(InputStream input, JobConfig config) {

        DatumReader<GenericRecord> datumReader = new GenericDatumReader<>();

        try (DataFileStream<GenericRecord> dataFileStream = new DataFileStream<>(input, datumReader)) {
            Schema schema = dataFileStream.getSchema();
            // TODO process data to find constraints
            if (dataFileStream.hasNext()) {
                GenericRecord record = dataFileStream.next();
                schema.getFields().forEach(f -> log.debug(f.name() + " " + record.get(f.name())));
            }
            Locale locale = getLocale(config);

            return Optional.of(schemaConverter.convertFrom(schema, locale));

        } catch (IOException e) {
            log.error("Error while processing Avro file", e);
            throw new IllegalStateException("Error while processing Avro file", e);
        }

        //return Optional.empty();
    }

    @Override
    public Set<SupportedMediaType> supportedTypes() {
        return Sets.newHashSet(SupportedMediaType.AVRO);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(LOCALE_PROPERTY);
    }
}

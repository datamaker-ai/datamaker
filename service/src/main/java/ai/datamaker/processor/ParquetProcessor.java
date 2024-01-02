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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.InputFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@Slf4j
public class ParquetProcessor extends DatasetProcessor {

    @Autowired
    private AvroSchemaConverter schemaConverter;

    @Override
    public Optional<Dataset> process(InputStream input, JobConfig config) {

        try {
            File tempParquetFile = File.createTempFile("temp-", ".parquet");
            Files.copy(input, tempParquetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Configuration conf = new Configuration();

            Path path = new Path(tempParquetFile.toURI());
            InputFile inputFile = HadoopInputFile.fromPath(path, conf);

            //ParquetFileReader parquetFileReader = ParquetFileReader.open(inputFile);
            ParquetReader<GenericRecord> parquetReader = AvroParquetReader.<GenericRecord>builder(inputFile).build();

            Schema schema = parquetReader.read().getSchema();
            Locale locale = getLocale(config);

            return Optional.of(schemaConverter.convertFrom(schema, locale));

            //Schema schema = parquetFileReader.getFileMetaData().getSchema().get;

        } catch (IOException e) {
            log.error("Error while processing Parquet file", e);
            throw new IllegalStateException("Error while processing Parquet file", e);
        }
    }

    @Override
    public Set<SupportedMediaType> supportedTypes() {
        return Sets.newHashSet(SupportedMediaType.PARQUET);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(LOCALE_PROPERTY);
    }
}

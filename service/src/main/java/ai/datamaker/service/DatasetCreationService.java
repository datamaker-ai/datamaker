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

package ai.datamaker.service;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.SupportedMediaType;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.processor.DatasetProcessor;
import ai.datamaker.repository.DatasetRepository;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Dataset creation service
 */
@Service
public class DatasetCreationService {

    @Autowired
    private List<DatasetProcessor> datasetProcessors;

    @Autowired
    private DatasetRepository datasetRepository;

    private final Map<SupportedMediaType, DatasetProcessor> mimeTypeToProcessors = Maps.newHashMap();

    @PostConstruct
    public void init() {
        datasetProcessors.forEach(p -> p.supportedTypes().forEach(s -> mimeTypeToProcessors.put(s, p)));
    }

    public Optional<Dataset> create(String tableMetadata, JobConfig config) {
        return Optional.empty();
    }

    public Optional<Dataset> create(String contentType, String fileExtension, InputStream inputStream, JobConfig config) {

        SupportedMediaType supportedMediaType = SupportedMediaType.from(contentType, fileExtension);
        if (supportedMediaType != null) {
            if (mimeTypeToProcessors.containsKey(supportedMediaType)) {
                // TODO determine properties or infer them?
                Optional<Dataset> dataset = mimeTypeToProcessors.get(supportedMediaType).process(inputStream, config);

                dataset.ifPresent(d -> {
                    List<Field> nestedFields = Lists.newArrayList();
                    d.getFields().forEach(f -> processField(d, f, nestedFields));
                    d.getFields().addAll(nestedFields);
                });

                return dataset;
            }
        }

        // No match found
        return Optional.empty();
    }

    /**
     * Add nested fields to dataset.
     * @param field
     */
    private void processField(Dataset dataset, Field field, List<Field> nestedFields) {
        if (field.getIsNested()) {
            field.setDataset(dataset);
            //dataset.addField(field);
            nestedFields.add(field);
        }
        if (field instanceof ComplexField) {
            ((ComplexField)field).getReferences().forEach(f -> processField(dataset, f, nestedFields));
        } else if (field instanceof ArrayField) {
            processField(dataset, ((ArrayField)field).getReference(), nestedFields);
        }
    }

    public Optional<Dataset> create(String contentType, String fileExtension, InputStream inputStream) {
        return create(contentType, fileExtension, inputStream, JobConfig.EMPTY);
    }
}

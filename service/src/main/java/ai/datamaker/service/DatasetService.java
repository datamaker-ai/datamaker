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
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.field.ContainReference;
import ai.datamaker.model.field.ContainReferences;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.type.ReferenceField;
import ai.datamaker.repository.DatasetRepository;
import ai.datamaker.utils.graph.Graphs;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DatasetService {

    @Autowired
    private DatasetRepository datasetRepository;

    public void delete(Dataset dataset) {
        findReferences(dataset);
        datasetRepository.delete(dataset);
    }

    public void findReferences(Dataset dataset) {

        List<Dataset> datasets = dataset.getWorkspace().getDatasets();
        final List<Field> referenceFields = Lists.newArrayList();

        dataset
            .getFields()
            .stream()
            .filter(Field::getIsPrimaryKey)
            .forEach(pf -> datasets.forEach(d -> d.
                getFields()
                .stream()
                .filter(df -> df instanceof ReferenceField)
                .forEach(rf -> {
                    ReferenceField referenceField = (ReferenceField) rf;
                    if (referenceField.getReference().equals(pf)) {
                        referenceFields.add(referenceField);
                    }
                })));

        if (!referenceFields.isEmpty()) {
            throw new IllegalStateException("Cannot delete dataset because reference fields found: " +
                referenceFields
                    .stream()
                    .map(f -> f.getDataset().getName() + ":" + f.getName()).
                    collect(Collectors.joining(","))
            );
        }
    }

    @Deprecated
    public void injectFieldDependencies(List<Dataset> datasets) {
        Map<UUID, Field> idToField = Maps.newHashMap();

        // TODO test
        // Should get nested levels
        datasets
            .forEach(d -> d
                .getFields()
                .forEach(f -> idToField.put(f.getExternalId(), f))
            );

        idToField.values().forEach(
            f -> {
                Optional<PropertyConfig> refPropertyConfig = f.getConfigProperties()
                    .stream(
                    ).filter(p -> p.getType() == PropertyConfig.ValueType.REFERENCE)
                    .findFirst();
                if (refPropertyConfig.isPresent()) {
                    String referenceUUID = (String) f.getConfig().getConfigProperty(refPropertyConfig.get());
                    if (f instanceof ContainReference && StringUtils.isNotBlank(referenceUUID)) {
                        ContainReference cf = (ContainReference)f;
                        UUID uuid = UUID.fromString(referenceUUID);

                        if (!idToField.containsKey(uuid)) {
                            throw new IllegalStateException("Field reference not found: " + uuid);
                        }
                        cf.setReference(idToField.get(uuid));
                    }
                }

                Optional<PropertyConfig> allRefPropertyConfig = f.getConfigProperties()
                    .stream(
                    ).filter(p -> p.getType() == PropertyConfig.ValueType.REFERENCES)
                    .findFirst();
                if (allRefPropertyConfig.isPresent()) {
                    List<Object> referencesUUID = (List<Object>) f.getConfig().getConfigProperty(allRefPropertyConfig.get());
                    if (f instanceof ContainReferences && CollectionUtils.isNotEmpty(referencesUUID)) {
                        ContainReferences cf = (ContainReferences)f;
                        List<Field> references = referencesUUID
                            .stream()
                            .map(u -> {
                                UUID uuid = UUID.fromString((String)u);

                                if (!idToField.containsKey(uuid)) {
                                    throw new IllegalStateException("Field reference not found: " + uuid);
                                }
                                Field field = idToField.get(uuid);
                                return field;
                            })
                            .collect(Collectors.toList());

                        cf.setReferences(references);
                    }
                }
            }
        );
    }

    public List<Dataset> sortDatasetsPerDependencies(List<Dataset> datasets) {

        //injectFieldDependencies(datasets);
        List<Dataset> sortedDatasets = Lists.newArrayList();

        MutableGraph<Dataset> graph =
            GraphBuilder.directed()
                .allowsSelfLoops(false)
                .build();

        datasets.forEach(d -> {
            Optional<Field> anyPrimary = d.getFields().stream().filter(Field::getIsPrimaryKey).findAny();
            Optional<Field> anyReference = d.getFields().stream().filter(f -> f instanceof ReferenceField).findAny();

            graph.addNode(d);
            if (!anyReference.isEmpty()) {
                ReferenceField referenceField = (ReferenceField) anyReference.get();
                if (referenceField.getReference() != null && referenceField.getReference().getDataset() != null) {
                    graph.putEdge(referenceField.getReference().getDataset(), d);
                }
            }
        });

        sortedDatasets.addAll(Graphs.topologicallySortedNodes(graph));

        return sortedDatasets;
    }
}

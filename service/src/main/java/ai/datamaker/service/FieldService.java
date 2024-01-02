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
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.repository.FieldRepository;
import java.util.UUID;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FieldService {

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private DatasetService datasetService;

    @Deprecated
    public void injectDependencies(Field field) {
        datasetService.injectFieldDependencies(field.getDataset().getWorkspace().getDatasets());
    }

    @Transactional
    public Field getFieldFor(String externalId) {
        Field field = fieldRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        Dataset dataset = field.getDataset();
        return field;
    }

    @Transactional
    public void delete(Field field) {
        if (field instanceof ComplexField) {
            ComplexField complexField = (ComplexField) field;
            if (CollectionUtils.isNotEmpty(complexField.getReferences())) {
                complexField.getReferences().stream().filter(Field::getIsNested).forEach(this::delete);
            }
        } else if (field instanceof ArrayField) {
            ArrayField arrayField = (ArrayField) field;
            if (arrayField.getReference() != null && arrayField.getReference().getIsNested()) {
                delete(arrayField.getReference());
            }
        } else if (field.getIsPrimaryKey()) {
            datasetService.findReferences(field.getDataset());
        }
        // ONLY delete nested
        // FIXME delete primary key or not (check dataset first)
        fieldRepository.delete(field);
    }
}

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

package ai.datamaker.controller;

import ai.datamaker.model.Constants;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.ContainReference;
import ai.datamaker.model.field.ContainReferences;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.forms.FieldForm;
import ai.datamaker.model.forms.MultipleFieldsForm;
import ai.datamaker.model.mapper.FieldMapper;
import ai.datamaker.model.response.ApiResponse;
import ai.datamaker.model.response.ResponseSuccess;
import ai.datamaker.repository.DatasetRepository;
import ai.datamaker.repository.FieldRepository;
import ai.datamaker.service.FieldService;
import ai.datamaker.utils.model.FieldFactory;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@Slf4j
@RequestMapping("/api/field")
public class FieldController extends AbstractRestController {

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private FieldService fieldService;

    @GetMapping("/{externalId}")
    public ResponseEntity<ApiResponse> get(@PathVariable @NotBlank String externalId) {

        Field field = fieldRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        authorize(field.getDataset().getWorkspace(), false);

        return ResponseEntity.ok(ResponseSuccess.builder()
                                         .externalId(field.getExternalId().toString())
                                         .objectType(Constants.FIELD_OBJECT)
                                         .payload(FieldMapper.INSTANCE.fieldToFieldResponse(field))
                                         .build());
    }

    @GetMapping("/{externalId}/complex")
    public ResponseEntity<ApiResponse> getComplexField(@PathVariable @NotBlank String externalId) {

        Field field = fieldRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        Assert.isTrue(field instanceof ComplexField, "Should be a complex field");
        authorize(field.getDataset().getWorkspace(), false);

        return ResponseEntity.ok(ResponseSuccess.builder()
            .externalId(field.getExternalId().toString())
            .objectType(Constants.FIELD_OBJECT)
            .payload(((ComplexField) field)
                .getReferences()
                .stream()
                .sorted(Comparator.comparingInt(Field::getPosition))
                .map(FieldMapper.INSTANCE::fieldToFieldResponse)
                .collect(Collectors.toList()))
            .build());
    }

    @GetMapping("/{externalId}/array")
    public ResponseEntity<ApiResponse> getArrayField(@PathVariable @NotBlank String externalId) {

        Field field = fieldRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        Assert.isTrue(field instanceof ArrayField, "Should be an array field");

        authorize(field.getDataset().getWorkspace(), false);

        ArrayField arrayField = (ArrayField)field;
        if (arrayField.getReference() == null) {
            return ResponseEntity.ok(ResponseSuccess.builder()
                                      .externalId(field.getExternalId().toString())
                                      .objectType(Constants.FIELD_OBJECT)
                                      .payload(Collections.EMPTY_LIST)
                                      .build());
        }

        return ResponseEntity.ok(ResponseSuccess.builder()
            .externalId(field.getExternalId().toString())
            .objectType(Constants.FIELD_OBJECT)
            .payload(Lists.newArrayList(FieldMapper.INSTANCE.fieldToFieldResponse(arrayField.getReference())))
            .build());
    }

//    @GetMapping(path = "/dataset/{datasetId}/fields", produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<ApiResponse> listFields(@PathVariable @NotBlank String datasetId) {
//        Dataset dataset = datasetRepository.findByExternalId(UUID.fromString(datasetId)).orElseThrow();
//        authorize(dataset.getWorkspace(), false);
//
//        return ResponseEntity
//                .ok(ResponseSuccess
//                            .builder()
//                            .objectType(Constants.FIELD_OBJECT)
//                            .externalId(dataset.getExternalId().toString())
//                            .payload(dataset
//                                             .getFields()
//                                             .stream()
//                                             .map(FieldMapper.INSTANCE::fieldToFieldResponse)
//                                             .collect(Collectors.toList())
//                            )
//                            .build());
//    }

    private void validateReferences(Field field) {
        log.debug("Validating dependencies for field: name={}, id={}", field.getName(), field.getExternalId());
        if (field instanceof ContainReference) {
            ContainReference containReference = (ContainReference) field;
            String uuidReference = field.getConfig().getProperty(containReference.getConfigKey());
            //Assert.notNull(uuidReference,
            //    String.format("Reference key should not be null for field: name=%s, id=%s", field.getName(), field.getExternalId()));

            if (uuidReference != null) {
                field.injectDependencies();

                Assert.notNull(containReference.getReference(),
                    String.format("Reference should not be null for field: name=%s, id=%s", field.getName(), field.getExternalId()));
                if (containReference.getReference().getDataset() != null) {
                    authorize(containReference.getReference().getDataset().getWorkspace(), false);
                }
            }
        } else if (field instanceof ContainReferences) {
            ContainReferences containReferences = (ContainReferences) field;

            Collection<String> uuidReferences = (Collection<String>) field.getConfig().get(containReferences.getConfigKey());
            //Assert.isTrue(uuidReferences != null,
             //   String.format("Reference keys should not be empty for field: name=%s, id=%s", field.getName(), field.getExternalId()));

            if (uuidReferences != null) {
                field.injectDependencies();

                Assert.notNull(containReferences.getReferences(),
                    String.format("Reference key should not be null for field: name=%s, id=%s", field.getName(), field.getExternalId()));
                containReferences.getReferences().forEach(r -> {
                    Assert.notNull(r, String.format("Reference should not be null for field: name=%s, id=%s", field.getName(), field.getExternalId()));
                    authorize(r.getDataset().getWorkspace(), false);
                });
            }
        }
    }

    // Create
    @PostMapping
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody FieldForm fieldForm) throws Exception {

        Dataset dataset = datasetRepository.findByExternalId(UUID.fromString(fieldForm.getDatasetId())).orElseThrow();
        authorize(dataset.getWorkspace(), true);

        Field field = FieldFactory.createField(fieldForm);
        field.setDataset(dataset);
        validateReferences(field);
        // TODO verify if alias exists

        field = fieldRepository.save(field);

        return ResponseEntity.ok(ResponseSuccess.builder()
                                         .externalId(field.getExternalId().toString())
                                         .objectType(Constants.FIELD_OBJECT)
                                         .payload(FieldMapper.INSTANCE.fieldToFieldResponse(field))
                                         .build());
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse> batchProcess(@Valid @RequestBody MultipleFieldsForm fieldsForm) {
        Assert.notEmpty(fieldsForm.getFields(), "Fields is empty");
        Dataset dataset = datasetRepository.findByExternalId(UUID.fromString(fieldsForm.getDatasetId())).orElseThrow();
        authorize(dataset.getWorkspace(), true);

        List<Field> fields = Lists.newArrayList();
        fieldsForm.getFields().forEach(form -> {
            try {
                if (StringUtils.isNotBlank(form.getExternalId())) {
                    Field field = fieldRepository.findByExternalId(UUID.fromString(form.getExternalId())).orElseThrow();
                    Field updatedField = FieldFactory.createField(form);
                    updatedField.setDateCreated(field.getDateCreated());
                    updatedField.setDateModified(new Date());
                    validateReferences(updatedField);
                    updatedField.setDataset(dataset);
                    updatedField.setId(field.getId());
                    if (field.getClass().equals(updatedField.getClass())) {
                        updatedField.setExternalId(field.getExternalId());
                    } else {
                        fieldRepository.delete(field);
                    }

                    fields.add(updatedField);
                } else {
                    Field field = FieldFactory.createField(form);
                    field.setDataset(dataset);
                    validateReferences(field);
                    fields.add(field);
                }
            } catch (Exception e) {
                if (e instanceof NoSuchElementException) {
                    throw (NoSuchElementException)e;
                }
                throw new IllegalArgumentException(e);
            }
        });

        Iterable<Field> iterableFields = fieldRepository.saveAll(fields);

        return ResponseEntity.ok(ResponseSuccess.builder()
            .objectType(Constants.FIELD_OBJECT)
            .payload(StreamSupport
                .stream(iterableFields.spliterator(), false)
                .map(FieldMapper.INSTANCE::fieldToFieldResponse)
                .collect(Collectors.toList()))
            .build());
    }

    @PutMapping("/{externalId}")
    public ResponseEntity<ApiResponse> update(@PathVariable @NotBlank String externalId, @Valid @RequestBody FieldForm fieldForm) throws Exception {

        Field field = fieldRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        authorize(field.getDataset().getWorkspace(), true);
        // TODO verify if alias exists

        Field updatedField = FieldFactory.createField(fieldForm);
        field.setDateModified(new Date());
        updatedField.setDataset(field.getDataset());
        updatedField.setId(field.getId());
        validateReferences(updatedField);

        if (field.getClass().equals(updatedField.getClass())) {
            updatedField.setExternalId(field.getExternalId());
        } else {
            fieldRepository.delete(field);
        }
        field = fieldRepository.save(updatedField);

        return ResponseEntity.ok(ResponseSuccess.builder()
                                         .externalId(field.getExternalId().toString())
                                         .objectType(Constants.FIELD_OBJECT)
                                         .payload(FieldMapper.INSTANCE.fieldToFieldResponse(field))
                                         .build());
    }

    @DeleteMapping("/{externalId}")
    public ResponseEntity<ApiResponse> delete(@PathVariable @NotBlank String externalId) {

        Field field = fieldRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        authorize(field.getDataset().getWorkspace(), true);

        fieldService.delete(field);

        return ResponseEntity.ok(ResponseSuccess.builder()
                                         .externalId(field.getExternalId().toString())
                                         .objectType(Constants.FIELD_OBJECT)
                                         .build());
    }

    @PutMapping("/{externalId}/formatter")
    public void setFormatter(@PathVariable @NotBlank String externalId) {
        Field field = fieldRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        authorize(field.getDataset().getWorkspace(), true);

        field.getFormatter();
    }

}

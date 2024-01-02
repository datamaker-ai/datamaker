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
import ai.datamaker.model.field.FieldMapping;
import ai.datamaker.model.forms.FieldForm;
import ai.datamaker.model.forms.FieldMappingForm;
import ai.datamaker.model.response.ApiResponse;
import ai.datamaker.model.response.FieldMappingResponse;
import ai.datamaker.model.response.ResponseError;
import ai.datamaker.model.response.ResponseSuccess;
import ai.datamaker.repository.FieldMappingRepository;
import ai.datamaker.service.FieldDetectorService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Add custom field mappings (synonyms)
 */
@RestController
@Slf4j
@RequestMapping("/api/field-mappings")
public class FieldMappingController {

  @Autowired
  private FieldDetectorService fieldDetectorService;

  @Autowired
  private FieldMappingRepository fieldMappingRepository;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @GetMapping(path = "/lang/{languageTag}")
  public ResponseEntity<ApiResponse> getForLanguage(@PathVariable String languageTag) {
    List<FieldMappingResponse> fieldMappingList = StreamSupport.stream(fieldMappingRepository.findAll().spliterator(), false)
            .filter(fm -> fm.getMappingKey().endsWith("-" + languageTag))
            .map(this::convertFieldMapping)
            .filter(Objects::nonNull)
        .sorted((f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()))
        .collect(Collectors.toList());
    return ResponseEntity.ok(ResponseSuccess
                                     .builder()
                                     .payload(fieldMappingList)
                                     .objectType(Constants.FIELD_MAPPING_OBJECT)
                                     .build());
  }

  // TODO pageable ?
  @GetMapping
  public ResponseEntity<ApiResponse> getAll() {
    List<FieldMappingResponse> fieldMappingList = StreamSupport
        .stream(fieldMappingRepository.findAll().spliterator(), false)
        .map(this::convertFieldMapping)
        .filter(Objects::nonNull)
        .sorted((f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()))
        .collect(Collectors.toList());
    return ResponseEntity.ok(ResponseSuccess
                                     .builder()
                                     .payload(fieldMappingList)
                                     .objectType(Constants.FIELD_MAPPING_OBJECT)
                                     .build());
  }

  @GetMapping(path = "/{externalId}")
  @ResponseBody
  public ResponseEntity<ApiResponse> get(@PathVariable @NotBlank String externalId) {

    FieldMapping fieldMapping = fieldMappingRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();

    return ResponseEntity.ok(ResponseSuccess.builder()
        .externalId(fieldMapping.getExternalId().toString())
        .objectType(Constants.FIELD_MAPPING_OBJECT)
        .payload(convertFieldMapping(fieldMapping))
        .build());
  }

  @PostMapping
  public ResponseEntity<ApiResponse> create(@Valid @RequestBody FieldMappingForm fieldForm) throws JsonProcessingException {

    String key = FieldDetectorService.getKey(fieldForm.getName(), Locale.forLanguageTag(fieldForm.getLanguageTag()));

    Optional<FieldMapping> fieldMappingExisting = fieldMappingRepository.findByMappingKey(key);
    if (fieldMappingExisting.isPresent()) {
      return ResponseEntity
              .badRequest()
              .body(ResponseError
                            .builder()
                            .title("cannot create field mapping")
                            .detail("field mapping found for key " + key + " with external id: " + fieldMappingExisting.get().getExternalId())
                            .status(400)
                            .build());
    }

    FieldMapping fieldMapping = new FieldMapping();
    fieldMapping.setMappingKey(key);
    fieldMapping.setFieldJson(OBJECT_MAPPER.writeValueAsString(fieldForm));
    fieldMapping = fieldMappingRepository.save(fieldMapping);

    return initializeFieldDetectorAndCreateResponse(fieldMapping.getExternalId().toString());
  }

  @PutMapping(path = "/{externalId}")
  public ResponseEntity<ApiResponse> update(@PathVariable @NotBlank String externalId, @Valid @RequestBody FieldMappingForm fieldForm) throws JsonProcessingException {

    Optional<FieldMapping> fieldMapping =  fieldMappingRepository.findByExternalId(UUID.fromString(externalId));
    if (fieldMapping.isEmpty()) {
      return ResponseEntity
          .badRequest()
          .body(ResponseError
              .builder()
              .title("field mapping not found for external id: " + externalId)
              .status(400)
              .build());
    }
    // test if key already exists with different id
    String key = FieldDetectorService.getKey(fieldForm.getName(), Locale.forLanguageTag(fieldForm.getLanguageTag()));

    Optional<FieldMapping> fieldMappingExisting = fieldMappingRepository.findByMappingKey(key);
    if (fieldMappingExisting.isPresent() && !fieldMappingExisting.get().getExternalId().equals(fieldMapping.get().getExternalId())) {
      return ResponseEntity
              .badRequest()
              .body(ResponseError
                            .builder()
                            .title("cannot update field mapping")
                            .detail("different field mapping found for key " + key + " with external id: " + fieldMappingExisting.get().getExternalId())
                            .status(400)
                            .build());
    }

    FieldMapping fieldMappingNew = fieldMapping.get();
    fieldMappingNew.setMappingKey(key);
    fieldMappingNew.setFieldJson(OBJECT_MAPPER.writeValueAsString(fieldForm));
    fieldMappingRepository.save(fieldMappingNew);

    return initializeFieldDetectorAndCreateResponse(externalId);
  }

  @DeleteMapping(path = "/{externalId}")
  public ResponseEntity<ApiResponse> delete(@PathVariable @NotBlank String externalId) {

    Optional<FieldMapping> fieldMapping = fieldMappingRepository.findByExternalId(UUID.fromString(externalId));
    if (fieldMapping.isEmpty()) {
      return ResponseEntity
         .badRequest()
          .body(ResponseError
              .builder()
              .detail("field mapping not found for external id: " + externalId)
              .title("cannot update field mapping")
              .status(400)
              .build());

    }

    fieldMappingRepository.delete(fieldMapping.get());

    return initializeFieldDetectorAndCreateResponse(externalId);
  }

  private FieldMappingResponse convertFieldMapping(FieldMapping fieldMapping) {
    FieldForm fieldForm = null;
    try {
      fieldForm = OBJECT_MAPPER.readValue(fieldMapping.getFieldJson(), FieldForm.class);
    } catch (JsonProcessingException e) {
      return null;
    }

    return FieldMappingResponse
        .builder()
        .externalId(fieldMapping.getExternalId().toString())
        .className(fieldForm.getClassName())
        .formatterClassName(fieldForm.getFormatterClassName())
        .name(fieldForm.getName())
        .languageTag(fieldForm.getLanguageTag())
        .config(fieldForm.getConfig())
        .isNullable(fieldForm.getIsNullable())
        .isPrimaryKey(fieldForm.getIsPrimaryKey())
        .isAttribute(fieldForm.getIsAttribute())
        .nullValue(fieldForm.getNullValue())
        .build();
  }

  private ResponseEntity<ApiResponse> initializeFieldDetectorAndCreateResponse(String externalId) {
    try {
      fieldDetectorService.init();
    } catch (Exception e) {
      log.error("Cannot initialize field detector service", e);

      return ResponseEntity
          .status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ResponseError
              .builder()
              .title("cannot initialize field detector service")
              .detail(e.getMessage())
              .status(500)
              .build());
    }

    return ResponseEntity.ok(ResponseSuccess
        .builder()
        .externalId(externalId)
        .objectType(Constants.FIELD_MAPPING_OBJECT)
        .build());
  }

}

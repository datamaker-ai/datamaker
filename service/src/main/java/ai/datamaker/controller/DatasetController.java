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

import ai.datamaker.exception.InvalidParameterException;
import ai.datamaker.model.Constants;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.Workspace;
import ai.datamaker.model.forms.DatasetForm;
import ai.datamaker.model.mapper.DatasetMapper;
import ai.datamaker.model.mapper.FieldMapper;
import ai.datamaker.model.response.ApiResponse;
import ai.datamaker.model.response.DatasetResponse;
import ai.datamaker.model.response.ResponseError;
import ai.datamaker.model.response.ResponseSuccess;
import ai.datamaker.repository.DatasetRepository;
import ai.datamaker.repository.FieldRepository;
import ai.datamaker.repository.WorkspaceRepository;
import ai.datamaker.service.DatasetCreationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SuppressWarnings("rawtypes")
@RestController
@Slf4j
@RequestMapping("/api/dataset")
public class DatasetController extends AbstractRestController {

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private DatasetCreationService datasetCreationService;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private BuildProperties buildProperties;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @GetMapping(path = "/{externalId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> get(@PathVariable @NotBlank String externalId) {

        Dataset dataset = datasetRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        authorize(dataset.getWorkspace(), false);

        return ResponseEntity.ok(ResponseSuccess.builder()
            .externalId(dataset.getExternalId().toString())
            .objectType(Constants.DATASET_OBJECT)
            .payload(DatasetMapper.INSTANCE.datasetToDatasetResponse(dataset))
            .build());
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<ApiResponse> list() {

        List<DatasetResponse> datasets = StreamSupport
            .stream(datasetRepository.findAll().spliterator(), false)
            .filter(d -> isAuthorized(d.getWorkspace(), false))
            .map(DatasetMapper.INSTANCE::datasetToDatasetResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .objectType(Constants.DATASET_OBJECT)
            .payload(datasets)
            .build());
    }

    @DeleteMapping(path = "/{externalId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> delete(@Valid @PathVariable String externalId) {

        Dataset dataset = datasetRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        authorize(dataset.getWorkspace(), true);

        dataset.getFields().forEach(f -> fieldRepository.deleteById(f.getId()));
        dataset.getFields().clear();
        datasetRepository.delete(dataset);

        return ResponseEntity.ok(ResponseSuccess.builder()
            .externalId(dataset.getExternalId().toString())
            .objectType(Constants.DATASET_OBJECT)
            .build());

    }

    @PutMapping(path = "/{externalId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> update(@Valid @PathVariable String externalId, @Valid @RequestBody DatasetForm form) {

        Dataset dataset = datasetRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        authorize(dataset.getWorkspace(), true);

        Dataset updatedDataset = DatasetMapper.INSTANCE.datasetFormToDataset(form);
        updatedDataset.setDateModified(new Date());
        updatedDataset.setDateCreated(dataset.getDateCreated());
        updatedDataset.setId(dataset.getId());
        updatedDataset.setFields(dataset.getFields());
        updatedDataset.setWorkspace(dataset.getWorkspace());

        if (dataset.getWorkspace().getExternalId().toString().equals(form.getWorkspaceId())) {
            updatedDataset.setWorkspace(dataset.getWorkspace());
        } else {
            Workspace workspaceTo = workspaceRepository.findByExternalId(UUID.fromString(form.getWorkspaceId())).orElseThrow();
            authorize(workspaceTo, true);
            updatedDataset.setWorkspace(workspaceTo);
        }

        datasetRepository.save(updatedDataset);

        return ResponseEntity.ok(ResponseSuccess.builder()
                                         .externalId(dataset.getExternalId().toString())
                                         .objectType(Constants.DATASET_OBJECT)
                                         .payload(DatasetMapper.INSTANCE.datasetToDatasetResponse(updatedDataset))
                                         .build());

    }

    @CacheEvict(value="dashboard", allEntries=true)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody DatasetForm form) {

        if ("DEMO".equalsIgnoreCase(buildProperties.get("profile"))) {
            if (datasetRepository.count() >= 10) {
                throw new IllegalArgumentException("Dataset limit reached for demo version");
            }
        }

        Workspace workspace = workspaceRepository.findByExternalId(UUID.fromString(form.getWorkspaceId())).orElseThrow();
        authorize(workspace, true);

        if (datasetRepository.findByName(form.getName()).isPresent()) {
            throw new InvalidParameterException("name.already.exists", form.getName());
        }

        Dataset dataset = DatasetMapper.INSTANCE.datasetFormToDataset(form);
        dataset.setWorkspace(workspace);

        dataset = datasetRepository.save(dataset);

        return ResponseEntity.ok(ResponseSuccess.builder()
                                  .externalId(dataset.getExternalId().toString())
                                  .objectType(Constants.DATASET_OBJECT)
                                  .payload(DatasetMapper.INSTANCE.datasetToDatasetResponse(dataset))
                                  .build());
    }

    @GetMapping(path = "/{datasetId}/fields", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> listFields(
        @PathVariable @NotBlank String datasetId,
        @RequestParam(required = false, defaultValue = "false") Boolean primaryOnly,
        @RequestParam(required = false, defaultValue = "false") Boolean includeNested) {

        Dataset dataset = datasetRepository.findByExternalId(UUID.fromString(datasetId)).orElseThrow();
        authorize(dataset.getWorkspace(), false);

        return ResponseEntity
                .ok(ResponseSuccess
                            .builder()
                            .objectType(Constants.FIELD_OBJECT)
                            .externalId(dataset.getExternalId().toString())
                            .payload(dataset
                                             .getFields()
                                             .stream()
                                             .filter(f -> includeNested || !f.getIsNested())
                                             .filter(f -> primaryOnly ? f.getIsPrimaryKey() : true)
                                             .map(FieldMapper.INSTANCE::fieldToFieldResponse)
                                             .collect(Collectors.toList())
                            )
                            .build());
    }

    @PostMapping("/generate-from-database/{workspaceId}")
    public void handleDatabaseSync() {

        // list databases
        // list tables
        // select columns ?
    }

    @PostMapping("/generate-from-content/{workspaceId}")
    public ResponseEntity<ApiResponse> handleContent(@PathVariable @NotBlank String workspaceId,
                                                     @RequestParam("name") String name,
                                                     @RequestParam("config") String configProperties,
                                                     @RequestParam("content") String content,
                                                     @RequestParam("mediaType") String mediaType,
                                                     Locale locale) throws JsonProcessingException {

        Workspace workspace = workspaceRepository.findByExternalId(UUID.fromString(workspaceId)).orElseThrow();
        authorize(workspace, true);

        JobConfig config = OBJECT_MAPPER.readValue(configProperties, JobConfig.class);

        if (!config.containsKey(Constants.LOCALE)) {
            config.put(Constants.LOCALE, locale.toLanguageTag());
        }
        if (!config.containsKey(Constants.INPUT_FILENAME_KEY)) {
            config.put(Constants.INPUT_FILENAME_KEY, "from content " + mediaType);
        }
        try {
            return createResponse(mediaType,
                                  "",
                                  new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                                  config,
                                  workspace,
                                  name);

        } catch (RuntimeException e) {
            log.warn("Generate dataset from content error: ", e);
            return ResponseEntity
                    .badRequest()
                    .body(ResponseError
                                  .builder()
                                  .title("cannot get dataset from content")
                                  .detail(e.getMessage())
                                  .status(HttpStatus.NOT_FOUND.value())
                                  .build());
        }
    }

    @PostMapping("/generate-from-file/{workspaceId}")
    public ResponseEntity<ApiResponse> handleFileUpload(@PathVariable @NotBlank String workspaceId,
                                                        @RequestParam("name") String name,
                                                        @RequestParam("mediaType") String mediaType,
                                                        @RequestParam("config") String configProperties,
                                                        @RequestParam("file") MultipartFile file,
                                                        Locale locale) throws JsonProcessingException {

        Workspace workspace = workspaceRepository.findByExternalId(UUID.fromString(workspaceId)).orElseThrow();
        authorize(workspace, true);

        JobConfig config = OBJECT_MAPPER.readValue(configProperties, JobConfig.class);

        if (!config.containsKey(Constants.LOCALE)) {
            config.put(Constants.LOCALE, locale.toLanguageTag());
        }
        if (!config.containsKey(Constants.INPUT_FILENAME_KEY)) {
            config.put(Constants.INPUT_FILENAME_KEY, file.getOriginalFilename());
        }

        try {
            return createResponse(mediaType,
                                  FilenameUtils.getExtension(file.getOriginalFilename()),
                                  new BufferedInputStream(file.getInputStream()),
                                  config,
                                  workspace,
                                  name);

        } catch (IOException e) {
            log.warn("Generate dataset from file error: ", e);
            return ResponseEntity
                    .badRequest()
                    .body(ResponseError
                                  .builder()
                                  .title("cannot get dataset from file")
                                  .detail(e.getMessage())
                                  .status(HttpStatus.NOT_FOUND.value())
                                  .build());
        }
    }

    private ResponseEntity<ApiResponse> createResponse(String mediaType,
                                                       String fileExtension,
                                                       InputStream inputStream,
                                                       JobConfig config,
                                                       Workspace workspace,
                                                       String datasetName) {

        Optional<Dataset> optionalDataset = datasetCreationService.create(mediaType,
                                                                  fileExtension,
                                                                  inputStream,
                                                                  config);

        return optionalDataset.<ResponseEntity<ApiResponse>>map(dataset -> {
            if (StringUtils.isNotBlank(datasetName)) {
                dataset.setName(datasetName);
            }
            dataset.setWorkspace(workspace);
            datasetRepository.save(dataset);
            return ResponseEntity.ok(ResponseSuccess.builder()
                .externalId(dataset.getExternalId().toString())
                .objectType(Constants.DATASET_OBJECT)
                .payload(DatasetMapper.INSTANCE.datasetToDatasetResponse(dataset))
                .build());
            })
            .orElseGet(() -> ResponseEntity.badRequest().body(ResponseError.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .title("cannot create dataset for media type " + mediaType)
            .build()));

    }

}

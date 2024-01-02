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
import ai.datamaker.model.Workspace;
import ai.datamaker.model.forms.GenerateDataJobForm;
import ai.datamaker.model.forms.GeneratorForm;
import ai.datamaker.model.forms.SinkForm;
import ai.datamaker.model.job.GenerateDataJob;
import ai.datamaker.model.job.JobExecution;
import ai.datamaker.model.mapper.GenerateDataJobMapper;
import ai.datamaker.model.response.ApiResponse;
import ai.datamaker.model.response.GenerateDataJobResponse;
import ai.datamaker.model.response.GenerateDataJobSummaryResponse;
import ai.datamaker.model.response.ResponseSuccess;
import ai.datamaker.repository.DatasetRepository;
import ai.datamaker.repository.GenerateDataJobRepository;
import ai.datamaker.repository.JobExecutionRepository;
import ai.datamaker.repository.SinkConfigurationRepository;
import ai.datamaker.repository.WorkspaceRepository;
import ai.datamaker.service.DatasetService;
import ai.datamaker.service.JobSchedulerService;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/generate-data-job")
public class GenerateDataJobController extends AbstractRestController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private GenerateDataJobRepository generateDataJobRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private SinkConfigurationRepository sinkConfigurationRepository;

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @Autowired
    private JobSchedulerService jobSchedulerService;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private BuildProperties buildProperties;

    @GetMapping(path = "/{externalId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> get(@PathVariable @NotBlank String externalId) {

        GenerateDataJob generateDataJob = generateDataJobRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        authorize(generateDataJob.getWorkspace(), false);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(generateDataJob.getExternalId().toString())
                                         .objectType(Constants.GENERATE_DATA_JOB_OBJECT)
                                         .payload(GenerateDataJobMapper.INSTANCE.generateDataJobToGenerateDataJobResponse(generateDataJob))
                                         .build());
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<ApiResponse> list() {

        List<GenerateDataJobResponse> dataJobResponses = StreamSupport
            .stream(generateDataJobRepository.findAll().spliterator(), false)
            .filter(d -> isAuthorized(d.getWorkspace(), false))
            .map(GenerateDataJobMapper.INSTANCE::generateDataJobToGenerateDataJobResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .objectType(Constants.GENERATE_DATA_JOB_OBJECT)
            .payload(dataJobResponses)
            .build());
    }

    @GetMapping("/summary")
    @ResponseBody
    public ResponseEntity<ApiResponse> summary() {
        List<GenerateDataJobSummaryResponse> dataJobResponses = StreamSupport
            .stream(generateDataJobRepository.findAll().spliterator(), false)
            .filter(d -> isAuthorized(d.getWorkspace(), false))
            .map(job -> {
                JobExecution jobExecution =jobExecutionRepository.findFirstByDataJobOrderByStartTimeDesc(job);
                Long nextRun = jobSchedulerService.monitorFutureTask(job);

                return GenerateDataJobSummaryResponse
                    .builder()
                    .externalId(job.getExternalId().toString())
                    .description(job.getDescription())
                    .name(job.getName())
                    .workspace(job.getWorkspace().getName())
                    .lastRunStatus(jobExecution != null ? jobExecution.getState().toString() : "INIT")
                    .lastStartTime(jobExecution != null ? DATE_TIME_FORMATTER.format(jobExecution.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()) : "-")
                    .nextRunSchedule(nextRun == null ? "-" : DATE_TIME_FORMATTER.format(LocalDateTime.now().plusSeconds(nextRun.intValue())))
                    .build();
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .objectType(Constants.GENERATE_DATA_JOB_OBJECT)
            .payload(dataJobResponses)
            .build());
    }

    @GetMapping(path = "/workspace/{workspaceId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> list(@PathVariable @NotBlank String workspaceId) {

        Workspace workspace = workspaceRepository.findByExternalId(UUID.fromString(workspaceId)).orElseThrow();
        authorize(workspace, false);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(workspaceId)
                                         .objectType(Constants.GENERATE_DATA_JOB_OBJECT)
                                         .payload(workspace.getDataJobs()
                                                          .stream()
                                                          .map(GenerateDataJobMapper.INSTANCE::generateDataJobToGenerateDataJobResponse))
                                         .build());
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody GenerateDataJobForm generateDataJobForm) {
        if ("DEMO".equalsIgnoreCase(buildProperties.get("profile"))) {
            if (generateDataJobRepository.count() >= 10) {
                throw new IllegalArgumentException("Generate data job limit reached for demo version");
            }
        }

        Workspace workspace = workspaceRepository.findByExternalId(UUID.fromString(generateDataJobForm.getWorkspaceId())).orElseThrow();
        authorize(workspace, true);

        if (generateDataJobRepository.findByNameAndWorkspace(generateDataJobForm.getName(), workspace).isPresent()) {
            throw new InvalidParameterException("name.already.exists", generateDataJobForm.getName());
        }

        GenerateDataJob generateDataJob = GenerateDataJobMapper.INSTANCE.generateDataJobFormToGenerateDataJob(generateDataJobForm);
        generateDataJob.setWorkspace(workspace);

        generateDataJobRepository.save(generateDataJob);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(generateDataJob.getExternalId().toString())
                                         .objectType(Constants.GENERATE_DATA_JOB_OBJECT)
                                         .payload(GenerateDataJobMapper.INSTANCE.generateDataJobToGenerateDataJobResponse(generateDataJob))
                                         .build());
    }

    @PostMapping(path = "/{externalId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> duplicate(@PathVariable @NotBlank String externalId, @Valid String name) {

//        Workspace workspace = workspaceRepository.findByExternalId(UUID.fromString(generateDataJobForm.getWorkspaceId())).orElseThrow();
//        authorize(workspace, true);
//
//        if (generateDataJobRepository.findByNameAndWorkspace(generateDataJobForm.getName(), workspace).isPresent()) {
//            throw new InvalidParameterException("name.already.exists", generateDataJobForm.getName());
//        }
//
//        GenerateDataJob generateDataJob = GenerateDataJobMapper.INSTANCE.generateDataJobFormToGenerateDataJob(generateDataJobForm);
//        generateDataJob.setWorkspace(workspace);
//
//        generateDataJobRepository.save(generateDataJob);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         //.externalId(generateDataJob.getExternalId().toString())
                                         .objectType(Constants.GENERATE_DATA_JOB_OBJECT)
                                         .build());
    }

    @PutMapping(path = "/{externalId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> update(@PathVariable @NotBlank String externalId, @Valid @RequestBody GenerateDataJobForm generateDataJobForm) {

        GenerateDataJob generateDataJob = generateDataJobRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        authorize(generateDataJob.getWorkspace(), true);

        if (!generateDataJob.getName().equals(generateDataJobForm.getName())
                && generateDataJobRepository.findByNameAndWorkspace(generateDataJobForm.getName(), generateDataJob.getWorkspace()).isPresent()) {
            throw new InvalidParameterException("name.already.exists", generateDataJobForm.getName());
        }

        GenerateDataJob updatedGenerateDataJob = GenerateDataJobMapper.INSTANCE.generateDataJobFormToGenerateDataJob(generateDataJobForm);
        updatedGenerateDataJob.setId(generateDataJob.getId());
        updatedGenerateDataJob.setWorkspace(generateDataJob.getWorkspace());
        updatedGenerateDataJob.setExternalId(generateDataJob.getExternalId());
        updatedGenerateDataJob.setDataset(generateDataJob.getDataset());
        updatedGenerateDataJob.getSinkNames().clear();
        updatedGenerateDataJob.setDateModified(new Date());
        updatedGenerateDataJob.getSinkNames().addAll(generateDataJob.getSinkNames());
        updatedGenerateDataJob.setGeneratorName(generateDataJob.getGeneratorName());
        updatedGenerateDataJob.setConfig(generateDataJob.getConfig());

        generateDataJobRepository.save(updatedGenerateDataJob);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(generateDataJob.getExternalId().toString())
                                         .objectType(Constants.GENERATE_DATA_JOB_OBJECT)
                                         .build());
    }

    @PutMapping(path = "/{externalId}/dataset/{datasetId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> addDataset(@PathVariable @NotBlank String externalId, @PathVariable @NotBlank String datasetId) {

        GenerateDataJob generateDataJob = generateDataJobRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        authorize(generateDataJob.getWorkspace(), true);

        Dataset dataset = datasetRepository.findByExternalId(UUID.fromString(datasetId)).orElseThrow();

        generateDataJob.getDataset().add(dataset);

//        List<Dataset> sortedDatasets = datasetService.sortDatasetsPerDependencies(generateDataJob.getDataset());
//        generateDataJob.setDataset(sortedDatasets);

        generateDataJobRepository.save(generateDataJob);

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(generateDataJob.getExternalId().toString())
            .objectType(Constants.GENERATE_DATA_JOB_OBJECT)
            .build());
    }

    @PutMapping(path = "/{externalId}/datasets")
    @ResponseBody
    public ResponseEntity<ApiResponse> addDatasets(@PathVariable @NotBlank String externalId, @RequestBody List<String> datasetIds) {

        GenerateDataJob generateDataJob = generateDataJobRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        authorize(generateDataJob.getWorkspace(), true);

        Iterable<Dataset> datasets = datasetRepository
            .findAllByExternalIdIn(datasetIds
                .stream()
                .map(UUID::fromString)
                .collect(Collectors.toList()));

//        List<Dataset> sortedDatasets = datasetService.sortDatasetsPerDependencies(Lists.newArrayList(datasets));
//        generateDataJob.setDataset(sortedDatasets);

        generateDataJob.getDataset().clear();
        generateDataJob.getDataset().addAll(datasetService.sortDatasetsPerDependencies(Lists.newArrayList(datasets)));
        generateDataJobRepository.save(generateDataJob);

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(generateDataJob.getExternalId().toString())
            .objectType(Constants.GENERATE_DATA_JOB_OBJECT)
            .build());
    }

    @PutMapping(path = "/{externalId}/generator")
    @ResponseBody
    public ResponseEntity<ApiResponse> setGenerator(@PathVariable @NotBlank String externalId, @Valid @RequestBody GeneratorForm form) throws Exception {

        GenerateDataJob generateDataJob = generateDataJobRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        authorize(generateDataJob.getWorkspace(), true);

        generateDataJob.getConfig().remove(generateDataJob.getGenerator());
        generateDataJob.setGeneratorName(form.getGeneratorClassName());
        generateDataJob.getConfig().put(form.getGeneratorClassName(), form.getConfig());
        generateDataJob.build();

        // Optional<SinkConfiguration> globalSink = sinkConfigurationRepository.findBySinkClassnameAndWorkspace(sinkName, generateDataJob.getWorkspace());
        // globalSink.ifPresent(sinkConfig -> generateDataJob.getConfig().putAll(sinkConfig.getJobConfig()));

        generateDataJobRepository.save(generateDataJob);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(generateDataJob.getExternalId().toString())
                                         .objectType(Constants.GENERATE_DATA_JOB_OBJECT)
                                         .build());
    }

    @PutMapping(path = "/{externalId}/sink")
    @ResponseBody
    public ResponseEntity<ApiResponse> addSink(@PathVariable @NotBlank String externalId, @Valid @RequestBody SinkForm form) throws Exception {

        GenerateDataJob generateDataJob = generateDataJobRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        authorize(generateDataJob.getWorkspace(), true);

        generateDataJob.getSinkNames().forEach(s -> generateDataJob.getConfig().remove(s));
        generateDataJob.getSinks().clear();
        generateDataJob.getSinkNames().clear();
        generateDataJob.getSinkNames().add(form.getSinkClassName());
        generateDataJob.getConfig().put(form.getSinkClassName(), form.getConfig());
        generateDataJob.build();

        // Optional<SinkConfiguration> globalSink = sinkConfigurationRepository.findBySinkClassnameAndWorkspace(sinkName, generateDataJob.getWorkspace());
        // globalSink.ifPresent(sinkConfig -> generateDataJob.getConfig().putAll(sinkConfig.getJobConfig()));

        generateDataJobRepository.save(generateDataJob);

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(generateDataJob.getExternalId().toString())
            .objectType(Constants.GENERATE_DATA_JOB_OBJECT)
            .build());
    }

    @DeleteMapping(path = "/{externalId}/sink")
    @ResponseBody
    public ResponseEntity<ApiResponse> removeSink(@PathVariable @NotBlank String externalId, @Valid SinkForm sink) throws Exception {

        GenerateDataJob generateDataJob = generateDataJobRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        authorize(generateDataJob.getWorkspace(), true);

        generateDataJob.getSinkNames().remove(sink.getSinkClassName());
        generateDataJob.getConfig().remove(sink.getSinkClassName());
        generateDataJob.build();

        // Optional<SinkConfiguration> globalSink = sinkConfigurationRepository.findBySinkClassnameAndWorkspace(sinkName, generateDataJob.getWorkspace());
        // globalSink.ifPresent(sinkConfig -> generateDataJob.getConfig().putAll(sinkConfig.getJobConfig()));

        generateDataJobRepository.save(generateDataJob);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(generateDataJob.getExternalId().toString())
                                         .objectType(Constants.GENERATE_DATA_JOB_OBJECT)
                                         .build());
    }

    @DeleteMapping(path = "/{externalId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> delete(@PathVariable @NotBlank String externalId) {

        GenerateDataJob dataJob = generateDataJobRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        Workspace workspace = dataJob.getWorkspace();

        authorize(workspace, true);
        workspace.removeGenerateDataJob(dataJob);
        Workspace workspaceResponse = workspaceRepository.save(workspace);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(workspaceResponse.getExternalId().toString())
                                         .objectType(Constants.GENERATE_DATA_JOB_OBJECT)
                                         .build());
    }
}

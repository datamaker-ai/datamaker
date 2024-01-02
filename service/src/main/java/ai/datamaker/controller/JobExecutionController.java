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
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.SupportedMediaType;
import ai.datamaker.model.job.GenerateDataJob;
import ai.datamaker.model.job.JobExecution;
import ai.datamaker.model.mapper.JobExecutionMapper;
import ai.datamaker.model.response.ApiResponse;
import ai.datamaker.model.response.JobExecutionResponse;
import ai.datamaker.model.response.ResponseError;
import ai.datamaker.model.response.ResponseSuccess;
import ai.datamaker.repository.DatasetRepository;
import ai.datamaker.repository.GenerateDataJobRepository;
import ai.datamaker.repository.JobExecutionRepository;
import ai.datamaker.service.GenerateDataService;
import ai.datamaker.service.JobSchedulerService;
import ai.datamaker.sink.base.FileOutputSink;
import ai.datamaker.sink.base.ProxyOutputSink;
import ai.datamaker.utils.FormatTypeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/job-execution")
public class JobExecutionController extends AbstractRestController {

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private GenerateDataService generateDataService;

    @Autowired
    private JobSchedulerService jobSchedulerService;

    @Autowired
    private GenerateDataJobRepository generateDataJobRepository;

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    //@Autowired
    //private AzureStorageOutputSink azureStorageOutputSink;

    @Autowired
    private ServletContext servletContext;

    // FIXME validate workspace permissions

    @Autowired
    private BuildProperties buildProperties;

    @PostMapping("/{generateDataJobId}/run")
    @ResponseBody
    public ResponseEntity<ApiResponse> runJobSynchronous(@NotBlank @PathVariable String generateDataJobId) {

        if ("DEMO".equalsIgnoreCase(buildProperties.get("profile"))) {
            if (jobExecutionRepository.count() >= 100) {
                throw new IllegalArgumentException("Job execution limit reached for demo version");
            }
        }

        GenerateDataJob generateDataJob = generateDataJobRepository.findByExternalId(UUID.fromString(generateDataJobId)).orElseThrow();
        authorize(generateDataJob.getWorkspace(), false);

        JobExecution jobExecution = generateDataService.execute(generateDataJob, true);

        if (!jobExecution.getIsSuccess()) {
            return ResponseEntity.ok(ResponseError
                .builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .title("Error while executing job")
                .detail(jobExecution.getErrors().get(0))
                .build());
        }

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .objectType(Constants.JOB_EXECUTION_OBJECT)
            .externalId(generateDataJob.getExternalId().toString())
            .payload(jobExecution)
            .build());
    }

    // Helper
    @GetMapping(path = "/{generateDataJobId}/file", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public FileSystemResource getDatasetFile(@NotBlank @PathVariable String generateDataJobId, HttpServletResponse response) throws Exception {

        // Add file output stream to sinks
        GenerateDataJob generateDataJob = generateDataJobRepository.findByExternalId(UUID.fromString(generateDataJobId)).orElseThrow();
        authorize(generateDataJob.getWorkspace(), false);

        String fileOutputSink = FileOutputSink.class.getCanonicalName();
        generateDataJob.getSinks().clear();
        generateDataJob.getSinkNames().clear();
        generateDataJob.getSinkNames().add(fileOutputSink);
        generateDataJob.build();

        JobExecution jobExecution = generateDataService.execute(generateDataJob, false);

        File filePath = new File(jobExecution.getResults().get(0));

        // FIXME set proper content type
        try {
            SupportedMediaType supportedMediaType = SupportedMediaType.valueOf(generateDataJob.getGenerator().getDataType().toString());
            response.setContentType(supportedMediaType.getMediaTypes().iterator().next());
        } catch (NullPointerException npe) {
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);

        }
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filePath.getName() + "\"");

        return new FileSystemResource(filePath);
    }

    // Helper
    @GetMapping(path = "/{generateDataJobId}/output", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public void getDatasetOutput(@NotBlank @PathVariable String generateDataJobId, HttpServletResponse response) throws Exception {

        // Add output stream to sinks
        GenerateDataJob generateDataJob = generateDataJobRepository.findByExternalId(UUID.fromString(generateDataJobId)).orElseThrow();
        authorize(generateDataJob.getWorkspace(), false);

        String proxyOutputSink = ProxyOutputSink.class.getCanonicalName();
        generateDataJob.getSinks().clear();
        generateDataJob.getSinkNames().clear();
        generateDataJob.getSinkNames().add(proxyOutputSink);
        generateDataJob.build();

        response.setContentType(FormatTypeUtils.getContentType(generateDataJob.getGenerator().getDataType()));

        JobConfig config = new JobConfig();
        config.put(ProxyOutputSink.SINK_PROXY_STREAM, response.getOutputStream());
        generateDataJob.getConfig().put(ProxyOutputSink.class.getName(), config);

        generateDataService.execute(generateDataJob, false);

        response.flushBuffer();
    }

    @GetMapping(path = "/{generateDataJobId}/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> getNextJobRun(@Valid @NotBlank @PathVariable String generateDataJobId) {

        GenerateDataJob dataJob = generateDataJobRepository.findByExternalId(UUID.fromString(generateDataJobId)).orElseThrow();
        authorize(dataJob.getWorkspace(), false);
        Long nextRun = jobSchedulerService.monitorFutureTask(dataJob);

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(dataJob.getExternalId().toString())
            .objectType(Constants.JOB_EXECUTION_OBJECT)
            .payload(nextRun)
            .build());
    }

    @PostMapping(path = "/{generateDataJobId}/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> scheduleJob(@Valid @NotBlank @PathVariable String generateDataJobId) {

        GenerateDataJob dataJob = generateDataJobRepository.findByExternalId(UUID.fromString(generateDataJobId)).orElseThrow();
        authorize(dataJob.getWorkspace(), false);
        jobSchedulerService.scheduleJob(dataJob);

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(dataJob.getExternalId().toString())
            .objectType(Constants.JOB_EXECUTION_OBJECT)
            .payload(jobSchedulerService.monitorFutureTask(dataJob))
            .build());
    }

    @DeleteMapping(path = "/{generateDataJobId}/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> cancelScheduleJob(@Valid @NotBlank @PathVariable String generateDataJobId) {

        GenerateDataJob dataJob = generateDataJobRepository.findByExternalId(UUID.fromString(generateDataJobId)).orElseThrow();
        authorize(dataJob.getWorkspace(), false);
        jobSchedulerService.cancelJob(dataJob);

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(dataJob.getExternalId().toString())
            .objectType(Constants.JOB_EXECUTION_OBJECT)
            .build());
    }

    @GetMapping(path = "/{generateDataJobId}/logs")
    @ResponseBody
    public ResponseEntity<ApiResponse> getJobExecutionLogs(
        @PathVariable @NotBlank String generateDataJobId,
        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
        @RequestParam(value = "size", required = false, defaultValue = "100") int size,
        @RequestParam(value = "sortOrder", required = false, defaultValue = "DESC") String sortOrder,
        @RequestParam(value = "properties", required = false, defaultValue = "id") String[] properties) {

        GenerateDataJob generateDataJob = generateDataJobRepository.findByExternalId(UUID.fromString(generateDataJobId)).orElseThrow();
        authorize(generateDataJob.getWorkspace(),false);

        Pageable pageRequest = PageRequest.of(page, size, Sort.by(Direction.valueOf(sortOrder), properties));

        Page<JobExecutionResponse> jobExecutions = jobExecutionRepository
                .findAllByDataJob(generateDataJob, pageRequest)
                .map(je -> {
                    JobExecutionResponse response = JobExecutionMapper.INSTANCE.jobExecutionToJobExecutionResponse(je);
                    if (generateDataJob.getReplayable()) {
                        generateDataJob.getDataset().forEach(d -> {
                            generateDataJob.getSinks().forEach(s -> {
                                String path = generateDataService.getReplayPath(je, s, d);
                                response.setReplayLogFound(Files.exists(Paths.get(path)));
                            });
                        });
                    }
                    return response;
                });

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .objectType(Constants.JOB_EXECUTION_OBJECT)
            .payload(jobExecutions)
            .build());
    }

    @PostMapping(path = "/{jobExecutionId}/replay", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ApiResponse> replayJob(@Valid @NotBlank @PathVariable String jobExecutionId) {

        JobExecution jobExecution = jobExecutionRepository.findByExternalId(UUID.fromString(jobExecutionId)).orElseThrow();
        authorize(jobExecution.getDataJob().getWorkspace(),false);
        generateDataService.replayData(jobExecution);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(jobExecution.getExternalId().toString())
                                         .objectType(Constants.JOB_EXECUTION_OBJECT)
                                         .build());
    }

}

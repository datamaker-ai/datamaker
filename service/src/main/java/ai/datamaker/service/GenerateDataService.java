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

import ai.datamaker.exception.CancelJobException;
import ai.datamaker.generator.DataGenerator;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.job.GenerateDataJob;
import ai.datamaker.model.job.JobExecution;
import ai.datamaker.repository.GenerateDataJobRepository;
import ai.datamaker.repository.JobExecutionRepository;
import ai.datamaker.sink.DataOutputSink;
import ai.datamaker.utils.stream.TeeOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class GenerateDataService {

    @Autowired
    private GenerateDataJobRepository dataJobRepository;

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @Autowired
    private DatasetService datasetService;

    @Autowired
    private GenerateDataService generateDataService;

    @Autowired
    private CachingService cachingService;

    @Value("${replay.path}")
    private String replayPath;

    //@CacheEvict(value="dashboard", allEntries=true)
    @Transactional
    public JobExecution execute(Long jobId) {
        cachingService.evictAllCacheValues("dashboard");
        GenerateDataJob generateDataJob =  dataJobRepository.findById(jobId).orElseThrow();
        return execute(generateDataJob, true);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persistJobExecution(JobExecution jobExecution) {
        jobExecutionRepository.saveAndFlush(jobExecution);
    }

    public JobExecution replayData(JobExecution previousJob) {
        cachingService.evictAllCacheValues("dashboard");
        final JobExecution jobExecution = new JobExecution();
        jobExecution.setDataJob(previousJob.getDataJob());
        jobExecution.setReplay(true);
        jobExecution.setState(JobExecution.JobExecutionState.RUNNING);
        GenerateDataJob job = previousJob.getDataJob();

        try {
            job.getDataset().forEach(dataset -> {
                job.getSinks()
                    .forEach(sink -> {
                        if (Thread.currentThread().isInterrupted()) {
                            throw new CancelJobException("Job was cancelled");
                        }

                        JobConfig sinkJobConfig = job.getConfig().containsKey(sink.getClass().getName()) ?
                                (JobConfig) job.getConfig().get(sink.getClass().getName()) :
                                JobConfig.EMPTY;

                        sinkJobConfig.setDataset(dataset);
                        sinkJobConfig.setJobExecution(jobExecution);
                        sinkJobConfig.setGenerateDataJob(job);

                        try (OutputStream sinkStream = job.getUseBuffer() ?
                                new BufferedOutputStream(sink.getOutputStream(sinkJobConfig), job.getBufferSize()) :
                                sink.getOutputStream(sinkJobConfig)) {

                            // generateData(job, dataset, sink, sinkJobConfig, sinkStream, jobExecution);
                            FileInputStream fileInputStream = new FileInputStream(getReplayPath(previousJob, sink, dataset));
                            fileInputStream.transferTo(sinkStream);

                        } catch (Exception e) {
                            if (e instanceof IllegalStateException) {
                                throw (IllegalStateException) e;
                            } else {
                                throw new IllegalStateException(e.getMessage(), e);
                            }
                        }
                    });
                jobExecution.getResults().add(String.format("Dataset %s records generated: %d", dataset.getName(), dataset.getNumberOfRecords()));
            });

            jobExecution.setIsSuccess(true);
            jobExecution.setState(JobExecution.JobExecutionState.COMPLETED);

        } catch (Throwable t) {
            if (Thread.currentThread().isInterrupted()) {
                jobExecution.setState(JobExecution.JobExecutionState.CANCELLED);
                jobExecution.setCancelTime(new Date());
                log.warn("schedule job interrupted {}", job.getName());
            } else {
                log.error("Error: ", t);
                jobExecution.setState(JobExecution.JobExecutionState.FAILED);
            }
            jobExecution.setIsSuccess(false);
            jobExecution.getErrors().add(t.getLocalizedMessage());
            String stackTrace = ExceptionUtils.getStackTrace(t);
            jobExecution.getErrors().add(stackTrace.substring(0, Math.min(stackTrace.length(), 65535)));

        } finally {
            jobExecution.setEndTime(new Date());
            generateDataService.persistJobExecution(jobExecution);
        }

        return jobExecution;
    }

    //@CacheEvict(value="dashboard", allEntries=true)
    public JobExecution execute(GenerateDataJob job, boolean persist) {
        cachingService.evictAllCacheValues("dashboard");

        final JobExecution jobExecution = new JobExecution();
        jobExecution.setDataJob(job);
        jobExecution.setState(JobExecution.JobExecutionState.RUNNING);
        if (persist) {
            //jobExecutionRepository.saveAndFlush(jobExecution);
            generateDataService.persistJobExecution(jobExecution);
        }

        final Long numberOfRecords = job.getRandomizeNumberOfRecords() ?
            ThreadLocalRandom.current().nextLong(1, job.getNumberOfRecords()) :
            job.getNumberOfRecords();

        try {
            job.getDataset().forEach(dataset -> {
                dataset.setFlushOnEveryRecord(job.getFlushOnEveryRecord());
                if (job.getThreadPoolSize() != null) {
                    dataset.setThreadPoolSize(numberOfRecords <= job.getThreadPoolSize() ? 1 : job.getThreadPoolSize());
                }
                dataset.setNumberOfRecords(numberOfRecords);
                jobExecution.setNumberOfRecords(numberOfRecords);

                dataset.setRandomizeNumberRecords(job.getRandomizeNumberOfRecords());
                job.getSinks()
                    .forEach(sink -> {

                        if (Thread.currentThread().isInterrupted()) {
                            throw new CancelJobException("Job was cancelled");
                        }

                        JobConfig sinkJobConfig = job.getConfig().containsKey(sink.getClass().getName()) ?
                            (JobConfig) job.getConfig().get(sink.getClass().getName()) :
                            JobConfig.EMPTY;

                        sinkJobConfig.setDataset(dataset);
                        sinkJobConfig.setJobExecution(jobExecution);
                        sinkJobConfig.setGenerateDataJob(job);

                        // TODO determine if sink is flushable / buffer enabled
                        // sink.flushable();
                        // dataset.setFlushOnEveryRecord(false);

//                        if (sink.getClass().isAnnotationPresent(DataOutputSinkType.class)) {
//                            DataOutputSinkType dataOutputSinkType = sink.getClass().getAnnotation(DataOutputSinkType.class);
//                            if (dataOutputSinkType.encrypted()) {
//
//                            }
//                            if (dataOutputSinkType.compressed()) {
//
//                            }
//                        }

                        try (OutputStream sinkStream = job.getUseBuffer() ?
                            new BufferedOutputStream(sink.getOutputStream(sinkJobConfig), job.getBufferSize()) :
                            sink.getOutputStream(sinkJobConfig)) {

                            generateData(job, dataset, sink, sinkJobConfig, sinkStream, jobExecution);

                        } catch (Exception e) {
                            if (e instanceof IllegalStateException) {
                                throw (IllegalStateException) e;
                            } else {
                                throw new IllegalStateException(e.getMessage(), e);
                            }
                        }
                    });
                jobExecution.getResults().add(String.format("Dataset %s records generated: %d", dataset.getName(), dataset.getNumberOfRecords()));
            });

            jobExecution.setIsSuccess(true);
            jobExecution.setState(JobExecution.JobExecutionState.COMPLETED);

        } catch (Throwable t) {
            if (Thread.currentThread().isInterrupted()) {
                jobExecution.setState(JobExecution.JobExecutionState.CANCELLED);
                jobExecution.setCancelTime(new Date());
                log.warn("schedule job interrupted {}", job.getName());
            } else {
                log.error("Error: ", t);
                jobExecution.setState(JobExecution.JobExecutionState.FAILED);
            }
            jobExecution.setIsSuccess(false);
            jobExecution.getErrors().add(t.getLocalizedMessage());
            String stackTrace = ExceptionUtils.getStackTrace(t);
            jobExecution.getErrors().add(stackTrace.substring(0, Math.min(stackTrace.length(), 65535)));

        } finally {
            jobExecution.setEndTime(new Date());
            if (persist) {
                generateDataService.persistJobExecution(jobExecution);
                //jobExecutionRepository.save(jobExecution);
            }
        }

        return jobExecution;
    }

    private void generateData(GenerateDataJob job,
                              Dataset dataset,
                              DataOutputSink sink,
                              JobConfig sinkJobConfig,
                              OutputStream sinkStream,
                              JobExecution jobExecution) throws Exception {
        DataGenerator dataGenerator = job.getGenerator();

        if (sink.accept(dataGenerator.getDataType())) {
            JobConfig generatorJobConfig = job.getConfig().containsKey(dataGenerator.getClass().getName()) ?
                (JobConfig) job.getConfig().get(dataGenerator.getClass().getName()) :
                JobConfig.EMPTY;

            if (job.getReplayable()) {
                Files.createDirectories(Paths.get(replayPath));
                List<JobExecution> jobExecutions = jobExecutionRepository.findAllByDataJobOrderByStartTimeDesc(job);
                int count = 0;
                for (JobExecution je : jobExecutions) {
                    Path tempHistory = Paths.get(getReplayPath(je, sink, dataset));
                    if (Files.exists(tempHistory)) {
                        if (count > job.getReplayHistorySize()) {
                            try {
                                Files.deleteIfExists(tempHistory);
                                log.info("Delete old history file {}", tempHistory);
                            } catch (IOException e) {
                                log.warn("Exception while deleting: {}", getReplayPath(je, sink, dataset), e);
                            }
                        }
                        count++;
                    }
                }

                FileOutputStream fileOutputStream = new FileOutputStream(getReplayPath(jobExecution, sink, dataset));
                TeeOutputStream teeStream = new TeeOutputStream(sinkStream, fileOutputStream);

                dataGenerator.generate(dataset,
                                       sink.encryptCompressStream(sinkJobConfig, teeStream),
                                       generatorJobConfig);
            } else {
                dataGenerator.generate(dataset,
                                       sink.encryptCompressStream(sinkJobConfig, sinkStream),
                                       generatorJobConfig);
            }
        } else {
            throw new IllegalStateException(String.format("Sink %s is not able to process data type %s",
                                                          sink.getClass().getSimpleName(),
                                                          dataGenerator.getDataType()));
        }
    }

    public String getReplayPath(JobExecution jobExecution, DataOutputSink sink, Dataset dataset) {
        return String.format("%s%s%s-%s-%s",
                             replayPath,
                             FileSystems.getDefault().getSeparator(),
                             jobExecution.getExternalId().toString(),
                             dataset.getName(),
                             sink.getClass().getSimpleName());
    }

}

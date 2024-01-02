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

import ai.datamaker.model.Constants;
import ai.datamaker.model.job.GenerateDataJob;
import ai.datamaker.model.job.JobExecution;
import ai.datamaker.repository.GenerateDataJobRepository;
import ai.datamaker.repository.JobExecutionRepository;
import ai.datamaker.utils.scheduling.RandomTrigger;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class JobSchedulerService {

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private JobExecutionRepository jobExecutionRepository;

    @Autowired
    private GenerateDataService generateDataService;

    @Autowired
    private GenerateDataJobRepository dataJobRepository;

    private final Map<Long, ScheduledFuture<?>> currentSchedule = Maps.newHashMap();

    @Autowired
    private CachingService cachingService;

//    @Autowired
//    public JobSchedulerService(TaskScheduler taskScheduler, GenerateDataService generateDataService) {
//        this.taskScheduler = taskScheduler;
//        this.generateDataService = generateDataService;
//    }

    @PostConstruct
    public void init() {
        // TODO spread schedule across worker nodes
        // inject discoveryclient
        // call web service to schedule job based on random number of clients
        jobExecutionRepository.cancelRunning();
        try {
            StreamSupport
                .stream(dataJobRepository.findAll().spliterator(), false)
                .filter(dj -> StringUtils.isNotBlank(dj.getSchedule()) && !Constants.SCHEDULE_ONCE.equals(dj.getSchedule()))
                .forEach(this::scheduleJob);
        } catch (RuntimeException e) {
            log.warn("Error while initializing schedule jobs", e);
        }
    }

    public void cancelJob(GenerateDataJob dataJob) {
        ScheduledFuture<?> future = currentSchedule.remove(dataJob.getId());

        // get current job execution
        //System.out.println(future.getDelay(TimeUnit.MILLISECONDS));

        if (future != null && !future.isDone()) {
            future.cancel(true);
        }

        cachingService.evictAllCacheValues("dashboard");
        // TODO Save job execution state
    }

    public Long monitorFutureTask(GenerateDataJob dataJob) {
        ScheduledFuture<?> future = currentSchedule.get(dataJob.getId());

        // get current job execution
        //System.out.println(future.getDelay(TimeUnit.MILLISECONDS));

        if (future != null && !future.isDone()) {
            return future.getDelay(TimeUnit.SECONDS);
        }

        return null;
    }

    private void runJob(final GenerateDataJob job) {
        JobExecution jobExecution = null;
        try {
            log.debug("Running schedule {}", job.getSchedule());
            jobExecution = generateDataService.execute(job.getId());

        } catch (Throwable t) {
            // If thread is waiting (IO, Sleep): will throw InterruptedException exception else flag is set
            if (t instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            if (jobExecution != null) {
                jobExecution.getErrors().add(t.getLocalizedMessage());
                jobExecution.getErrors().add(ExceptionUtils.getStackTrace(t));
            }

        } finally {

            if (jobExecution != null && Thread.currentThread().isInterrupted()) {
                jobExecution.setState(JobExecution.JobExecutionState.CANCELLED);
                log.warn("schedule job interrupted {}", job.getName());
            }
        }
    }

    // TODO show currently scheduled tasks vs one shot
    // TODO schedule job execution or generated data job
    public void scheduleJob(final GenerateDataJob job) {
        if (currentSchedule.containsKey(job.getId())) {
            cancelJob(job);
        }

        if (Constants.SCHEDULE_ONCE.equals(job.getSchedule())) {
            ScheduledFuture<?> future = taskScheduler.schedule(() -> {
                log.debug("Running schedule {}", job.getSchedule());
                generateDataService.execute(job.getId());
            }, DateTime.now().minusMinutes(1).toDate());

            currentSchedule.put(job.getId(), future);
        } else if (job.getSchedule().startsWith(Constants.SCHEDULE_RANDOM)) {
            RandomTrigger randomTrigger = new RandomTrigger();
            String[] randomConfig = job.getSchedule().split(" ");

            if (randomConfig.length == 3) {
                randomTrigger.setMinDelayInSeconds(Long.parseLong(randomConfig[1]));
                randomTrigger.setMaxDelayInSeconds(Long.parseLong(randomConfig[2]));
            }

            ScheduledFuture<?> future = taskScheduler.schedule(() -> {
                log.debug("Running schedule {}", job.getSchedule());
                generateDataService.execute(job.getId());
            }, randomTrigger);

            currentSchedule.put(job.getId(), future);

        } else {
            CronTrigger cronTrigger = new CronTrigger(job.getSchedule());

            ScheduledFuture<?> future = taskScheduler.schedule(() -> {
                log.debug("Running schedule {}", job.getSchedule());
                generateDataService.execute(job.getId());
            }, cronTrigger);

            currentSchedule.put(job.getId(), future);
        }
    }

}

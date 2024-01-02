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

package ai.datamaker.integration;

import ai.datamaker.model.job.GenerateDataJob;
import ai.datamaker.model.job.JobExecution;
import ai.datamaker.repository.JobExecutionRepository;
import ai.datamaker.service.CachingService;
import ai.datamaker.service.GenerateDataService;
import ai.datamaker.service.JobSchedulerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobSchedulerServiceIntegrationTest {

    @Mock
    private GenerateDataService generateDataService;

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @Mock
    private CachingService cachingService;

    @InjectMocks
    private JobSchedulerService schedulerService;

    private ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();


    @BeforeEach
    public void init() {
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        threadPoolTaskScheduler.initialize();

        ReflectionTestUtils.setField(schedulerService, "taskScheduler", threadPoolTaskScheduler);
    }

    @AfterEach
    public void destroy() {
        threadPoolTaskScheduler.shutdown();
    }

    @Test
    void cancelJob() throws InterruptedException {
        JobExecution jobExecution = new JobExecution();
        // when(generateDataService.execute(any(GenerateDataJob.class))).thenReturn(jobExecution);
        doAnswer((Answer<JobExecution>) invocationOnMock -> {
            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            //Thread.sleep(10*1000L);
            return jobExecution;
        }).when(generateDataService).execute(anyLong());

        GenerateDataJob job = new GenerateDataJob();
        job.setId(System.currentTimeMillis());
        job.setSchedule("* * * * * *");
        schedulerService.scheduleJob(job);

        // Make sure we execute at least once
        Thread.sleep(10*1000L);
        schedulerService.cancelJob(job);

        Thread.sleep(10*1000L);

        verify(generateDataService, atLeastOnce()).execute(eq(job.getId()));
    }

    @Test
    void cancelJob_neverScheduled() throws InterruptedException {

        GenerateDataJob job = new GenerateDataJob();
        job.setId(System.currentTimeMillis());
        job.setSchedule("* * * * * *");
        schedulerService.cancelJob(job);

        verify(generateDataService, never()).execute(eq(job.getId()));
    }

    @Test
    void monitorFutureTask() {
        GenerateDataJob job = new GenerateDataJob();
        job.setId(System.currentTimeMillis());
        job.setSchedule("0 0 * * * *");
        schedulerService.scheduleJob(job);

        long delay = schedulerService.monitorFutureTask(job);


        assertTrue(delay >= 0, "delay is positive " + delay);
    }

    @Test
    void scheduleJob() throws InterruptedException {

        JobExecution jobExecution = new JobExecution();
        jobExecution.setState(JobExecution.JobExecutionState.COMPLETED);
        when(generateDataService.execute(anyLong())).thenReturn(jobExecution);

        GenerateDataJob job = new GenerateDataJob();
        job.setId(System.currentTimeMillis());
        job.setSchedule("* * * * * *");
        schedulerService.scheduleJob(job);

        Thread.sleep(10*1000L);

        verify(generateDataService, atLeastOnce()).execute(eq(job.getId()));
    }

    @Test
    void scheduleJob_runOnce() throws InterruptedException {

        JobExecution jobExecution = new JobExecution();
        jobExecution.setState(JobExecution.JobExecutionState.COMPLETED);
        when(generateDataService.execute(anyLong())).thenReturn(jobExecution);

        GenerateDataJob job = new GenerateDataJob();
        job.setId(System.currentTimeMillis());
        job.setSchedule("once");
        schedulerService.scheduleJob(job);

        Thread.sleep(10*1000L);

        verify(generateDataService, atLeastOnce()).execute(eq(job.getId()));
    }

    @Test
    void scheduleJob_random() throws InterruptedException {

        JobExecution jobExecution = new JobExecution();
        jobExecution.setState(JobExecution.JobExecutionState.COMPLETED);
        when(generateDataService.execute(anyLong())).thenReturn(jobExecution);

        GenerateDataJob job = new GenerateDataJob();
        job.setId(System.currentTimeMillis());
        job.setSchedule("random 1 10");
        schedulerService.scheduleJob(job);

        Thread.sleep(60*1000L);

        verify(generateDataService, atLeastOnce()).execute(eq(job.getId()));
    }
}
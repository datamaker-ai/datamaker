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

import ai.datamaker.generator.JsonGenerator;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.type.AddressField;
import ai.datamaker.model.field.type.AgeField;
import ai.datamaker.model.field.type.FloatField;
import ai.datamaker.model.field.type.SequenceField;
import ai.datamaker.model.job.GenerateDataJob;
import ai.datamaker.model.job.JobExecution;
import ai.datamaker.model.job.JobExecution.JobExecutionState;
import ai.datamaker.service.CachingService;
import ai.datamaker.service.GenerateDataService;
import ai.datamaker.repository.JobExecutionRepository;
import ai.datamaker.sink.base.StringOutputSink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateDataServiceTest {

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @Mock
    private CachingService cachingService;

    @InjectMocks
    private GenerateDataService generateDataService;

    @Test
    void execute() throws Exception {

        ReflectionTestUtils.setField(generateDataService, "generateDataService", generateDataService);

        GenerateDataJob dataJob = new GenerateDataJob();
        dataJob.getSinks().add(new StringOutputSink());
        //dataJob.setGenerator(CsvGenerator.builder().encoding("UTF-8").build());
        dataJob.setGenerator(new JsonGenerator());

        JobExecution jobExecution = new JobExecution();
        when(jobExecutionRepository.saveAndFlush(any())).thenReturn(jobExecution);
        // when(jobExecutionRepository.save(any())).thenReturn(jobExecution);

        Dataset dataset = new Dataset("test", Locale.getDefault());
        dataset.setNumberOfRecords(1024L);
        dataset.addField(new SequenceField("id", Locale.ENGLISH));
        dataset.addField(new AgeField("age", Locale.ENGLISH));
        dataset.addField(new FloatField("balance", Locale.ENGLISH));
        dataset.addField(new AddressField("address", Locale.ENGLISH));
        dataset.setExportHeader(true);
        dataJob.getDataset().add(dataset);

        jobExecution = generateDataService.execute(dataJob, true);

        ArgumentCaptor<JobExecution> jobExecutionArgumentCaptor = ArgumentCaptor.forClass(JobExecution.class);
        verify(jobExecutionRepository, atLeastOnce()).saveAndFlush(jobExecutionArgumentCaptor.capture());

        assertEquals(JobExecutionState.COMPLETED, jobExecutionArgumentCaptor.getValue().getState());

        assertFalse(dataset.getPrimaryKeyValues().isEmpty());
        assertTrue(jobExecution.getIsSuccess());
    }
}
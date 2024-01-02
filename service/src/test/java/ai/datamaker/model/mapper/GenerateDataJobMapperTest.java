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

package ai.datamaker.model.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.Workspace;
import ai.datamaker.model.forms.GenerateDataJobForm;
import ai.datamaker.model.job.GenerateDataJob;
import ai.datamaker.model.mapper.GenerateDataJobMapper;
import ai.datamaker.model.response.GenerateDataJobResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GenerateDataJobMapperTest {

    GenerateDataJobMapper generateDataJobMapper = GenerateDataJobMapper.INSTANCE;

    @Test
    void generateDataJobToGenerateDataJobResponse() {
        GenerateDataJob generateDataJob = new GenerateDataJob();
        generateDataJob.setName("test");
        generateDataJob.setDescription("description");

        Workspace workspace = new Workspace();
        generateDataJob.setWorkspace(workspace);

        Dataset dataset = new Dataset();
        dataset.setName("data");
        generateDataJob.getDataset().add(dataset);

        GenerateDataJobResponse generateDataJobResponse = generateDataJobMapper.generateDataJobToGenerateDataJobResponse(generateDataJob);

        assertEquals("test", generateDataJobResponse.getName());
        assertEquals("description", generateDataJobResponse.getDescription());
    }

    @Test
    void generateDataJobFormToGenerateDataJob() {
        GenerateDataJobForm generateDataJobForm = new GenerateDataJobForm();
        generateDataJobForm.setDescription("description");
        generateDataJobForm.setName("test");
        generateDataJobForm.setWorkspaceId(UUID.randomUUID().toString());

        GenerateDataJob generateDataJob = generateDataJobMapper.generateDataJobFormToGenerateDataJob(generateDataJobForm);

        assertEquals("test", generateDataJob.getName());
        assertEquals("description", generateDataJob.getDescription());
    }
}
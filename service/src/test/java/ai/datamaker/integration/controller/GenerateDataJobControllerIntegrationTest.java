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

package ai.datamaker.integration.controller;

import ai.datamaker.config.SpringTestConfiguration;
import ai.datamaker.controller.GenerateDataJobController;
import ai.datamaker.generator.CsvGenerator;
import ai.datamaker.model.job.GenerateDataJob;
import ai.datamaker.repository.GenerateDataJobRepository;
import ai.datamaker.repository.WorkspaceRepository;
import ai.datamaker.sink.base.StringOutputSink;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsIterableContaining;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({SpringExtension.class})
@WebMvcTest(controllers = GenerateDataJobController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(classes = {SpringTestConfiguration.class})
@WithMockUser(roles = {"ADMIN"}, username = "admin")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GenerateDataJobControllerIntegrationTest {

    @Autowired
    private GenerateDataJobController controller;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GenerateDataJobRepository generateDataJobRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Order(1)
    @Test
    void get() throws Exception {
        String externalId = "d86f8326-281d-11ea-978f-2e728ce88127";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/generate-data-job/{externalId}", externalId)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.externalId", Is.is(externalId)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.name", Is.is("datajob-json")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Order(2)
    @Test
    void list() throws Exception {
        String externalId = "d86f8326-281d-11ea-978f-2e728ce88128";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/generate-data-job/workspace/" + externalId)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$['payload']", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload[*].name", IsIterableContaining.hasItems("datajob-csv", "datajob-xml")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Order(3)
    @Test
    void create() throws Exception {

        String generateDataJobJson = "{\"name\":\"created\", "
            + "\"description\":\"from junit\", "
            + "\"numberOfRecords\": 43, "
            + "\"schedule\":\"3 0 0 * * *\", "
            + "\"size\": 999, "
            + "\"useBuffer\": true, "
            + "\"bufferSize\": 1030, "
            + "\"workspaceId\":\"d86f8326-281d-11ea-978f-2e728ce88127\"}";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/generate-data-job")
                                .content(generateDataJobJson)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.externalId", IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        // Assert repository
        GenerateDataJob dataJob = generateDataJobRepository.findByNameAndWorkspace("created",
            workspaceRepository.findByExternalId(UUID.fromString("d86f8326-281d-11ea-978f-2e728ce88127")).orElseThrow())
            .orElseThrow();

        assertEquals("created", dataJob.getName());
        assertEquals("from junit", dataJob.getDescription());
        assertEquals(43L, dataJob.getNumberOfRecords());
        assertEquals("3 0 0 * * *", dataJob.getSchedule());
        assertEquals(999, dataJob.getSize());
        assertEquals(true, dataJob.getUseBuffer());
        assertEquals(1030, dataJob.getBufferSize());
        assertEquals("d86f8326-281d-11ea-978f-2e728ce88127", dataJob.getWorkspace().getExternalId().toString());
    }

    @Order(4)
    @Test
    void create_name_already_exists() throws Exception {
        String generateDataJobJson = "{\"name\":\"datajob-json\", "
            + "\"description\":\"from junit\", "
            + "\"schedule\":\"once\", "
            + "\"workspaceId\":\"d86f8326-281d-11ea-978f-2e728ce88127\"}";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/generate-data-job")
                                .content(generateDataJobJson)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail", Is.is("name datajob-json already exist")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Order(5)
    @Test
    void update() throws Exception {
        String generateDataJobJson = "{\"name\":\"updated\", "
            + "\"schedule\": \"*/5 * * * * *\", "
            + "\"description\":\"from junit\", "
            + "\"workspaceId\":\"d86f8326-281d-11ea-978f-2e728ce88127\"}";
        String externalId = "d86f8326-281d-11ea-978f-2e728ce88129";

        mockMvc.perform(MockMvcRequestBuilders.put("/api/generate-data-job/{externalId}", externalId)
                                .content(generateDataJobJson)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.externalId", Is.is(externalId)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        // Assert repository
        GenerateDataJob dataJob = generateDataJobRepository.findByNameAndWorkspace(
            "updated",
            workspaceRepository.findByExternalId(UUID.fromString("d86f8326-281d-11ea-978f-2e728ce88128")).orElseThrow())
            .orElseThrow();
    }

    @Test
    @Transactional
    void addDataset() throws Exception {
        String generateDataJobJson = "{\"name\":\"updated\", \"description\":\"from junit\", \"workspaceId\":\"d86f8326-281d-11ea-978f-2e728ce88127\"}";
        String externalId = "d86f8326-281d-11ea-978f-2e728ce88129";
        String datasetId = "d86f8326-281d-11ea-978f-2e728ce88128";

        mockMvc.perform(MockMvcRequestBuilders.put("/api/generate-data-job/{externalId}/dataset/{datasetId}", externalId, datasetId)
            .content(generateDataJobJson)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.externalId", Is.is(externalId)))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        // Assert repository
        GenerateDataJob generateDataJob = generateDataJobRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();

        assertEquals(datasetId, generateDataJob.getDataset().get(0).getExternalId().toString());
    }

    @Test
    @Transactional
    void addSink() throws Exception {
        String externalId = "d86f8326-281d-11ea-978f-2e728ce88129";
        String sinkClassName = StringOutputSink.class.getCanonicalName();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/generate-data-job/{externalId}/sink", externalId)
            .content("{ \"sinkClassName\": \"" + sinkClassName + "\", \"config\": {} }")
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.externalId", Is.is(externalId)))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        // Assert repository
        GenerateDataJob generateDataJob = generateDataJobRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();

        assertTrue(generateDataJob.getSinkNames().contains(sinkClassName));
    }

    @Test
    @Transactional
    void setGenerator() throws Exception {

        String externalId = "d86f8326-281d-11ea-978f-2e728ce88129";
        String generatorClassName = CsvGenerator.class.getCanonicalName();

        mockMvc.perform(MockMvcRequestBuilders.put("/api/generate-data-job/{externalId}/generator", externalId)
            .content("{ \"generatorClassName\": \"" + generatorClassName + "\", \"config\": { \"sink.file.output.filename\":\"/tmp/test.csv\" } }")
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.externalId", Is.is(externalId)))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        // Assert repository
        GenerateDataJob generateDataJob = generateDataJobRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();

        assertEquals(generatorClassName, generateDataJob.getGeneratorName());
        assertNotNull(generateDataJob.getConfig().get(generatorClassName));
    }

    @Order(6)
    @Test
    void update_name_already_exists() throws Exception {
        String generateDataJobJson = "{\"name\":\"updated\", "
            + "\"description\":\"from junit\", "
            + "\"schedule\":\"random 0 10\", "
            + "\"workspaceId\":\"d86f8326-281d-11ea-978f-2e728ce88128\"}";
        String externalId = "d86f8326-281d-11ea-978f-2e728ce88128";

        mockMvc.perform(MockMvcRequestBuilders.put("/api/generate-data-job/{externalId}", externalId)
                                .content(generateDataJobJson)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail", Is.is("name updated already exist")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Order(7)
    @Test
    void delete() throws Exception {
        String externalId = "d86f8326-281d-11ea-978f-2e728ce88128";

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/generate-data-job/{externalId}", externalId)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.externalId", Is.is(externalId)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        assertTrue(generateDataJobRepository.findByExternalId(UUID.fromString(externalId)).isEmpty());
    }


}
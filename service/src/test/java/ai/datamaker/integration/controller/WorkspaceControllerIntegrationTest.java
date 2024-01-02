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
import ai.datamaker.controller.WorkspaceController;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.Workspace;
import ai.datamaker.repository.DatasetRepository;
import ai.datamaker.repository.WorkspaceRepository;
import ai.datamaker.utils.CapturingMatcher;
import ai.datamaker.utils.WithMockCustomUser;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsIterableContaining;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.Disabled;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = WorkspaceController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(classes = {SpringTestConfiguration.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WorkspaceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @Test
    @WithMockUser(roles = {"ADMIN"}, username = "admin")
    @Transactional
    void moveAllDatasetFrom() throws Exception {
        String workspaceFromId = "d86f8326-281d-11ea-978f-2e728ce88130";
        Workspace workspaceFrom = workspaceRepository.findByExternalId(UUID.fromString(workspaceFromId)).orElseThrow();
        Dataset dataset = createDataset(workspaceFrom);

        String workspaceToId = "d86f8326-281d-11ea-978f-2e728ce88129";

        mockMvc.perform(MockMvcRequestBuilders.post(String.format("/api/workspace/move/%s/%s", workspaceFromId, workspaceToId))
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        assertTrue(workspaceRepository.findByExternalId(
                UUID.fromString(workspaceToId)).get().getDatasets().contains(dataset));
    }

    @Transactional
    Dataset createDataset(Workspace workspaceFrom) {
        Dataset dataset = new Dataset();
        dataset.setName("copy from");
        dataset.setWorkspace(workspaceFrom);
        return datasetRepository.save(dataset);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"}, username = "admin")
    @Transactional
    @Disabled
    void moveDatasetFrom() throws Exception {

        String datasetId = "d86f8326-281d-11ea-978f-2e728ce88128";
        String workspaceToId = "d86f8326-281d-11ea-978f-2e728ce88130";

        mockMvc.perform(MockMvcRequestBuilders.post(String.format("/api/workspace/move/dataset/%s/%s", datasetId, workspaceToId))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        Dataset dataset = datasetRepository.findByExternalId(UUID.fromString(datasetId)).get();
        Workspace workspace = dataset.getWorkspace();
        assertEquals(
                workspace.getExternalId(),
                UUID.fromString(workspaceToId));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"}, username = "admin")
    void get() throws Exception {

        String workspaceId = "d86f8326-281d-11ea-978f-2e728ce88130";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/workspace/{workspaceId}", workspaceId)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.externalId", Is.is(workspaceId)))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    @WithMockCustomUser
    @Order(7)
    void get_notOwner_forbidden() throws Exception {

        String workspaceId = "d86f8326-281d-11ea-978f-2e728ce88131";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/workspace/{workspaceId}", workspaceId)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isForbidden())
            .andExpect(MockMvcResultMatchers.jsonPath("$.detail", Is.is("User does not have privilege to modify workspace my-workspace-datasets")))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    @WithMockCustomUser
    @Order(8)
    void get_notOwner_butSameGroup_OK() throws Exception {

        String workspaceId = "d86f8326-281d-11ea-978f-2e728ce88128";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/workspace/" + workspaceId)
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.payload.externalId", Is.is(workspaceId)))
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    @WithMockCustomUser
    void create_failed() throws Exception {

        String workspaceJson = "{\"name\":null, \"description\":\"from junit\", \"groupPermissions\":\"FULL\", \"owner\": \"rob\"}";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/workspace")
                                .content(workspaceJson)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail", Is.is("name=Name is mandatory")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    @Order(1)
    @WithMockCustomUser
    void create() throws Exception {

        String workspaceJson = "{\"name\":\"created\", \"description\":\"from junit\", \"groupPermissions\":\"FULL\", \"owner\": \"rob\"}";
        CapturingMatcher capturingMatcher = new CapturingMatcher();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/workspace")
                                .content(workspaceJson)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.externalId", IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.externalId", capturingMatcher))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        String externalId = (String) capturingMatcher.getLastMatched();
        Workspace workspace = workspaceRepository.findByExternalId(UUID.fromString(externalId)).orElseThrow();
        assertEquals("created", workspace.getName());
        assertEquals(Workspace.WorkspacePermissions.FULL, workspace.getGroupPermissions());
        assertEquals("rob", workspace.getOwner().getUsername());
        assertEquals("from junit", workspace.getDescription());
    }

    @Test
    @Order(2)
    @WithMockCustomUser
    void list() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/workspace")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$['payload']", hasSize(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload[*].name",
                                                          IsIterableContaining.hasItems("my-workspace-ro", "my-workspace-none", "created", "change-owner")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    @Order(3)
    @WithMockUser(roles = {"ADMIN"}, username = "admin")
    @Transactional
    void delete() throws Exception {
        String workspaceId = "d86f8326-281d-11ea-978f-2e728ce88128";

        assertTrue(datasetRepository.findByExternalId(UUID.fromString("d86f8326-281d-11ea-978f-2e728ce88128")).isPresent());

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/workspace/" + workspaceId)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.externalId", Is.is(workspaceId)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        assertTrue(workspaceRepository.findByExternalId(UUID.fromString(workspaceId)).isEmpty());
        assertTrue(datasetRepository.findByExternalId(UUID.fromString("d86f8326-281d-11ea-978f-2e728ce88128")).isEmpty());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"}, username = "admin")
    void update() throws Exception {
        String workspaceId = "d86f8326-281d-11ea-978f-2e728ce88127";

        String workspaceJson = "{\"name\":\"updated\", \"description\":\"updated from junit\", \"groupPermissions\":\"NONE\", \"owner\": \"admin\"}";

        mockMvc.perform(MockMvcRequestBuilders.put("/api/workspace/{workspaceId}", workspaceId)
                                .content(workspaceJson)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.externalId", Is.is(workspaceId)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        Workspace workspace = workspaceRepository.findByExternalId(UUID.fromString(workspaceId)).orElseThrow();
        assertEquals("updated", workspace.getName());
        assertEquals(Workspace.WorkspacePermissions.NONE, workspace.getGroupPermissions());
        assertEquals("updated from junit", workspace.getDescription());
    }

    @Test
    @Order(4)
    @WithMockCustomUser
    void updateFailed_insufficientPermissions() throws Exception {
        String workspaceId = "d86f8326-281d-11ea-978f-2e728ce88127";

        String workspaceJson = "{\"name\":\"updated\", \"description\":\"updated from junit\", \"groupPermissions\":\"NONE\", \"owner\": \"rob\"}";

        mockMvc.perform(MockMvcRequestBuilders.put("/api/workspace/" + workspaceId)
                                .content(workspaceJson)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail", Is.is("User does not have privilege to modify workspace my-workspace-full")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

    }

    @Test
    @WithMockCustomUser
    @Order(5)
    void changeOwner() throws Exception {
        String workspaceId = "d86f8326-281d-11ea-978f-2e728ce88132";
        String newOwnerId = "d86f86f0-281d-11ea-978f-2e728ce88123";

        assertEquals("abc@gmail.com", workspaceRepository.findByExternalId(UUID.fromString(workspaceId)).get().getOwner().getUsername());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/workspace/{workspaceId}/owner/{ownerId}", workspaceId, newOwnerId)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.externalId", Is.is(workspaceId)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        assertEquals(newOwnerId, workspaceRepository.findByExternalId(UUID.fromString(workspaceId)).get().getOwner().getExternalId().toString());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"}, username = "admin")
    @Order(6)
    void changeUserGroup() throws Exception {
        String workspaceId = "d86f8326-281d-11ea-978f-2e728ce88127";
        String newUserGroupId = "d86f8326-281d-11ea-978f-2e728ce88123";

        assertNull(workspaceRepository.findByExternalId(UUID.fromString(workspaceId)).get().getUserGroup());

        mockMvc.perform(MockMvcRequestBuilders.put("/api/workspace/{workspaceId}/group/{newUserGroupId}", workspaceId, newUserGroupId)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.externalId", Is.is(workspaceId)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        assertEquals(newUserGroupId, workspaceRepository.findByExternalId(UUID.fromString(workspaceId)).get().getUserGroup().getExternalId().toString());
    }

}
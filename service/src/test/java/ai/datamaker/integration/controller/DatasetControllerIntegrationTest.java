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
import ai.datamaker.controller.DatasetController;
import ai.datamaker.model.Dataset;
import ai.datamaker.repository.DatasetRepository;
import ai.datamaker.utils.CapturingMatcher;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
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

import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = DatasetController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(classes = {SpringTestConfiguration.class})
@WithMockUser(roles = "ADMIN", username = "admin")
//@ContextConfiguration(classes = { SpringTestConfiguration.class })
public class DatasetControllerIntegrationTest {
 
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DatasetRepository datasetRepository;

    @Test
    void get() {
        fail("implements");
    }

    @Test
    void list() {
        fail("implements");
    }

    @Test
    @Transactional
    void create() throws Exception {
        // Test workspace contains dataset
        String workspaceId = "d86f8326-281d-11ea-978f-2e728ce88131";
        String datasetForm = "{\"name\": \"bob\", \"languageTag\" : \"fr-CA\", \"workspaceId\": \"" + workspaceId + "\"}";
        CapturingMatcher capturingMatcher = new CapturingMatcher();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/dataset")
                                .content(datasetForm)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.externalId", capturingMatcher))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        Dataset dataset = datasetRepository.findByExternalId(UUID.fromString(capturingMatcher.getLastMatched().toString())).orElseThrow();
        assertEquals("bob", dataset.getName());
        assertEquals(Locale.CANADA_FRENCH, dataset.getLocale());
    }

    @Test
    void create_workspaceNotFound() throws Exception {
        // Test workspace contains dataset
        String workspaceId = "d86f8326-281d-11ea-978f-2e728ce88999";
        String dataset = "{\"name\": \"bob\", \"languageTag\" : \"fr-CA\", \"workspaceId\": \"" + workspaceId + "\"}";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/dataset")
                                .content(dataset)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail", Is.is("No value present")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    void create_invalidForm() throws Exception {
        // Test workspace contains dataset
        String workspaceId = "d86f8326-281d-11ea-978f-2e728ce88131";
        String dataset = "{\"name\": \"bob\", \"languageTag2\" : \"\", \"workspaceId\": \"" + workspaceId + "\"}";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/dataset")
                                .content(dataset)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail", Is.is("languageTag=must not be blank")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    @WithMockUser
    void create_insufficientPermissions() throws Exception {
        // Test workspace contains dataset
        String workspaceId = "d86f8326-281d-11ea-978f-2e728ce88131";
        String dataset = "{\"name\": \"bob\", \"languageTag\" : \"fr-CA\", \"workspaceId\": \"" + workspaceId + "\"}";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/dataset")
                                .content(dataset)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail", Is.is("User does not have privilege to modify workspace")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    void delete() throws Exception {
        // Test workspace doesn't contain dataset
        String workspaceId = "d86f8326-281d-11ea-978f-2e728ce88127";
        String datasetId = "d86f8326-281d-11ea-978f-2e728ce88127";

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/dataset/{datasetId}", datasetId)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        assertTrue(datasetRepository.findByExternalId(UUID.fromString("d86f8326-281d-11ea-978f-2e728ce88127")).isEmpty());
    }

    @Test
    void update() throws Exception {
        fail("implements");
    }
}

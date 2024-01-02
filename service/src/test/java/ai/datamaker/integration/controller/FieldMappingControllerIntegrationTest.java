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
import ai.datamaker.controller.FieldMappingController;
import ai.datamaker.model.field.FieldMapping;
import ai.datamaker.repository.FieldMappingRepository;
import ai.datamaker.service.FieldDetectorService;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.hamcrest.core.StringStartsWith;
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

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = FieldMappingController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(classes = {SpringTestConfiguration.class})
@WithMockUser
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FieldMappingControllerIntegrationTest {

    @Autowired
    private FieldMappingRepository repository;

    @Autowired
    private FieldDetectorService fieldDetectorService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void getAllForLanguage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/field-mappings/lang/fr")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$['payload']", hasSize(4)))
                .andExpect(MockMvcResultMatchers.jsonPath("$['payload'][0]['name']", Is.is("Age")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    @Order(2)
    void getAll() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/field-mappings")
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$['payload']", hasSize(21)))
                .andExpect(MockMvcResultMatchers.jsonPath("$['payload'][1]['name']", Is.is("Age")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    @Order(3)
    void create() throws Exception {
        String fieldMappingJson = "{\"name\": \"bob\", \"datasetId\": \"00000000-0000-0000-0000-000000000000\", \"description\": \"\", \"languageTag\": \"it\", \"className\": \"ca.breakpoints.datamaker.model.field.type.NameField\", \"config\": { \"field.name.type\": \"FULL\" } } }";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/field-mappings")
                .content(fieldMappingJson)
                .locale(Locale.ITALIAN)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.externalId", IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        verify(fieldDetectorService, times(1)).init();

        Optional<FieldMapping> fieldMapping = repository.findByMappingKey("bob-it");
        assertTrue(fieldMapping.isPresent());
    }

    @Test
    @Order(4)
    void create_Failed() throws Exception {
        String fieldMappingJson = "{\"name\": \"test\", \"datasetId\": \"00000000-0000-0000-0000-000000000000\", \"description\": \"\", \"languageTag\": \"fr\", \"className\": \"ca.breakpoints.datamaker.model.field.type.NameField\", \"config\": { \"field.name.type\": \"FULL\" } } }";

        mockMvc.perform(MockMvcRequestBuilders.post("/api/field-mappings")
                                .content(fieldMappingJson)
                                .locale(Locale.FRENCH)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail", StringStartsWith.startsWith("field mapping found for key test-fr with external id")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        verify(fieldDetectorService, never()).init();
    }

    @Test
    @Order(5)
    void update_Failed() throws Exception {
        Optional<FieldMapping> fieldMappingTest = repository.findByMappingKey("update-en");

        String fieldMappingJson = "{\"name\": \"test\", \"datasetId\": \"00000000-0000-0000-0000-000000000000\", \"description\": \"\", \"languageTag\": \"en\", \"className\": \"ca.breakpoints.datamaker.model.field.type.NameField\", \"config\": { \"field.name.type\": \"FULL\" } } }";

        mockMvc.perform(MockMvcRequestBuilders.put("/api/field-mappings/" + fieldMappingTest.get().getExternalId())
                                .content(fieldMappingJson)
                                .locale(Locale.ENGLISH)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail", StringStartsWith.startsWith("different field mapping found for key test-en with external id")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        verify(fieldDetectorService, never()).init();
    }

    @Test
    @Order(6)
    void update() throws Exception {
        Optional<FieldMapping> fieldMappingTest = repository.findByMappingKey("test-fr");

        String fieldMappingJson = "{\"name\": \"test\", \"datasetId\": \"00000000-0000-0000-0000-000000000000\", \"description\": \"\", \"languageTag\": \"fr\", \"className\": \"ca.breakpoints.datamaker.model.field.type.NameField\", \"config\": { \"field.name.type\": \"TITLE\" } } }";

        mockMvc.perform(MockMvcRequestBuilders.put("/api/field-mappings/" + fieldMappingTest.get().getExternalId())
                                .content(fieldMappingJson)
                                .locale(Locale.FRANCE)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.externalId", IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        verify(fieldDetectorService, times(1)).init();

        Optional<FieldMapping> fieldMapping = repository.findByMappingKey("test-fr");
        assertTrue(fieldMapping.isPresent());
        assertTrue(fieldMapping.get().getFieldJson().contains("TITLE"));
    }

    @Test
    @Order(7)
    void delete() throws Exception {
        Optional<FieldMapping> fieldMappingTest = repository.findByMappingKey("test-en");

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/field-mappings/" + fieldMappingTest.get().getExternalId())
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.externalId", IsNull.notNullValue()))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        assertTrue(repository.findByMappingKey("test-en").isEmpty());

        verify(fieldDetectorService, times(1)).init();
    }

    @Test
    @Order(8)
    void delete_Failed() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/field-mappings/" + UUID.randomUUID().toString())
                                .locale(Locale.FRENCH)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail", StringStartsWith.startsWith("field mapping not found for external id")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        verify(fieldDetectorService, never()).init();
    }
}
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
import ai.datamaker.controller.FieldController;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.type.ArrayField;
import ai.datamaker.model.field.type.CompanyField;
import ai.datamaker.model.field.type.ComplexField;
import ai.datamaker.model.field.type.GeographicField;
import ai.datamaker.model.field.type.StockField;
import ai.datamaker.model.field.type.StringField;
import ai.datamaker.model.forms.FieldForm;
import ai.datamaker.model.forms.MultipleFieldsForm;
import ai.datamaker.repository.DatasetRepository;
import ai.datamaker.repository.FieldRepository;
import ai.datamaker.utils.CapturingMatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.hamcrest.core.Is;
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

import java.util.Locale;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = FieldController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(classes = {SpringTestConfiguration.class})
@WithMockUser(roles = "ADMIN", username = "admin")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@ContextConfiguration(classes = { SpringTestConfiguration.class })
class FieldControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FieldRepository fieldRepository;

    @Autowired
    private DatasetRepository datasetRepository;

    @Test
    void get() throws Exception {
        String externalId = "d86f8326-281d-11ea-978f-2e728ce88125";

         mockMvc.perform(MockMvcRequestBuilders.get(String.format("/api/field/%s", externalId)))
             .andExpect(MockMvcResultMatchers.status().isOk())
             .andExpect(MockMvcResultMatchers.jsonPath("$.success", Is.is(true)))
             .andExpect(MockMvcResultMatchers.jsonPath("$.payload.externalId", Is.is("d86f8326-281d-11ea-978f-2e728ce88125")))
             .andExpect(MockMvcResultMatchers.jsonPath("$.payload.className", Is.is("ca.breakpoints.datamaker.model.field.type.StringField")))
             .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    @Transactional
    void getComplexField() throws Exception {
        ComplexField field = new ComplexField("nested", Locale.ENGLISH);
        Field reference = fieldRepository.findAll().iterator().next();
        field.getReferences().add(reference);

        StringField stringField = new StringField();
        stringField.setName("string2");
        stringField.setDataset(reference.getDataset());
        field.getReferences().add(stringField);

        field.setDataset(reference.getDataset());
        fieldRepository.save(field);

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/api/field/%s/complex", field.getExternalId().toString())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success", Is.is(true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$['payload']", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$['payload'][1]['name']", Is.is("string2")))
                .andExpect(MockMvcResultMatchers.jsonPath("$['payload'][1]['position']", Is.is(2)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

    }

    @Test
    @Transactional
    void getArrayField() throws Exception {
        ArrayField field = new ArrayField("myarray", Locale.ENGLISH);
        Field reference = fieldRepository.findAll().iterator().next();
        field.setReference(reference);
        field.setDataset(reference.getDataset());
        fieldRepository.save(field);

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/api/field/%s/array", field.getExternalId().toString())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success", Is.is(true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$['payload']", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$['payload'][0]['name']", Is.is("junit")))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    void batchProcess() throws Exception {
        // batch create
        String datasetId = "d86f8326-281d-11ea-978f-2e728ce88127";

        ObjectMapper mapper = new ObjectMapper();
        MultipleFieldsForm multipleFieldsForm = new MultipleFieldsForm();
        multipleFieldsForm.setFields(Lists.newArrayList());
        multipleFieldsForm.setDatasetId(datasetId);

        FieldForm geoloc = new FieldForm();
        geoloc.setName("geo");
        geoloc.setPosition(3);
        geoloc.setDatasetId(datasetId);
        geoloc.setLanguageTag("en");
        geoloc.setClassName(GeographicField.class.getName());
        multipleFieldsForm.getFields().add(geoloc);

        FieldForm stock = new FieldForm();
        stock.setName("stock");
        stock.setPosition(5);
        stock.setDatasetId(datasetId);
        stock.setLanguageTag("en");
        stock.setClassName(StockField.class.getName());
        multipleFieldsForm.getFields().add(stock);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/field/batch")
                                .content(mapper.writeValueAsString(multipleFieldsForm))
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$['payload']", hasSize(2)))
                .andExpect(MockMvcResultMatchers.jsonPath("$['payload'][1]['name']", Is.is("stock")))
                .andExpect(MockMvcResultMatchers.jsonPath("$['payload'][1]['position']", Is.is(5)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    @Transactional
    @Order(1)
    void create() throws Exception {
        String datasetId = "d86f8326-281d-11ea-978f-2e728ce88127";
        String fieldForm = "{\"name\": \"bob\", \"position\": 1, \"languageTag\" : \"fr-CA\", " +
                "\"datasetId\": \"" + datasetId + "\", \"className\": \"ca.breakpoints.datamaker.model.field.type.ArrayField\", " +
                "\"config\": {\"field.array.element\": \"d86f8326-281d-11ea-978f-2e728ce88125\"}}";
        CapturingMatcher capturingMatcher = new CapturingMatcher();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/field")
                                .content(fieldForm)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.externalId", capturingMatcher))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        Field field = fieldRepository.findByExternalId(UUID.fromString(capturingMatcher.getLastMatched().toString())).orElseThrow();
        assertEquals("bob", field.getName());
        assertEquals(Locale.CANADA_FRENCH, field.getLocale());
        assertNotNull(((ArrayField)field).getReference());
    }

    @Test
    @Transactional
    void create_failed() throws Exception {
        String datasetId = "d86f8326-281d-11ea-978f-2e728ce88127";
        String fieldForm = "{\"name\": \"\", " +
                "\"datasetId\": \"" + datasetId + "\", \"className\": \"\", " +
                "\"config\": {\"field.array.element\": \"d86f8326-281d-11ea-978f-2e728ce88125\"}}";
        CapturingMatcher capturingMatcher = new CapturingMatcher();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/field")
                                .content(fieldForm)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail", capturingMatcher))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        assertTrue(capturingMatcher.getLastMatched().toString().contains("name=Name is mandatory"));
        assertTrue(capturingMatcher.getLastMatched().toString().contains("className=must match \"ca\\.breakpoints\\.datamaker\\.model\\.field\\.type.*\""));
        assertTrue(capturingMatcher.getLastMatched().toString().contains("position=must be greater than 0"));
        assertTrue(capturingMatcher.getLastMatched().toString().contains("languageTag=must not be blank"));
    }

    @Test
    @Transactional
    void update() throws Exception {

        String datasetId = "d86f8326-281d-11ea-978f-2e728ce88127";
        String externalId = "d86f8326-281d-11ea-978f-2e728ce88126";

        String fieldForm = "{\"name\": \"my-name2\", \"position\": 2, \"languageTag\" : \"fr-CA\", " +
                "\"datasetId\": \"" + datasetId + "\", \"className\": \"ca.breakpoints.datamaker.model.field.type.NameField\"}";

        CapturingMatcher capturingMatcher = new CapturingMatcher();

        mockMvc.perform(MockMvcRequestBuilders.put(String.format("/api/field/%s", externalId))
                                .content(fieldForm)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.externalId", capturingMatcher))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        Field field = fieldRepository.findByExternalId(UUID.fromString(capturingMatcher.getLastMatched().toString())).orElseThrow();
        assertEquals("my-name2", field.getName());
        assertEquals(2, field.getPosition());
        assertEquals(Locale.CANADA_FRENCH, field.getLocale());
    }

    @Test
    @Transactional
    void update_className() throws Exception {

        String datasetId = "d86f8326-281d-11ea-978f-2e728ce88127";
        String externalId = "d86f8326-281d-11ea-978f-2e728ce88126";

        String fieldForm = "{\"name\": \"my-name\", \"position\": 1, \"languageTag\" : \"fr-CA\", " +
                "\"datasetId\": \"" + datasetId + "\", \"className\": \"ca.breakpoints.datamaker.model.field.type.ArrayField\", " +
                "\"config\": {\"field.array.element\": \"d86f8326-281d-11ea-978f-2e728ce88125\"}}";

        CapturingMatcher capturingMatcher = new CapturingMatcher();

        mockMvc.perform(MockMvcRequestBuilders.put(String.format("/api/field/%s", externalId))
                                .content(fieldForm)
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.externalId", capturingMatcher))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

        Field field = fieldRepository.findByExternalId(UUID.fromString(capturingMatcher.getLastMatched().toString())).orElseThrow();
        assertEquals("my-name", field.getName());
        assertEquals(Locale.CANADA_FRENCH, field.getLocale());
        assertNotNull(ArrayField.class.getName(), field.getClassName());
    }

    @Test
    @Transactional
    void delete() throws Exception {
        String datasetId = "d86f8326-281d-11ea-978f-2e728ce88127";

        CompanyField field = new CompanyField("acme", Locale.ENGLISH);
        field.setDataset(datasetRepository.findByExternalId(UUID.fromString(datasetId)).orElseThrow());
        fieldRepository.save(field);

        mockMvc.perform(MockMvcRequestBuilders.delete(String.format("/api/field/%s", field.getExternalId().toString())))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success", Is.is(true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.externalId", Is.is(field.getExternalId().toString())))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE));

    }

    @Test
    void delete_failed() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.delete(String.format("/api/field/%s", UUID.randomUUID().toString())))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail", Is.is("No value present")));

    }

    @Test
    void setFormatter() {
        fail("implements");
    }
}
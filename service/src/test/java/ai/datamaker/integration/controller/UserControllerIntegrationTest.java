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
import ai.datamaker.controller.UserController;
import ai.datamaker.repository.UserGroupRepository;
import ai.datamaker.repository.UserRepository;
import com.google.common.collect.Iterators;
import org.hamcrest.core.Is;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(classes = {SpringTestConfiguration.class})
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGroupRepository groupRepository;

    @Test
    void get() throws Exception {

        String externalId = Iterators.get(userRepository.findAll().iterator(), 2).getExternalId().toString();

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/api/user/%s", externalId)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.payload.username", Is.is("xyz@email.com")));
    }

    @Test
    void get_invalid() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/api/user/%s",
                                                                 UUID.randomUUID())))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail", StringContains.containsString("No value present")));
    }

    @Test
    void list() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/user"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$['payload']", hasSize(5)))
                .andExpect(MockMvcResultMatchers.jsonPath("$['payload'][1]['username']", Is.is("abc@gmail.com")));
    }

    @Test
    void group() {
        fail("implements");
    }

    @Test
    void listGroups() {
        fail("implements");
    }

    @Test
    void create() {
        fail("implements");
    }

    @Test
    void createGroup() {
        fail("implements");
    }

    @Test
    void delete() {
        fail("implements");
    }

    @Test
    void deleteGroup() {
        fail("implements");
    }

    @Test
    void addGroupToUser() {
        fail("implements");
    }

    @Test
    void changeStatus() {
        fail("implements");
    }

    @Test
    void update() {
        fail("implements");
    }

    @Test
    void changePassword() {
        fail("implements");
    }

    @Test
    void updateGroup() {
        fail("implements");
    }

    @Test
    void logout() {
        fail("implements");
    }

    @Test
    void retrievePrincipal() {
        fail("implements");
    }
}
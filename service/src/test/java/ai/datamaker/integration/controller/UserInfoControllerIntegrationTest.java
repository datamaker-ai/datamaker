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
import ai.datamaker.controller.UserInfoController;
import ai.datamaker.repository.UserRepository;
import org.hamcrest.core.Is;
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

import java.security.Principal;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserInfoController.class)
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(classes = {SpringTestConfiguration.class})
class UserInfoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void currentUser_notFound() throws Exception {
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("me");

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/api/me")).principal(mockPrincipal))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$.detail", Is.is("User not found.")))
        ;
    }

    @Test
    void currentUser() throws Exception {
        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("rob");

        mockMvc.perform(MockMvcRequestBuilders.get(String.format("/api/me")).principal(mockPrincipal))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(MockMvcResultMatchers.jsonPath("$['groups']", hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role", Is.is("ROLE_USER")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.externalId", Is.is("d86f86f0-281d-11ea-978f-2e728ce88124")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.enabled", Is.is(true)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.username", Is.is("rob")))
        ;
    }

}
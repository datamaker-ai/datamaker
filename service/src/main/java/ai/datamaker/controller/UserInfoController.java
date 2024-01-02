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

package ai.datamaker.controller;

import ai.datamaker.model.User;
import ai.datamaker.model.UserGroup;
import ai.datamaker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api")
public class UserInfoController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping(path = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<Object, Object>> currentUser(Principal principal){
        Map<Object, Object> model = new HashMap<>();

        User user = userRepository.findByUsername(principal.getName());
        if (user == null) {
            throw new UsernameNotFoundException("User not found.");
        }

        model.put("username", user.getUsername());
        model.put("enabled", user.getEnabled());
        model.put("externalId", user.getExternalId());
        model.put("role", user.getAuthority());
        model.put("groups", user.getGroups()
                .stream()
                .map(UserGroup::getName)
                .collect(Collectors.toList()));

        return ok(model);
    }
}

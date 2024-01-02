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

import ai.datamaker.exception.ForbiddenException;
import ai.datamaker.model.Authority;
import ai.datamaker.model.Constants;
import ai.datamaker.model.User;
import ai.datamaker.model.UserGroup;
import ai.datamaker.model.forms.UserForm;
import ai.datamaker.model.forms.UserGroupForm;
import ai.datamaker.model.mapper.UserGroupMapper;
import ai.datamaker.model.mapper.UserMapper;
import ai.datamaker.model.response.ApiResponse;
import ai.datamaker.model.response.ResponseSuccess;
import ai.datamaker.model.response.UserGroupResponse;
import ai.datamaker.model.response.UserResponse;
import ai.datamaker.repository.UserGroupRepository;
import ai.datamaker.repository.UserRepository;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGroupRepository groupRepository;

    @Autowired
    private PasswordEncoder encoder;

    private UserGroup everyoneUserGroup;

    @Autowired
    private BuildProperties buildProperties;

    @PostConstruct
    public void init() {
        everyoneUserGroup = groupRepository.findByName("Everyone");
    }

    @GetMapping(path = "/{userId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> get(@PathVariable @NotBlank String userId) {

        User user = userRepository.findByExternalId(UUID.fromString(userId)).orElseThrow();

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(user.getExternalId().toString())
            .objectType(Constants.USER_OBJECT)
            .payload(UserMapper.INSTANCE.userToUserResponse(user))
            .build());
    }

    @GetMapping(path = "/group/{groupId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> group(@PathVariable @NotBlank String groupId) {

        UserGroup userGroup = groupRepository.findByExternalId(UUID.fromString(groupId)).orElseThrow();

        return ResponseEntity.ok(ResponseSuccess
                                     .builder()
                                     .externalId(userGroup.getExternalId().toString())
                                     .objectType(Constants.GROUP_OBJECT)
                                     .payload(UserGroupMapper.INSTANCE.userGroupToUserGroupResponse(userGroup))
                                     .build());
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<ApiResponse> list() {

        List<UserResponse> users = StreamSupport
                .stream(userRepository.findAll().spliterator(), false)
                .map(UserMapper.INSTANCE::userToUserResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .objectType(Constants.USER_OBJECT)
            .payload(users)
            .build());
    }

    @GetMapping(path = "/groups")
    @ResponseBody
    public ResponseEntity<ApiResponse> listGroups() {

        List<UserGroupResponse> groups = StreamSupport
                .stream(groupRepository.findAll().spliterator(), false)
                .map(UserGroupMapper.INSTANCE::userGroupToUserGroupResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .objectType(Constants.GROUP_OBJECT)
            .payload(groups)
            .build());
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<ApiResponse> create(@Valid @RequestBody final UserForm userForm) {
        if ("DEMO".equalsIgnoreCase(buildProperties.get("profile"))) {
            if (userRepository.count() >= 5) {
                throw new IllegalArgumentException("User limit reached for demo version");
            }
        }
        if (!userForm.getPassword().equals(userForm.getConfirmPassword())) {
            throw new IllegalArgumentException("confirm password must be equals to password");
        }
        userForm.setPassword(encoder.encode(userForm.getPassword()));
        List<UserGroup> userGroups = Lists.newArrayList(groupRepository.findAllByExternalIdIn(
          userForm.getGroupIds().stream().map(UUID::fromString).collect(Collectors.toList())
        ));
        if (!userGroups.contains(everyoneUserGroup)) {
            userGroups.add(everyoneUserGroup);
        }
        User user = UserMapper.INSTANCE.userFormToUser(userForm);
        user.getGroups().addAll(userGroups);
        user = userRepository.save(user);

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(user.getExternalId().toString())
            .objectType(Constants.USER_OBJECT)
            .build());
    }

    @PostMapping(path = "/group")
    @ResponseBody
    public ResponseEntity<ApiResponse> createGroup(@Valid @RequestBody final UserGroupForm groupForm) {

        UserGroup userGroup = groupRepository.save(UserGroupMapper.INSTANCE.userGroupFormToUserGroup(groupForm));

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(userGroup.getExternalId().toString())
            .objectType(Constants.GROUP_OBJECT)
            .payload(UserGroupMapper.INSTANCE.userGroupToUserGroupResponse(userGroup))
            .build());
    }

    @DeleteMapping(path = "/{userId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> delete(@PathVariable @NotBlank String userId) {

        User user = userRepository.findByExternalId(UUID.fromString(userId)).orElseThrow();

        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new IllegalArgumentException("cannot delete admin account");
        }

        // TODO test propagation
        // reassign user to admin
        userRepository.delete(user);

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(user.getExternalId().toString())
            .objectType(Constants.USER_OBJECT)
            .build());
    }

    @DeleteMapping(path = "/group/{groupId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> deleteGroup(@PathVariable @NotBlank String groupId) {

        UserGroup group = groupRepository.findByExternalId(UUID.fromString(groupId)).orElseThrow();
        if (everyoneUserGroup.equals(group)) {
            throw new IllegalStateException("Cannot delete everyone group");
        }

        // TODO test propagation
        // reassign group
        groupRepository.delete(group);

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(group.getExternalId().toString())
            .objectType(Constants.GROUP_OBJECT)
            .build());
    }

    @PutMapping(path = "/{userId}/group/{groupId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> addGroupToUser(@PathVariable @NotBlank String userId, @PathVariable @NotBlank String groupId) {

        User user = userRepository.findByExternalId(UUID.fromString(userId)).orElseThrow();
        UserGroup userGroup = groupRepository.findByExternalId(UUID.fromString(groupId)).orElseThrow();

        user.getGroups().add(userGroup);
        User userResponse = userRepository.save(user);

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(userResponse.getExternalId().toString())
            .objectType(Constants.USER_OBJECT)
            .build());
    }


    @PutMapping(path = "/{userId}/status")
    @ResponseBody
    public ResponseEntity<ApiResponse> changeStatus(@PathVariable @NotBlank String userId, @RequestParam boolean enabled) {

        User user = userRepository.findByExternalId(UUID.fromString(userId)).orElseThrow();

        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new IllegalArgumentException("cannot change admin status");
        }

        user.setEnabled(enabled);
        userRepository.save(user);

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(user.getExternalId().toString())
            .objectType(Constants.USER_OBJECT)
            .build());
    }

    @PutMapping(path = "/{userId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> update(@PathVariable @NotBlank String userId, @Valid @RequestBody final UserForm userForm) {

        User userFound = userRepository.findByExternalId(UUID.fromString(userId)).orElseThrow();
        if (userFound.getUserType() == User.UserType.EXTERNAL) {
            throw new ForbiddenException("You cannot modify an external user");
        }
        User updatedUser = UserMapper.INSTANCE.userFormToUser(userForm);
        if (StringUtils.isNotBlank(userForm.getPassword()) && !userForm.getPassword().equals(userForm.getConfirmPassword())) {
            throw new IllegalArgumentException("confirm password must be equals to password");
        }
        if ("admin".equalsIgnoreCase(userFound.getUsername())) {
            if (!userForm.getAuthority().equals(Authority.ROLE_ADMIN.toString())) {
                throw new IllegalArgumentException("cannot change admin role");
            }
            if (!userForm.getUsername().equals("admin")) {
                throw new IllegalArgumentException("cannot change admin username");
            }
        }
        updatedUser.setId(userFound.getId());
        updatedUser.setDateCreated(userFound.getDateCreated());
        updatedUser.setPassword(StringUtils.isBlank(updatedUser.getPassword()) ? userFound.getPassword() : encoder.encode(updatedUser.getPassword()));
        updatedUser.setDateModified(new Date());
        List<UserGroup> userGroups = Lists.newArrayList(groupRepository.findAllByExternalIdIn(
                userForm.getGroupIds().stream().map(UUID::fromString).collect(Collectors.toList())
        ));
        if (!userGroups.contains(everyoneUserGroup)) {
            userGroups.add(everyoneUserGroup);
        }
        updatedUser.getGroups().addAll(userGroups);
        User userResponse = userRepository.save(updatedUser);

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .externalId(userResponse.getExternalId().toString())
                                         .objectType(Constants.USER_OBJECT)
                                         .build());
    }

    @PutMapping(path = "/{userId}/change-password")
    @ResponseBody
    public ResponseEntity<ApiResponse> changePassword(@Valid @NotBlank @PathVariable final String userId,
                                                     @RequestBody final UserForm userForm,
                                                      Principal principal) {

        if (!userForm.getPassword().equals(userForm.getConfirmPassword())) {
            throw new IllegalArgumentException("confirm password must be equals to password");
        }

        User userFound = userRepository.findByExternalId(UUID.fromString(userId)).orElseThrow();
        if (userFound.getUserType() == User.UserType.EXTERNAL) {
            throw new ForbiddenException("You cannot change the password of externally managed users");
        }

        if (!userFound.getUsername().equals(principal.getName())) {
            throw new ForbiddenException("You can only change your password");
        }

        userFound.setPassword(encoder.encode(userForm.getPassword()));
        userFound.setDateModified(new Date());
        User userResponse = userRepository.save(userFound);

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(userResponse.getExternalId().toString())
            .objectType(Constants.USER_OBJECT)
            .build());
    }

    @PutMapping("/group/{groupId}")
    @ResponseBody
    public ResponseEntity<ApiResponse> updateGroup(@Valid @NotBlank @PathVariable final String groupId,
                                                   @Valid @RequestBody final UserGroupForm userGroupForm) {

        UserGroup groupFound = groupRepository.findByExternalId(UUID.fromString(groupId)).orElseThrow();
        if (everyoneUserGroup.equals(groupFound)) {
            throw new IllegalStateException("Cannot update everyone group");
        }
        UserGroup updatedGroup = UserGroupMapper.INSTANCE.userGroupFormToUserGroup(userGroupForm);
        updatedGroup.setId(groupFound.getId());
        UserGroup groupResponse = groupRepository.save(updatedGroup);

        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .externalId(groupResponse.getExternalId().toString())
            .objectType(Constants.GROUP_OBJECT)
            .build());
    }

    @PostMapping(value="/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null){
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.ok(ResponseSuccess
            .builder()
            .build());
    }

    @GetMapping(path = "/principal")
    public Principal retrievePrincipal(Principal principal) {
        return principal;
    }
}

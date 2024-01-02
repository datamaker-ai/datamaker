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
import ai.datamaker.model.CustomUserDetails;
import ai.datamaker.model.User;
import ai.datamaker.model.Workspace;
import ai.datamaker.model.Workspace.WorkspacePermissions;
import ai.datamaker.model.response.ApiResponse;
import ai.datamaker.model.response.ResponseError;
import ai.datamaker.repository.UserRepository;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

/**
 * Base class for API controllers.
 */
public abstract class AbstractRestController {

    @Autowired
    private UserRepository userRepository;

    private static GrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");

    protected ResponseEntity<ApiResponse> createResponse(ApiResponse response) {
        if (response instanceof ResponseError) {
            ResponseError error = (ResponseError) response;
            return ResponseEntity
                .status(error.getStatus())
                .body(error);
        }
        return ResponseEntity.ok(response);
    }

    // FIXME how to manage anonymous user (security disabled)
    protected void authorize(Workspace workspace, boolean modify) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        // TODO improve performance
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ForbiddenException("User does not have privilege to modify workspace");
        }

//        if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
//            if (!authentication.getAuthorities().contains(ADMIN)) {
//                throw new ForbiddenException("User does not have privilege to modify workspace");
//            }
//            return;
//        }
//
//        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (user.getAuthority() == Authority.ROLE_USER && !Objects.equals(workspace.getOwner(), user)) {
            if (CollectionUtils.isNotEmpty(user.getGroups()) &&
                workspace.getUserGroup() != null &&
                    user.getGroups().contains(workspace.getUserGroup())) {

                if (workspace.getGroupPermissions() == WorkspacePermissions.NONE ||
                    (modify && workspace.getGroupPermissions() == WorkspacePermissions.READ_ONLY)) {
                    throw new ForbiddenException("User does not have privilege to modify workspace " + workspace.getName());
                }
            } else {
                throw new ForbiddenException("User does not have privilege to modify workspace " + workspace.getName());
            }
        }
    }

    protected boolean isAuthorized(Workspace workspace, boolean modify) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        // TODO improve performance
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new ForbiddenException("User does not have privilege to modify workspace");
        }

//        if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
//            return authentication.getAuthorities().contains(ADMIN);
//        }

//        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (user.getAuthority() == Authority.ROLE_ADMIN || Objects.equals(workspace.getOwner(), user)) {
            return true;
        }

        return CollectionUtils.isNotEmpty(user.getGroups()) &&
            workspace.getUserGroup() != null &&
                user.getGroups().contains(workspace.getUserGroup()) &&
            (workspace.getGroupPermissions() == WorkspacePermissions.FULL ||
            workspace.getGroupPermissions() == WorkspacePermissions.READ_WRITE ||
            (!modify && (workspace.getGroupPermissions() == WorkspacePermissions.READ_ONLY ||
                    workspace.getGroupPermissions() == WorkspacePermissions.READ_EXECUTE)));
    }

    protected User getUser() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return userDetails.getUser();
    }
}

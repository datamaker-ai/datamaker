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

package ai.datamaker.model.mapper;

import ai.datamaker.model.User;
import ai.datamaker.model.UserGroup;
import ai.datamaker.model.Workspace;
import ai.datamaker.model.forms.WorkspaceForm;
import ai.datamaker.model.response.WorkspaceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtils.class)
public interface WorkspaceMapper {

    WorkspaceMapper INSTANCE = Mappers.getMapper(WorkspaceMapper.class);

    @Mappings({
        @Mapping(source = "owner", target = "owner", qualifiedByName = "ownerToString"),
        @Mapping(source = "userGroup", target = "groupName", qualifiedByName = "groupToString"),
        @Mapping(source="userGroup", target = "group", qualifiedByName = "groupIdToString"),
        @Mapping(target = "externalId", qualifiedBy = MapperUtils.UuidToString.class)
    })
    WorkspaceResponse workspaceToWorkspaceResponse(Workspace workspace);

    @Mappings({
        @Mapping(source = "owner", target = "owner", ignore = true),
        @Mapping(target = "externalId", qualifiedBy = MapperUtils.UuidToString.class)
    })
    Workspace workspaceFormToWorkspace(WorkspaceForm workspaceForm);

    @Named("ownerToString")
    static String ownerToString(User user) {
        return user != null ? user.getUsername() : null;
    }

    @Named("groupIdToString")
    static String groupIdToString(UserGroup group) {
        return group != null ? group.getExternalId().toString() : null;
    }

    @Named("groupToString")
    static String groupToString(UserGroup group) {
        return group != null ? group.getName() : null;
    }

}
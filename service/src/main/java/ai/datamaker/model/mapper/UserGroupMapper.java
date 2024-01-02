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

import ai.datamaker.model.UserGroup;
import ai.datamaker.model.forms.UserGroupForm;
import ai.datamaker.model.response.UserGroupResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtils.class)
public interface UserGroupMapper {
    UserGroupMapper INSTANCE = Mappers.getMapper(UserGroupMapper.class);

    @Mapping(source = "externalId", target = "externalId", qualifiedBy = MapperUtils.UuidToString.class)
    UserGroupResponse userGroupToUserGroupResponse(UserGroup userGroup);

    UserGroup userGroupFormToUserGroup(UserGroupForm form);
}

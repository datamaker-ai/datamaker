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
import ai.datamaker.model.forms.UserForm;
import ai.datamaker.model.response.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(uses = MapperUtils.class)
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mappings({
            @Mapping(source = "locale", target = "languageTag", qualifiedBy = MapperUtils.LocaleToString.class),
            @Mapping(source = "externalId", target = "externalId", qualifiedBy = MapperUtils.UuidToString.class),
            @Mapping(source = "groups", target = "groupIds", qualifiedByName = "groupsToString")
    })
    UserResponse userToUserResponse(User user);

    @Mapping(source = "languageTag", target = "locale", qualifiedBy = MapperUtils.StringToLocale.class)
    User userFormToUser(UserForm form);

    @Named("groupsToString")
    static Set<String> groupToString(Set<UserGroup> groups) {
        return groups != null ? groups
                .stream()
                .map(UserGroup::getExternalId)
                .map(UUID::toString)
                .collect(Collectors.toSet()) : null;
    }
}

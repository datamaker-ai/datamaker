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

package ai.datamaker.repository;

import ai.datamaker.model.User;
import ai.datamaker.model.UserGroup;
import ai.datamaker.model.Workspace;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides CRUD operations on {@link Workspace}.
 */
public interface WorkspaceRepository extends CrudRepository<Workspace, Long> {

  Optional<Workspace> findByExternalId(UUID externalId);

  Iterable<Workspace> findAllByUserGroup(UserGroup userGroup);

  Iterable<Workspace> findAllByOwner(User user);

  Iterable<Workspace> findAllByOrderByName();

  Optional<Workspace> findByName(String name);

  List<Workspace> findByNameAndUserGroupOrNameAndOwnerAndOwnerIsNotNull(String name, UserGroup group, String name2, User owner);
}

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

import ai.datamaker.model.field.Field;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Provides CRUD operations on {@link Field}.
 */
public interface FieldRepository extends PagingAndSortingRepository<Field, Long> {

    Optional<Field> findByExternalId(UUID externalId);

    @Query("SELECT f FROM Field f WHERE f.isAlias = true")
    Iterable<Field> findAllAliases();

    Iterable<Field> findAllByExternalIdIn(Iterable<UUID> externalIds);
}

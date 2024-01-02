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

import ai.datamaker.model.Dataset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

/**
 * Provides CRUD operations on {@link Dataset}.
 */
public interface DatasetRepository extends PagingAndSortingRepository<Dataset, Long> {

    Optional<Dataset> findByExternalId(UUID externalId);

    Iterable<Dataset> findAllByExternalIdIn(Iterable<UUID> externalIds);

    @Query("SELECT d FROM Dataset d WHERE d.workspace = :workspaceId")
    List<Dataset> findAllByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    Optional<Dataset> findByName(String name);

    List<Dataset> findAllByOrderByName();
}

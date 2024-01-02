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

import ai.datamaker.model.Workspace;
import ai.datamaker.sink.SinkConfiguration;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Provides CRUD operations on {@link SinkConfiguration}.
 */
public interface SinkConfigurationRepository extends CrudRepository<SinkConfiguration, Long> {
    Optional<SinkConfiguration> findByExternalId(UUID externalId);
    Optional<SinkConfiguration> findBySinkClassNameAndWorkspace(String sinkClassName, Workspace workspace);
}

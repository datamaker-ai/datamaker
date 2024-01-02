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

import ai.datamaker.model.job.GenerateDataJob;
import ai.datamaker.model.job.JobExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides CRUD operations on {@link JobExecution}.
 */
public interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {

    Optional<JobExecution> findByExternalId(UUID externalId);

    Page<JobExecution> findAllByDataJob(GenerateDataJob dataJob, Pageable pageable);

    List<JobExecution> findAllByDataJobOrderByStartTimeDesc(GenerateDataJob dataJob);

    JobExecution findFirstByDataJobOrderByStartTimeDesc(GenerateDataJob dataJob);

    @Modifying
    @Query("UPDATE JobExecution je SET je.state = 'CANCELLED' WHERE je.state = 'RUNNING'")
    @Transactional
    int cancelRunning();
}

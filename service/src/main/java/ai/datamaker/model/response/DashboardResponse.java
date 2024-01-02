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

package ai.datamaker.model.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class DashboardResponse {
    private Long failedJobExecutions;
    private Long[] failedJobExecutionsPerWeeks;
    private Long[] failedJobExecutionsPerMonths;
    private Long totalJobExecutions;
    private Long[] jobExecutionsPerWeeks;
    private Long[] jobExecutionsPerMonths;
    private Long activeJobExecutions;
    private Long pendingJobExecutions;
    private Long totalConfiguredDataJobs;
    private Long totalRecordsGenerated;
    private Long[] recordsGeneratedPerWeeks;
    private Long[] recordsGeneratedPerMonths;
    private Map<String, Long> totalFieldsPerType;
    private Long totalSinks;
    private Map<String, Long> totalSinksPerType;
    private Long totalGenerators;
    private Map<String, Long> totalGeneratorsPerType;
    private Long totalUsers;
    private Long totalDatasets;
    private Long totalGroups;
    private Long totalFields;
    private Long totalFieldMappings;
    private Long totalWorkspaces;
}

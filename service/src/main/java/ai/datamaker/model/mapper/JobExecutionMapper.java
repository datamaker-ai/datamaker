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

import ai.datamaker.model.job.JobExecution;
import ai.datamaker.model.response.JobExecutionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtils.class)
public interface JobExecutionMapper {

    JobExecutionMapper INSTANCE = Mappers.getMapper(JobExecutionMapper.class);

    @Mappings({
        @Mapping(target = "externalId", qualifiedBy = MapperUtils.UuidToString.class),
        @Mapping(target = "dataJobReplayable", expression = "java(jobExecution.getDataJob().getReplayable())")
    })
    JobExecutionResponse jobExecutionToJobExecutionResponse(JobExecution jobExecution);

}

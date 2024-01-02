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

import ai.datamaker.model.forms.GenerateDataJobForm;
import ai.datamaker.model.job.GenerateDataJob;
import ai.datamaker.model.response.GenerateDataJobResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtils.class)
public interface GenerateDataJobMapper {

    GenerateDataJobMapper INSTANCE = Mappers.getMapper(GenerateDataJobMapper.class);

    @Mappings({
        @Mapping(source = "generatorName", target = "generator"),
        @Mapping(source = "sinkNames", target = "sinks"),
        @Mapping(target = "externalId", qualifiedBy = MapperUtils.UuidToString.class),
//        @Mapping(target = "config", qualifiedBy = MapperUtils.JobConfigToJobConfigResponse.class),
        @Mapping(source = "workspace.externalId", target = "workspaceId", qualifiedBy = MapperUtils.UuidToString.class),
        @Mapping(target = "datasets", expression = "java(dataJob.getDataset().stream().map(d -> d.getExternalId().toString()).collect(java.util.stream.Collectors.toList()))")
    })
    GenerateDataJobResponse generateDataJobToGenerateDataJobResponse(GenerateDataJob dataJob);

    @Mappings({
        @Mapping(target = "config", qualifiedBy = MapperUtils.JobConfigToJobConfig.class)
    })
    GenerateDataJob generateDataJobFormToGenerateDataJob(GenerateDataJobForm generateDataJobForm);

}

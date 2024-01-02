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

import ai.datamaker.model.forms.SinkConfigurationForm;
import ai.datamaker.model.response.SinkConfigurationResponse;
import ai.datamaker.sink.SinkConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtils.class)
public interface SinkConfigurationMapper {
    SinkConfigurationMapper INSTANCE = Mappers.getMapper(SinkConfigurationMapper.class);

    @Mappings({
        @Mapping(source = "externalId", target = "externalId", qualifiedBy = MapperUtils.UuidToString.class),
            @Mapping(source = "jobConfig", target = "config", qualifiedBy = MapperUtils.JobConfigToJobConfig.class),
            @Mapping(source = "workspace.name", target = "workspaceName"),
        @Mapping(target = "workspaceId", expression = "java(sinkConfiguration.getWorkspace().getExternalId().toString())")
    })
    SinkConfigurationResponse sinkConfigurationToSinkConfigurationResponse(SinkConfiguration sinkConfiguration);

    @Mapping(source = "config", target = "jobConfig", qualifiedBy = MapperUtils.JobConfigToJobConfig.class)
    SinkConfiguration sinkConfigurationFormToSinkConfiguration(SinkConfigurationForm form);
}

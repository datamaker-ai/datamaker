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

import ai.datamaker.model.Dataset;
import ai.datamaker.model.forms.DatasetForm;
import ai.datamaker.model.response.DatasetResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtils.class)
public interface DatasetMapper {
    DatasetMapper INSTANCE = Mappers.getMapper(DatasetMapper.class);

    @Mappings({
        @Mapping(source = "locale", target = "languageTag", qualifiedBy = MapperUtils.LocaleToString.class),
        @Mapping(source = "externalId", target = "externalId", qualifiedBy = MapperUtils.UuidToString.class),
        @Mapping(source = "workspace.name", target = "workspaceName"),
        @Mapping(target = "workspaceId", expression = "java(dataset.getWorkspace().getExternalId().toString())")
    })
    DatasetResponse datasetToDatasetResponse(Dataset dataset);

    @Mapping(source = "languageTag", target = "locale", qualifiedBy = MapperUtils.StringToLocale.class)
    Dataset datasetFormToDataset(DatasetForm form);
}

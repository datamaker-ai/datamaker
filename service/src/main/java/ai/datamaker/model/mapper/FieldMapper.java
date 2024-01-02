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

import ai.datamaker.model.field.Field;
import ai.datamaker.model.response.FieldResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper(uses = MapperUtils.class)
public interface FieldMapper {
    FieldMapper INSTANCE = Mappers.getMapper(FieldMapper.class);

    @Mappings({
        @Mapping(source = "locale", target = "languageTag", qualifiedBy = MapperUtils.LocaleToString.class),
        @Mapping(source = "externalId", target = "externalId", qualifiedBy = MapperUtils.UuidToString.class),
        @Mapping(target = "datasetId", expression = "java(field.getDataset().getExternalId().toString())"),
        @Mapping(target = "className", expression = "java(field.getClass().getName())")
//        @Mapping(target = "config", qualifiedBy = MapperUtils.FieldConfigToFieldConfigResponse.class)
    })
    FieldResponse fieldToFieldResponse(Field field);

}

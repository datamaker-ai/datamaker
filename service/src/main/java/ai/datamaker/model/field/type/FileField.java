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

package ai.datamaker.model.field.type;

import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.PropertyConfig.ValueType;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.search.annotations.Indexed;

@Getter @Setter
@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Filename", localizationKey = "field.group.file", group = FieldGroup.CUSTOM)
public class FileField extends Field<String> {

    static final PropertyConfig FILE_DATA_TYPE_PROPERTY =
        new PropertyConfig("field.file.type",
            "File data type",
            ValueType.STRING,
            FileDataType.NAME.toString(),
            Arrays.stream(FileDataType.values()).map(FileDataType::toString).collect(Collectors.toList()));

    static final PropertyConfig DIRECTORY_NAME_PROPERTY =
        new PropertyConfig("field.file.dir.name",
            "Directory name",
            ValueType.STRING,
            null,
            Collections.emptyList());

    static final PropertyConfig FILE_NAME_PROPERTY =
        new PropertyConfig("field.file.file.name",
            "File name",
            ValueType.STRING,
            null,
            Collections.emptyList());

    static final PropertyConfig EXTENSION_PROPERTY =
        new PropertyConfig("field.file.extension",
            "File extension",
            ValueType.STRING,
            null,
            Collections.emptyList());

    static final PropertyConfig SEPARATOR_PROPERTY =
        new PropertyConfig("field.file.path.separator",
            "Path separator",
            ValueType.STRING,
            null,
            Collections.emptyList());

    public enum FileDataType {
        NAME, EXTENSION, MIME_TYPE;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(
            FILE_DATA_TYPE_PROPERTY,
            DIRECTORY_NAME_PROPERTY,
            FILE_NAME_PROPERTY,
            SEPARATOR_PROPERTY,
            EXTENSION_PROPERTY
        );
    }

    @Override
    protected String generateData() {

        FileDataType type = FileDataType.valueOf((String) config.getConfigProperty(FILE_DATA_TYPE_PROPERTY));

        switch(type) {
            case EXTENSION:
                return faker.file().extension();
            case MIME_TYPE:
                return faker.file().mimeType();
            case NAME:
            default:
                String dirOrNull = (String) config.getConfigProperty(DIRECTORY_NAME_PROPERTY);
                String nameOrNull = (String) config.getConfigProperty(FILE_NAME_PROPERTY);
                String extensionOrNull = (String) config.getConfigProperty(EXTENSION_PROPERTY);
                String separatorOrNull = (String) config.getConfigProperty(SEPARATOR_PROPERTY);
                return faker.file().fileName(dirOrNull, nameOrNull, extensionOrNull, separatorOrNull);
        }
    }

    public void setExtensionOrNull(String extension) {
        config.put(EXTENSION_PROPERTY, extension);
    }

    public void setDirOrNull(String directory) {
        config.put(DIRECTORY_NAME_PROPERTY, directory);
    }

    public void setSeparatorOrNull(String separator) {
        config.put(SEPARATOR_PROPERTY, separator);
    }
}

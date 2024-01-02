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
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.persistence.Entity;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Indexed;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "HTML/XML documents", localizationKey = "field.group.sgml.document", group = FieldGroup.CUSTOM)
public class SgmlField extends Field<String> {

    static final PropertyConfig DOCUMENT_TYPE_PROPERTY =
        new PropertyConfig("field.sgml.document.type",
            "SGL Document type",
            PropertyConfig.ValueType.STRING,
            DocumentType.HTML.toString(),
            Arrays.stream(DocumentType.values()).map(DocumentType::toString).collect(Collectors.toList()));

    public enum DocumentType {
        HTML, XML;
    }

    public SgmlField(String name, Locale locale) {
        super(name,
              locale);
    }

    @Override
    protected String generateData() {
        // FIXME implements

        DocumentType documentType = DocumentType.valueOf((String) config.getConfigProperty(DOCUMENT_TYPE_PROPERTY));

        switch (documentType) {
            case HTML:
                // generate scripts / css / metadata
                // faker.internet().url()
                return "<!DOCTYPE html><head></head><body></body></html>";

            case XML:
                return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
            default:
                return "";
        }
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(DOCUMENT_TYPE_PROPERTY);
    }

}

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
import ai.datamaker.model.field.ContainReferences;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.annotations.Indexed;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Represents a complex structure (like a JSON or nested list)
 */
@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Will contains other fields", localizationKey = "field.group.complex.type", group = FieldGroup.CUSTOM)
@Slf4j
public class ComplexField extends Field<Map<String, Object>> implements ContainReferences {

    public static final PropertyConfig ELEMENT_VALUES_PROPERTY =
            new PropertyConfig("field.complex.values",
                               "Element values",
                               PropertyConfig.ValueType.FIELDS,
                               Collections.emptyList(),
                               Collections.emptyList());
    @Transient
    @JsonIgnore
    private List<Field> references = Lists.newArrayList();

    public ComplexField(String name, Locale locale) {
        super(name, locale);
    }

    @Override
    protected Map<String, Object> generateData() {
        Assert.notNull(references, "References should not be null");

        Map<String, Object> values = Maps.newLinkedHashMap();
        getReferences().forEach(r -> values.put(r.getName(), r.getData()));

        return values;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(ELEMENT_VALUES_PROPERTY);
    }

    @Override
    public String getConfigKey() {
        return "field.complex.values";
    }

    @Transient
    @JsonIgnore
    public List<Field> getReferences() {
        for (int i=0; i<references.size(); i++) {
            Field f = references.get(i);
            f.setIsNested(true);
            if (f.getPosition() == 0) {
                f.setPosition(i + 1);
            }
        }
        return references;
    }

    @Transient
    @JsonIgnore
    public void setReferences(List<Field> references) {

        for (int i=0; i<references.size(); i++) {
            Field f = references.get(i);
            f.setIsNested(true);
            if (f.getPosition() == 0) {
                f.setPosition(i + 1);
            }
        }
        this.references = references;
    }

}

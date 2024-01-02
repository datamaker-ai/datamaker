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
import ai.datamaker.model.field.ContainReference;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Indexed;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@NoArgsConstructor
@Entity
@Indexed
@FieldType(description = "List of objects", localizationKey = "field.group.array", group = FieldGroup.PRIMITIVE)
public class ArrayField extends Field<List> implements ContainReference {

    @Transient
    @JsonIgnore
    private transient Field reference;

    static final PropertyConfig FIELD_ELEMENT_PROPERTY =
            new PropertyConfig("field.array.element",
                               "Field element",
                               PropertyConfig.ValueType.FIELD,
                               null,
                               Collections.emptyList());

    static final PropertyConfig NUMBER_ELEMENTS_PROPERTY =
            new PropertyConfig("field.array.number.elements",
                               "Number of elements",
                               PropertyConfig.ValueType.NUMERIC,
                               5,
                               Collections.emptyList());

    public ArrayField(String name, Locale locale) {
        super(name, locale);
    }

    @Override
    protected List<Object> generateData() {

        int numberOfElements = (int) config.getConfigProperty(NUMBER_ELEMENTS_PROPERTY);
        Assert.notNull(reference, "Reference should not be null");

        return IntStream.range(0, numberOfElements)
                .mapToObj((i) -> reference.getData())
                .collect(Collectors.toList());
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(NUMBER_ELEMENTS_PROPERTY, FIELD_ELEMENT_PROPERTY);
    }

    public int getNumberOfElements() {
        return (int) config.getConfigProperty(NUMBER_ELEMENTS_PROPERTY);
    }

    public void setNumberOfElements(int size) {
        config.put(NUMBER_ELEMENTS_PROPERTY.getKey(), size);
    }

    @Override
    public String getConfigKey() {
        return "field.array.element";
    }

    @Override
    public Field getReference() {
        if (reference != null) {
            reference.setIsNested(true);
            if (reference.getPosition() == 0) {
                reference.setPosition(1);
            }
        }
        return reference;
    }

    @Override
    public void setReference(Field reference) {
        reference.setIsNested(true);
        if (reference.getPosition() == 0) {
            reference.setPosition(1);
        }
        this.reference = reference;
    }
}

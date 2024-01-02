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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import javax.persistence.Entity;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Indexed;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "List of pre-defined values", localizationKey = "field.group.choice", group = FieldGroup.CUSTOM)
public class ChoiceField extends Field<Object> {

    static final PropertyConfig CHOICES_PROPERTY =
            new PropertyConfig("field.choice.values",
                               "Choices values",
                               PropertyConfig.ValueType.LIST,
                               Collections.emptyList(),
                               Collections.emptyList());

    static final PropertyConfig VALUE_TYPES_PROPERTY =
        new PropertyConfig("field.choice.value.type",
                    "Value type",
                            ValueType.STRING,
                    "STRING",
                            Lists.newArrayList("NUMERIC", "STRING"));

    public ChoiceField(String name, Locale locale) {
        super(name, locale);
    }

    @Override
    protected Object generateData() {
        Collection<Object> choices = getChoices();

        return choices.toArray()[ThreadLocalRandom.current().nextInt(0, choices.size())];
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(VALUE_TYPES_PROPERTY, CHOICES_PROPERTY);
    }

    public Collection<Object> getChoices() {
        return (Collection<Object>) config.getConfigProperty(CHOICES_PROPERTY);
    }

    public void setChoices(Set<Object> validChoices) {
        config.put(CHOICES_PROPERTY.getKey(), validChoices);
    }
}

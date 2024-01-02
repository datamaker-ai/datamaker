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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

@Entity
@Indexed
@FieldType(description = "Generate a unique primary key (Auto-increment value).", localizationKey = "field.group.sequence", group = FieldGroup.CUSTOM)
public class SequenceField extends Field<Long> {

  static final PropertyConfig DELTA_VALUE_PROPERTY =
      new PropertyConfig("field.long.sequence.delta",
          "Minimum value",
          ValueType.NUMERIC,
          1L,
          Collections.emptyList());

  static final PropertyConfig INITIAL_VALUE_PROPERTY =
      new PropertyConfig("field.long.sequence.initial.value",
          "Initial value",
          ValueType.NUMERIC,
          0L,
          Collections.emptyList());

  @Transient
  @JsonIgnore
  private AtomicLong index = new AtomicLong();

  public SequenceField() {
    super(null, Locale.getDefault());
    isPrimaryKey = true;
  }

  public SequenceField(String name, Locale locale) {
    super(name, locale);
    isPrimaryKey = true;
  }

  // FIXME rework
  public void setInitialValue(Long initialValue) {
    index = new AtomicLong(initialValue);
  }

  @Override
  protected Long generateData() {
    long delta = (long) config.getConfigProperty(DELTA_VALUE_PROPERTY);
    return index.addAndGet(delta);
  }

  @Override
  public List<PropertyConfig> getConfigProperties() {
    return Lists.newArrayList(
        DELTA_VALUE_PROPERTY,
        INITIAL_VALUE_PROPERTY
    );
  }

  public void setDelta(long delta) {
    config.put(DELTA_VALUE_PROPERTY, delta);
  }
}

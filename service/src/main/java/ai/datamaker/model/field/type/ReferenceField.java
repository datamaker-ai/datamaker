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
import ai.datamaker.model.field.ContainReference;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.http.util.Asserts;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "A reference to another field in the current dataset", localizationKey = "field.group.reference", group = FieldGroup.CUSTOM)
public class ReferenceField extends Field<Object> implements ContainReference {

  @Transient
  @JsonIgnore
  @Setter
  @Getter
  private transient Field reference;

  public static final PropertyConfig REFERENCE_VALUE_PROPERTY =
      new PropertyConfig("field.reference.value",
          "Reference value",
          ValueType.REFERENCE,
          null,
          Collections.emptyList());

  @Transient
  @JsonIgnore
  private final transient AtomicInteger currentCount = new AtomicInteger(0);

  public ReferenceField(String name, Locale locale) {
    super(name, locale);
  }

  @Override
  protected Object generateData() {

    Asserts.notNull(reference, "Reference must not be null");

    if (!reference.getIsPrimaryKey()) {
      throw new IllegalStateException(
          String.format("Reference id=%s, name=%s, dataset=%s is not a primary key. Only primary key can be referenced?",
              reference.getExternalId().toString(),
              reference.getName(),
              reference.getDataset().getName()));

    }

    if (!reference.getDataset().getPrimaryKeyValues().containsKey(reference)) {
      throw new IllegalStateException(
          String.format("Reference id=%s, name=%s not found. Did you forget to add the dataset %s in the generation job?",
          reference.getExternalId().toString(),
          reference.getName(),
          reference.getDataset().getName()));
    }

    if ((currentCount.get() + 1) > reference.getDataset().getPrimaryKeyValues().size()) {
      throw new IllegalStateException("Current count is greater that reference");
    }

    return Iterators.get(reference.getDataset().getPrimaryKeyValues().get(reference).iterator(),
                         currentCount.getAndIncrement());
  }

  @Override
  public List<PropertyConfig> getConfigProperties() {
    return Lists.newArrayList(REFERENCE_VALUE_PROPERTY);
  }


  @Override
  public String getConfigKey() {
    return "field.reference.value";
  }
}

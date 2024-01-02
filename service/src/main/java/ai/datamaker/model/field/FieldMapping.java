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

package ai.datamaker.model.field;

import ai.datamaker.model.Searchable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Entity
//@EntityListeners(FieldMappingListener.class)
@Setter
@Getter
@Indexed
@Table(indexes = @Index(columnList = "mappingKey"))
public class FieldMapping implements Searchable {

  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  @org.hibernate.search.annotations.Field
  private UUID externalId;

  @Column(unique = true)
  @org.hibernate.search.annotations.Field
  private String mappingKey;

  @Column(length = 10000)
  @org.hibernate.search.annotations.Field
  private String fieldJson;

  public FieldMapping() {
    this.externalId = UUID.randomUUID();
  }

  public FieldMapping(String mappingKey, String fieldJson) {
    this.mappingKey = mappingKey;
    this.fieldJson = fieldJson;
    this.externalId = UUID.randomUUID();
  }

  @Override
  public String getName() {
    return mappingKey.split("-")[0];
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public Collection<String> getTags() {
    return Collections.emptyList();
  }
}

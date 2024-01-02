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

package ai.datamaker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Indexed
public class UserGroup implements Searchable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @org.hibernate.search.annotations.Field
    @Column(nullable = false, unique = true)
    private UUID externalId;

    @NotBlank
    @org.hibernate.search.annotations.Field
    @Column(nullable = false, unique = true)
    private String name;

    @org.hibernate.search.annotations.Field
    private String description;

    @EqualsAndHashCode.Exclude
    private Date dateCreated = new Date();

    @EqualsAndHashCode.Exclude
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date dateModified;

    public UserGroup() {
        externalId = UUID.randomUUID();
    }

    @Override
    public Collection<String> getTags() {
        return null;
    }
}

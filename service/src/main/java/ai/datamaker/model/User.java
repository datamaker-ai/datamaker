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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
@Indexed
public class User implements Searchable, Serializable {

    private static final long serialVersionUID = -6145284622968284564L;

    @Id
    @JsonIgnore
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @org.hibernate.search.annotations.Field
    @Column(nullable = false, unique = true)
    private UUID externalId;

    @NotBlank
    @org.hibernate.search.annotations.Field
    @Column(nullable = false, unique = true)
    private String username;

    @NotNull
    @org.hibernate.search.annotations.Field
    private String firstName;

    @NotNull
    @org.hibernate.search.annotations.Field
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Authority authority = Authority.ROLE_USER;

    private Boolean enabled = true;

    @Exclude
    private Date dateCreated = new Date();

    @Exclude
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date dateModified;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Enumerated(EnumType.STRING)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UserType userType = UserType.INTERNAL;

    @Exclude
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<UserGroup> groups = Sets.newHashSet();

    private Locale locale = Locale.getDefault();

    public User() {
        externalId = UUID.randomUUID();
    }

    @Override
    public String getName() {
        return firstName + " " + lastName;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Collection<String> getTags() {
        return null;
    }

    public enum UserType {
        INTERNAL, EXTERNAL, DEFAULT
    }
}

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

import ai.datamaker.model.job.GenerateDataJob;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * A workspace is a logical entity containing {@link Dataset}.
 */
@Data
@Entity
@Indexed
public class Workspace implements Searchable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @org.hibernate.search.annotations.Field
    @Column(nullable = false, unique = true)
    private UUID externalId;

    @OneToMany(
        mappedBy = "workspace",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JsonManagedReference
    @ToString.Exclude
    private List<Dataset> datasets;

    @OneToMany(
        mappedBy = "workspace",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @JsonManagedReference
    @ToString.Exclude
    private List<GenerateDataJob> dataJobs;

    //@NotNull
    @ManyToOne
    private User owner;

    //@Column(name = "group_")
    @ManyToOne
    private UserGroup userGroup;

    @Enumerated(EnumType.STRING)
    private WorkspacePermissions groupPermissions = WorkspacePermissions.NONE;

    @NotBlank
    @Column(nullable = false)
    @org.hibernate.search.annotations.Field
    private String name;

    @org.hibernate.search.annotations.Field
    private String description;

    private Date dateCreated = new Date();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date dateModified;

    public Workspace() {
        externalId = UUID.randomUUID();
    }

    public enum WorkspacePermissions {
        NONE, READ_ONLY, READ_EXECUTE, READ_WRITE, FULL
    }

    public void addDataset(Dataset dataset) {
        datasets.add(dataset);
        dataset.setWorkspace(this);
    }

    public void removeDataset(Dataset dataset) {
        datasets.remove(dataset);
        dataset.setWorkspace(null);
    }

    public void addGenerateDataJob(GenerateDataJob job) {
        dataJobs.add(job);
        job.setWorkspace(this);
    }

    public void removeGenerateDataJob(GenerateDataJob job) {
        dataJobs.remove(job);
        job.setWorkspace(null);
    }

    public Collection<String> getTags() {
        return Collections.emptyList();
    }
}

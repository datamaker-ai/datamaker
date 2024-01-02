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

package ai.datamaker.model.job;

import ai.datamaker.model.Searchable;
import ai.datamaker.utils.bridge.PrimitiveCollectionBridge;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Lists;
import io.micrometer.core.instrument.Meter;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static ai.datamaker.model.job.JobExecution.JobExecutionState.INIT;

@Data
@Entity
@Indexed
public class JobExecution implements Searchable {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    // FIXME should we cascade?
    @JsonBackReference
    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private GenerateDataJob dataJob;

    @org.hibernate.search.annotations.Field
    @Column(nullable = false, unique = true)
    private UUID externalId;

    private Boolean isSuccess = false;

    @Enumerated(EnumType.STRING)
    private JobExecutionState state = INIT;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime = new Date();

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cancelTime;

    private Long numberOfRecords;

    private Boolean replay = false;

    @Column(columnDefinition="CLOB")
    @ElementCollection(targetClass=String.class)
    @FieldBridge(impl = PrimitiveCollectionBridge.class)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "job_execution_id")
    //@Cascade(value={CascadeType.ALL})
    @org.hibernate.search.annotations.Field
    private List<String> errors = Lists.newArrayList();

    @ElementCollection(targetClass=String.class)
    @FieldBridge(impl = PrimitiveCollectionBridge.class)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "job_execution_id")
    //@Cascade(value={CascadeType.ALL})
    @org.hibernate.search.annotations.Field
    private List<String> results = Lists.newArrayList();

    @Transient
    private Map<String, Meter> metrics;

    public enum JobExecutionState {
        COMPLETED, RUNNING, CANCELLED, INIT, FAILED
    }

    public JobExecution() {
        this.externalId = UUID.randomUUID();
    }

    @Override
    public String getName() {
        return "job execution";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Collection<String> getTags() {
        return null;
    }

}

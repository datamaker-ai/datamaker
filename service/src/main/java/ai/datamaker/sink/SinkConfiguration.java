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

package ai.datamaker.sink;

import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.Searchable;
import ai.datamaker.model.Workspace;
import ai.datamaker.service.BeanService;
import ai.datamaker.service.EncryptionService;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Indexed
public class SinkConfiguration implements Searchable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @org.hibernate.search.annotations.Field
    @Column(nullable = false, unique = true)
    private UUID externalId;

    @org.hibernate.search.annotations.Field
    @Column(nullable = false, unique = true)
    private String name;

    @org.hibernate.search.annotations.Field
    private String sinkClassName;

    private Date dateCreated = new Date();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date dateModified;

    @Lob
    private JobConfig jobConfig;

    @OneToOne
    private Workspace workspace;

    public SinkConfiguration() {
        this.externalId = UUID.randomUUID();
    }

    @PrePersist
    @PreUpdate
    public void saveConfig() throws Exception {
        EncryptionService encryptionService = BeanService.getBean(EncryptionService.class);

        if (StringUtils.isNotBlank(sinkClassName)) {
            DataOutputSink sink = (DataOutputSink) Class.forName(sinkClassName).getDeclaredConstructor().newInstance();
            sink.getConfigProperties().forEach(cp -> {
                if (cp.getType() == PropertyConfig.ValueType.PASSWORD || cp.getType() == PropertyConfig.ValueType.SECRET) {
                    String originalValue = jobConfig.getProperty(cp.getKey());

                    if (jobConfig.containsKey(cp.getKey()) && !originalValue.startsWith("enc-")) {
                        String password = encryptionService.encrypt(originalValue);
                        jobConfig.put(cp.getKey(), "enc-" + password);
                    }
                }
            });
        }
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

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

import ai.datamaker.generator.DataGenerator;
import ai.datamaker.model.Constants;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.Searchable;
import ai.datamaker.model.Workspace;
import ai.datamaker.service.BeanService;
import ai.datamaker.service.EncryptionService;
import ai.datamaker.sink.DataOutputSink;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.micrometer.core.instrument.Meter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
@Entity
@Data
@Indexed
@Slf4j
public class GenerateDataJob implements Searchable {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @org.hibernate.search.annotations.Field
    @Column(nullable = false, unique = true)
    private UUID externalId;

    // TODO implement burst, delay, frequency, windowing
    private Boolean streamForever = false;
    // Quartz timer
    private String schedule = Constants.SCHEDULE_ONCE;
    // random schedule
    private Boolean runStatus; // should include errors, startDate, endDate, user

    @ManyToMany
    private List<Dataset> dataset = Lists.newArrayList();

    @ManyToOne
    @JsonBackReference
    private Workspace workspace;

    private Long numberOfRecords = 10L;

    private Long size;

    private Boolean useBuffer = false;

    private Integer bufferSize = 8192;

    private Integer threadPoolSize = 10;

    private Boolean randomizeNumberOfRecords = false;

    private Boolean flushOnEveryRecord = true;

    private String generatorName;

    private Boolean replayable = false;

    private Integer replayHistorySize = 10;

    @Transient
    private transient DataGenerator generator;

    // Verify if format can be exported to sink
    // TODO if multiple sink Use TeeOutputStream
    // TODO some sinks support other options (ex: kafka burst)
    @org.hibernate.search.annotations.Field
    private String name;

    @org.hibernate.search.annotations.Field
    private String description;

    @ElementCollection
    private Set<String> sinkNames = Sets.newHashSet();

    @Transient
    private transient Set<DataOutputSink> sinks = Sets.newHashSet();

    @Lob
    private JobConfig config = new JobConfig();

    @Transient
    private List<Meter> meters;

    private Date dateCreated = new Date();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date dateModified;

    public GenerateDataJob() {
        this.externalId = UUID.randomUUID();
    }

    public GenerateDataJob(String name, Locale locale) {
        this.externalId = UUID.randomUUID();
        this.name = name;
    }

    public Collection<String> getTags() {
        return Collections.emptyList();
    }

//    @PrePersist
//    @PreUpdate
//    public void sortDatasets() {
//        log.debug("Sorting dataset for job: name={}, id={}", name, externalId);
//
//        DatasetService datasetService = BeanService.getBean(DatasetService.class);
//
//        List<Dataset> copyDataset = Lists.newArrayList(dataset);
//        dataset.clear();
//        dataset.addAll(datasetService.sortDatasetsPerDependencies(copyDataset));
//    }

    @PrePersist
    @PreUpdate
    public void saveConfig() {
        EncryptionService encryptionService = BeanService.getBean(EncryptionService.class);

        sinks.forEach(sink -> {
            sink.getConfigProperties().forEach(cp -> {
                if (config.containsKey(sink.getClass().getCanonicalName())) {
                    JobConfig sinkConfig = (JobConfig) config.get(sink.getClass().getCanonicalName());
                    if (cp.getType() == PropertyConfig.ValueType.PASSWORD || cp.getType() == PropertyConfig.ValueType.SECRET) {
                        String originalValue = sinkConfig.getProperty(cp.getKey());

                        if (sinkConfig.containsKey(cp.getKey()) && !originalValue.startsWith("enc-")) {
                            String password = encryptionService.encrypt(originalValue);
                            sinkConfig.put(cp.getKey(), "enc-" + password);
                        }
                    }
                }
            });
        });
    }

    @PostLoad
    public void build() throws Exception {
//        EncryptionService encryptionService = BeanService.getBean(EncryptionService.class);

        if (StringUtils.isNotBlank(generatorName)) {
            generator = (DataGenerator) Class.forName(generatorName).getDeclaredConstructor().newInstance();
        }
        sinkNames.forEach(sink -> {
            try {
                DataOutputSink dataOutputSink = (DataOutputSink) Class.forName(sink).getDeclaredConstructor().newInstance();
//                dataOutputSink.getConfigProperties().forEach(cp -> {
//                    if (config.containsKey(sink)) {
//                        JobConfig sinkConfig = (JobConfig) config.get(sink);
//                        if (cp.getType() == PropertyConfig.ValueType.PASSWORD || cp.getType() == PropertyConfig.ValueType.SECRET) {
//                            String originalValue = sinkConfig.getProperty(cp.getKey());
//                            if (sinkConfig.containsKey(cp.getKey()) && originalValue.endsWith("=")) {
//                                String password = encryptionService.decrypt(originalValue);
//                                sinkConfig.put(cp.getKey(), password);
//                            }
//                        }
//                    }
//                });
                sinks.add(dataOutputSink);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

}

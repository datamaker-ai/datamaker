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

import ai.datamaker.exception.CancelJobException;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldValue;
import ai.datamaker.utils.bridge.PrimitiveCollectionBridge;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.ToString;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Represents a data structure to generate.
 */
@Data
@Entity
@Indexed
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Dataset implements Searchable, Serializable {

    private static final long serialVersionUID = -1L;

    @Id
    @JsonIgnore
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @org.hibernate.search.annotations.Field
    @Column(nullable = false, unique = true)
    private UUID externalId;

    @NotBlank
    //@Column(nullable = false, unique = true)
    @org.hibernate.search.annotations.Field
    private String name;

    @org.hibernate.search.annotations.Field
    private String description;

    @ElementCollection
    @org.hibernate.search.annotations.Field
//    @IndexedEmbedded
    @FieldBridge(impl = PrimitiveCollectionBridge.class)
    private Set<String> tags;

    // TODO metadata

    // Dataset can be copied between workspace, only owner can modify the dataset (for the moment)
    @ManyToOne(
        cascade = {},
        fetch = FetchType.LAZY
    )
    @ToString.Exclude
    private Workspace workspace;

    // TODO add bidirectional relation
    // https://vladmihalcea.com/the-best-way-to-map-a-onetomany-association-with-jpa-and-hibernate/

    @OneToMany(
            mappedBy = "dataset",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("position ASC")
    @JsonManagedReference
    @ToString.Exclude
    private List<Field> fields = Lists.newLinkedList();

    // TODO maybe move to export service instead
    private Boolean exportHeader = true;

    // Locale applied to fields
    private Locale locale;

    private Long numberOfRecords = 1L;

    // Use to generate random number of records
    @Transient
    private Boolean randomizeNumberRecords = false;

    private Float nullablePercentLimit = 0.0f;

    @Transient
    private Boolean flushOnEveryRecord = true;

    @Transient
    @JsonIgnore
    private Multimap<Field, Object> primaryKeyValues = ArrayListMultimap.create();

    @Transient
    @JsonIgnore
    private Set<Integer> allHashes = Sets.newHashSet();

    private Boolean allowDuplicates = true;

    @Transient
    private int duplicatesCount;

    private Integer numberOfRetries = 10;

    private Date dateCreated = new Date();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date dateModified;

    private Integer threadPoolSize = 10;

    private Float duplicatesPercentLimit = 0.0f;

    public Dataset() {
        this.externalId = UUID.randomUUID();
        this.locale = Locale.getDefault();
    }

    public Dataset(String name, Locale locale) {
        this.externalId = UUID.randomUUID();
        this.name = name;
        this.locale = locale;
    }

    public void addField(Field field) {
        fields.add(field);
        recalculatePositions();
        field.setDataset(this);
    }

    public void removeField(Field field) {
        fields.remove(field);
        recalculatePositions();
        field.setDataset(null);
    }

    private void recalculatePositions() {
        IntStream
            .range(0, fields.size())
            .forEach(pos -> fields.get(pos).setPosition(pos + 1));
    }

    /**
     * Generate all the data associated to a dataset.
     * @param callback callback on the current generated values iteration
     */
    // TODO extract to abstract or service
    public void processAllValues(Consumer<List<FieldValue>> callback) {
        final AtomicLong currentIndex = new AtomicLong(0);
        final AtomicInteger retries = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        // TODO emit metrics completion: (currentIndex / numberRecord) * 100
        while (currentIndex.get() < numberOfRecords) {
            if (Thread.currentThread().isInterrupted()) {
                throw new CancelJobException("Job was cancelled");
            }

            List<Future<List<FieldValue>>> futures = Lists.newArrayList();
            LongStream.range(0, Math.min(threadPoolSize, numberOfRecords - currentIndex.get())).forEach((i) -> futures.add(executor.submit(this::getAllValues)));
            // List<FieldValue> fieldValues = getAllValues();

            // TODO sort per index?
            // TODO queue List<List<FieldValue>>, every x, apply callback
            futures.forEach(f -> {
                List<FieldValue> fieldValues;
                try {
                    fieldValues = f.get();
                } catch (Exception e) {
                    if(e instanceof InterruptedException) {
                        // just in case this Runnable is actually called directly,
                        // rather than in a new thread, don't want to swallow the
                        // flag:
                        Thread.currentThread().interrupt();
                        throw new CancelJobException("Job was cancelled", e);
                    }
                    throw new IllegalStateException(e);
                }

                int combinedHash = Objects.hash(fieldValues.stream().map(FieldValue::getValue).toArray());
                if (allHashes.contains(combinedHash)) {
                    if (allowDuplicates && (++duplicatesCount / (float) numberOfRecords) <= duplicatesPercentLimit) {
                        //duplicatesCount++;
                        processValue(callback, fieldValues, combinedHash);
                    } else {

                        if (retries.get() >= numberOfRetries) {
                            throw new IllegalStateException("Cannot generate unique values after " + numberOfRetries + " retries...");
                        }

                        retries.incrementAndGet();
                        return;
                    }

                } else {
                    processValue(callback, fieldValues, combinedHash);
                }

                retries.set(0);
                currentIndex.incrementAndGet();
            });

        }

        executor.shutdown();
    }

    private void processValue(Consumer<List<FieldValue>> callback, List<FieldValue> fieldValues, int combinedHash) {
        if (!allowDuplicates || duplicatesPercentLimit > 0.0f) {
            allHashes.add(combinedHash);
        }
        fieldValues.forEach(fv -> {
            if (fv.getField().getIsPrimaryKey()) {
                primaryKeyValues.put(fv.getField(),
                                     fv.getValue());
            }
        });
        callback.accept(fieldValues);
    }

    // TODO multi thread (parallel or not...)
    private List<FieldValue> getAllValues() {
        return fields//.parallelStream()
                .stream()
                .filter(f -> !f.getIsNested())
                .map(f -> {
                    Object value = f.getData();
                    return FieldValue.of(f, value);
                }).collect(Collectors.toList());
    }
}

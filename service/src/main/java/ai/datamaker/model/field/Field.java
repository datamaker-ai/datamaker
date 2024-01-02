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

import ai.datamaker.model.field.formatter.FieldFormatter;
import ai.datamaker.model.field.type.NullField;
import ai.datamaker.model.Configurable;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.Searchable;
import ai.datamaker.repository.FieldRepository;
import ai.datamaker.service.BeanService;
import ai.datamaker.service.EncryptionService;
import ai.datamaker.service.FieldService;
import ai.datamaker.utils.FakerUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javafaker.Faker;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Getter
@Setter
@ToString
@Entity
@Slf4j
public abstract class Field<V> implements Configurable, Serializable, Searchable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @org.hibernate.search.annotations.Field
    @Column(nullable = false, unique = true)
    private UUID externalId;

    // Leave blank to generate random value
    @Lob
    protected FieldConfig config = new FieldConfig();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Dataset dataset;

    // Defaults to workspace locale
    // The locale is used to format the data
    // Example: currency or even first name
    // If user select locale=FR and first name, the service will generate French's first name (Jean-Claude instead of Kevin)
    @NotNull
    private Locale locale = Locale.getDefault();

    // Column name, header
    @NotBlank
    @org.hibernate.search.annotations.Field
    private String name;

    @org.hibernate.search.annotations.Field
    private String description;

    private Boolean isNullable = false;

    private String nullValue = "";

    @Transient
    protected FieldFormatter<V> formatter;

    @Access(AccessType.PROPERTY)
    @org.hibernate.search.annotations.Field
    private String formatterClassName;

    private Integer position = 0;

    // Use for XML formatting or else...
    private Boolean isAttribute = false;

    private Boolean isAlias = false;

    protected Boolean isPrimaryKey = false;

    private Boolean isNested = false;

    private Date dateCreated = new Date();

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date dateModified;

    @Transient
    private Class<V> objectType;

    @Transient
    @org.hibernate.search.annotations.Field
    private String className = getClass().getSimpleName();

    @Transient
    protected transient Faker faker;

    @SuppressWarnings("unchecked")
    public Field() {
        this.externalId = UUID.randomUUID();
        this.faker = FakerUtils.getFakerForLocale(locale);
        if (getClass().getGenericSuperclass() instanceof ParameterizedType) {
            Type pt = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            if (pt instanceof ParameterizedType) {
                this.objectType = (Class<V>) ((ParameterizedType) pt).getRawType();
            } else {
                this.objectType = (Class<V>) pt;
            }
        } else {
            this.objectType = (Class<V>) Class.class;
        }
        this.config.setField(this);
    }

    @SuppressWarnings("unchecked")
    public Field(String name, Locale locale) {
        this.name = name;
        this.locale = locale;
        this.externalId = UUID.randomUUID();
        this.faker = FakerUtils.getFakerForLocale(locale);
        if (getClass().getGenericSuperclass() instanceof ParameterizedType) {
            Type pt = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
            if (pt instanceof ParameterizedType) {
                this.objectType = (Class<V>) ((ParameterizedType) pt).getRawType();
            } else {
                this.objectType = (Class<V>) pt;
            }
        } else {
            this.objectType = (Class<V>) Class.class;
        }
        this.config.setField(this);
    }

    protected abstract V generateData();

    public Object getData() {

        if (isNullable && dataset.getNullablePercentLimit() > 0.0f) {
            // Generate random null 10% of time (max)
            if (ThreadLocalRandom.current().nextFloat() <= dataset.getNullablePercentLimit()) {
                return null;
            }
        }

        V value = generateData();

        if (formatter != null) {
            return formatter.format(value, config);
        }

        return value;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        this.faker = FakerUtils.getFakerForLocale(locale);
    }

    public String getFormatterClassName() {
        return formatterClassName;
    }

    public void setFormatterClassName(String formatterClass) {
        if (StringUtils.isNotBlank(formatterClass)) {
            try {
                this.formatterClassName = formatterClass;
                this.formatter = (FieldFormatter) Class.forName(formatterClass).getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }


    @VisibleForTesting
    protected Field getFieldFor(String externalId) {
        FieldService fieldService = BeanService.getBean(FieldService.class);

        return fieldService.getFieldFor(externalId);
    }

    public Collection<String> getTags() {
        return Collections.emptyList();
    }

    protected NullField createNull() {
        NullField nullField = new NullField("null", locale);
        nullField.setDataset(dataset);

        return nullField;
    }

    @PostLoad
    public void injectDependencies() {

//        EncryptionService encryptionService = BeanService.getBean(EncryptionService.class);
//        getConfigProperties().forEach(cp -> {
//            if (cp.getType() == PropertyConfig.ValueType.PASSWORD || cp.getType() == PropertyConfig.ValueType.SECRET) {
//                String originalValue = config.getProperty(cp.getKey());
//                if (config.containsKey(cp.getKey()) && originalValue.endsWith("=")) {
//                    String password = encryptionService.decrypt(originalValue);
//                    config.put(cp.getKey(), password);
//                }
//            }
//        });

        if (this instanceof ContainReference) {
            log.debug("Injecting dependencies for field: name={}, id={}", name, externalId);

            ContainReference containReference = (ContainReference)this;
            FieldRepository fieldRepository = BeanService.getBean(FieldRepository.class);

            String uuidReference = config.getProperty(containReference.getConfigKey());
            if (StringUtils.isNotBlank(uuidReference)) {
                fieldRepository.findByExternalId(UUID.fromString(uuidReference)).ifPresent(((ContainReference) this)::setReference);
            }
//            else {
//                containReference.setReference(NullField.DEFAULT_NULL_FIELD);
//            }

        } else if (this instanceof ContainReferences) {
            log.debug("Injecting dependencies for field: name={}, id={}", name, externalId);

            ContainReferences containReferences = (ContainReferences)this;
            FieldRepository fieldRepository = BeanService.getBean(FieldRepository.class);

            Collection<String> uuidReferences = (Collection<String>) config.get(containReferences.getConfigKey());
            if (CollectionUtils.isNotEmpty(uuidReferences)) {
                //containReferences.getReferences().clear();
                containReferences.setReferences(
                    Lists.newArrayList(fieldRepository.findAllByExternalIdIn(uuidReferences
                        .stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toList()))
                    )
                );
            }
        }
    }

    @PrePersist
    @PreUpdate
    public void saveConfig() {
        EncryptionService encryptionService = BeanService.getBean(EncryptionService.class);
        getConfigProperties().forEach(cp -> {
            if (cp.getType() == PropertyConfig.ValueType.PASSWORD || cp.getType() == PropertyConfig.ValueType.SECRET) {
                String originalValue = config.getProperty(cp.getKey());

                if (config.containsKey(cp.getKey()) && !originalValue.startsWith("enc-")) {
                    String password = encryptionService.encrypt(originalValue);
                    config.put(cp.getKey(), "enc-" + password);
                }
            }
        });

        if (this instanceof ContainReference) {
            log.debug("Save config for field: name={}, id={}", name, externalId);

            ContainReference containReference = (ContainReference)this;

            if (containReference.getReference() != null &&
                !NullField.DEFAULT_NULL_FIELD.getExternalId().equals(containReference.getReference().getExternalId())) {
                config.put(containReference.getConfigKey(),
                           containReference.getReference().getExternalId().toString());
            }

        } else if (this instanceof ContainReferences) {
            log.debug("Save config for field: name={}, id={}", name, externalId);

            ContainReferences containReferences = (ContainReferences)this;

            if (CollectionUtils.isNotEmpty(containReferences.getReferences()) &&
                !config.containsKey(containReferences.getConfigKey())) {

                config.put(containReferences.getConfigKey(),
                           Lists.newArrayList(containReferences
                               .getReferences()
                               .stream()
                               .map(f -> f.getExternalId().toString())
                               .collect(Collectors.toList()))
                );
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Field<?> field = (Field<?>) o;
        return id != null && Objects.equals(id,
                                            field.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

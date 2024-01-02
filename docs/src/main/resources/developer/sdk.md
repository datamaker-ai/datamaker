---
layout: default
title: SDK
parent: Developer
---

# SDK
{: .no_toc }

## Table of contents
{: .no_toc .text-delta }

1. TOC
{:toc}

## How to extend functionalities

- Create basic Maven project
  
`mvn archetype:generate -DgroupId=ai.datamaker -DartifactId=demo-sdk -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false`
- Make sure the library (jar) is installed in your local or remote repository (Nexus, Artefactory)
- Add Datamaker dependency to pom.xml

```xml
<dependency>
    <groupId>ai.datamaker</groupId>
    <artifactId>service</artifactId>
    <version>1.0.6</version>
    <scope>provided</scope>
</dependency>
```

- Build: `mvn clean install`
- Make sure there are no compile errors
- From there you can create custom components to enrich your Datamaker instance 
- When you are done developing, you can copy the target jar on the server running datamaker in the following path
    - loader path:
```yaml
# APPLICATION FILES
application.config.path=/tmp/datamaker/conf
loader.path=${application.config.path}/jar
```    
- Make sure your copy all dependencies required by your library
- Restart the service

## Processor

The processors are mainly used to automatically create datasets from well-known file formats.
For example, if we have a CSV file that we want to process, we can use the CsvProcessor. 
This processor will use the headers and values to create the corresponding dataset.
A processor takes an InputStream and convert it to a dataset using the provided configuration.

Minimally you need to implement these two methods:

`public abstract Optional<Dataset> process(InputStream input, JobConfig config);`

`public abstract Set<SupportedMediaType> supportedTypes();`

Let say we have a custom format that we want to process.
This file used a basic structure such as TLV (Tag-Length-Value) triplets.
```
1 byte  = record type
4 bytes = record length
followed by record content
```

### Code sample
```java
    @Override
public Optional<Dataset> process(InputStream input, JobConfig config) {
        Locale locale = getLocale(config);
        String datasetName = (String)config.getConfigProperty(INPUT_FILENAME_PROPERTY);

        Dataset dataset = new Dataset(datasetName,locale);

        try {
            int position = 0;
            while (true) {
                int type = input.read();
                position += 1;
                byte[] lengthBuffer = new byte[4];
                int r = input.read(lengthBuffer, 0, 4);
                position += 3;
                int length = ByteBuffer.wrap(lengthBuffer).getInt();
                byte[] content = new byte[length];
                int result = input.read(content, 0, length);
                position += length;
                if (type == 1) {
                    IntegerField integerField = new IntegerField(new String(content), locale);
                    dataset.addField(integerField);
                } else if (type == 2) {
                    StringField stringField = new StringField(new String(content), locale);
                    dataset.addField(stringField);
                }
                if (result == -1) {
                    break;
                }
            }

        } catch (IOException e) {
            throw new DatasetSerializationException("invalid data", e, dataset);
        }

    return Optional.of(dataset);
}

```

### Base class
```java
package ca.breakpoints.datamaker.processor;

import ca.breakpoints.datamaker.model.*;
import ca.breakpoints.datamaker.model.field.Field;
import ca.breakpoints.datamaker.service.FieldDetectorService;
import java.io.InputStream;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Process input source to generate dataset automatically.
 * Determine data type based on values.
 * Apply default rules.
 */
public abstract class DatasetProcessor implements Configurable {

    static final PropertyConfig LOCALE_PROPERTY =
            new PropertyConfig(Constants.LOCALE,
                               "Locale",
                               PropertyConfig.ValueType.STRING,
                               Locale.ENGLISH.toLanguageTag(),
                               Arrays.asList(Locale.ENGLISH.toLanguageTag(), Locale.FRENCH.toLanguageTag()));

    static final PropertyConfig INPUT_FILENAME_PROPERTY =
            new PropertyConfig(Constants.INPUT_FILENAME_KEY,
                               "Input filename",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public Locale getLocale(JobConfig config) {
       return Locale.forLanguageTag((String) config.getConfigProperty(LOCALE_PROPERTY));
    }

    @Autowired
    protected FieldDetectorService fieldDetectorService;

    public Optional<Dataset> process(InputStream input) {
        return process(input, JobConfig.EMPTY);
    }

    public abstract Optional<Dataset> process(InputStream input, JobConfig config);

    public abstract Set<SupportedMediaType> supportedTypes();

    protected Optional<Field> detectField(String name, Collection<Object> values) {
        return Optional.empty();
    }
}
```
# Sink

A sink is the last link in the chain. It sends the data to one or multiple receivers.

You must implement these methods:

`boolean accept(FormatType type);`

`OutputStream getOutputStream(JobConfig config) throws Exception;`

`List<PropertyConfig> getConfigProperties();`

### Code sample

```java
package ca.breakpoints.datamaker.sink.base;

import ca.breakpoints.datamaker.generator.FormatType;
import ca.breakpoints.datamaker.model.DataOutputSinkType;
import ca.breakpoints.datamaker.model.JobConfig;
import ca.breakpoints.datamaker.model.PropertyConfig;
import ca.breakpoints.datamaker.model.PropertyConfig.ValueType;
import ca.breakpoints.datamaker.model.job.JobExecution;
import ca.breakpoints.datamaker.sink.DataOutputSink;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * Will generate a downloadable file.
 */
@Slf4j
@DataOutputSinkType(compressed = true, encrypted = true)
public class FileOutputSink implements DataOutputSink {

    public static final PropertyConfig FILE_OUTPUT_PATH_PROPERTY
        = new PropertyConfig(
        "file.sink.output.filename",
        "Output file path",
        ValueType.EXPRESSION,
        "\"/tmp/\" + #dataset.name + \"-\" + T(java.lang.System).currentTimeMillis() + \".\" + #dataJob.generator.dataType.name().toLowerCase()",
        Collections.emptyList());

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(FILE_OUTPUT_PATH_PROPERTY);
    }

    public OutputStream getOutputStream(JobConfig config) throws Exception {
        JobExecution jobExecution = config.getJobExecution();

        String path = (String) config.getConfigProperty(FILE_OUTPUT_PATH_PROPERTY);

        jobExecution.getResults().add(path);

        return new FileOutputStream(new File(path));
    }
}

```

### Interface class

```java
public interface DataOutputSink extends Configurable {

    ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    boolean accept(FormatType type);

    default boolean flushable() {
        return true;
    }

    default OutputStream getOutputStream() throws Exception {
        return getOutputStream(new JobConfig());
    }

    OutputStream getOutputStream(JobConfig config) throws Exception;

    default Object parseExpression(String expression, JobConfig config) {
        EvaluationContext evaluationContext = new StandardEvaluationContext();

        evaluationContext.setVariable("dataset", config.getDataset());
        evaluationContext.setVariable("dataJob", config.getGenerateDataJob());
        evaluationContext.setVariable("jobExecution", config.getJobExecution());

        Expression exp = EXPRESSION_PARSER.parseExpression(expression);
        //return exp.getValue();
        return exp.getValue(evaluationContext);
    }

    default List<List<SimpleFieldValue>> getRecords(InputStream inputStream) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(inputStream)) {
            return (List<List<SimpleFieldValue>>) in.readObject();
        }
    }

    default List<SimpleFieldValue> getRecord(InputStream inputStream) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(inputStream)) {
            return (List<SimpleFieldValue>) in.readObject();
        }
    }

    default OutputStream encryptCompressStream(JobConfig config, OutputStream outputStream) throws Exception {
        String compressionFormat = (String) config.getConfigProperty(CompressFilter.COMPRESSION_FORMAT);
        String encryptionAlgorithm = (String) config.getConfigProperty(EncryptFilter.ENCRYPTION_ALGORITHM);

        if ("NONE".equals(compressionFormat) && "NONE".equals(encryptionAlgorithm)) {
            return outputStream;
        } else if ("NONE".equals(compressionFormat)) {
            return EncryptFilter.encryptStream(config, outputStream);
        } else if ("NONE".equals(encryptionAlgorithm)) {
            return CompressFilter.getCompressedStream(config, outputStream);
        }

        OutputStream compressedStream = CompressFilter.getCompressedStream(config, outputStream);

        return new OutputStream() {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final OutputStream stream = EncryptFilter.encryptStream(config, baos);

            @Override
            public void write(int b) throws IOException {
                stream.write(b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                stream.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                stream.write(b, off, len);
            }

            @Override
            public void flush() throws IOException {
                stream.flush();
            }

            @Override
            public void close() throws IOException {
                stream.flush();
                stream.close();
                compressedStream.write(baos.toByteArray());
                compressedStream.close();
            }
        };
    }
}
```
# Field

Dataset contains fields. A field generates a value based on it's type. 

You must implement this method:

`protected abstract V generateData();`

`List<PropertyConfig> getConfigProperties();`

### Code sample

```java
package ca.breakpoints.datamaker.model.field.type;

import ca.breakpoints.datamaker.model.PropertyConfig;
import ca.breakpoints.datamaker.model.field.Field;
import ca.breakpoints.datamaker.model.field.FieldGroup;
import ca.breakpoints.datamaker.model.field.FieldType;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import javax.persistence.Entity;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Indexed;

@NoArgsConstructor
@Entity
@Indexed
@FieldType(description = "Age, Integer [1-100]", localizationKey = "field.group.age", group = FieldGroup.IDENTITY)
public class AgeField extends Field<Integer> {

    static final PropertyConfig MINIMUM_AGE_PROPERTY =
            new PropertyConfig("field.age.minAge",
                               "Mininum age",
                               PropertyConfig.ValueType.NUMERIC,
                               1,
                               Arrays.asList(1, 125));

    static final PropertyConfig MAXIMUM_AGE_PROPERTY =
            new PropertyConfig("field.age.maxAge",
                               "Maximum age",
                               PropertyConfig.ValueType.NUMERIC,
                               125,
                               Arrays.asList(1, 125));

    public AgeField(String name, Locale locale) {
        super(name, locale);
    }

    @Override
    public Integer generateData() {
        int minAge = (int) config.getConfigProperty(MINIMUM_AGE_PROPERTY);
        int maxAge = (int) config.getConfigProperty(MAXIMUM_AGE_PROPERTY);
        if (maxAge < minAge) {
            throw new IllegalArgumentException("Maximum age should be greather than minimum age");
        }

        return ThreadLocalRandom.current().nextInt(minAge,
                                            maxAge + 1);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        List<PropertyConfig> fieldConfigProperties = Lists.newArrayList();
        fieldConfigProperties.add(MINIMUM_AGE_PROPERTY);
        fieldConfigProperties.add(MAXIMUM_AGE_PROPERTY);
        return fieldConfigProperties;
    }
}

```

### Base class

```java
package ca.breakpoints.datamaker.model.field;

import ca.breakpoints.datamaker.model.Configurable;
import ca.breakpoints.datamaker.model.Dataset;
import ca.breakpoints.datamaker.model.PropertyConfig;
import ca.breakpoints.datamaker.model.Searchable;
import ca.breakpoints.datamaker.model.field.formatter.FieldFormatter;
import ca.breakpoints.datamaker.model.field.type.NullField;
import ca.breakpoints.datamaker.repository.FieldRepository;
import ca.breakpoints.datamaker.service.BeanService;
import ca.breakpoints.datamaker.service.EncryptionService;
import ca.breakpoints.datamaker.service.FieldService;
import ai.datamaker.utils.FakerUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.javafaker.Faker;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

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
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Data
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

}

```

# Generator

A generator implementation will generate data based on a dataset definition.

You need to implement the following method:

`void generate(Dataset dataset, OutputStream outputStream) throws Exception;`

### Code sample

```java
package ca.breakpoints.datamaker.generator;

import ca.breakpoints.datamaker.model.Dataset;
import ca.breakpoints.datamaker.model.JobConfig;
import ca.breakpoints.datamaker.model.PropertyConfig;
import ca.breakpoints.datamaker.model.PropertyConfig.ValueType;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

public class TextGenerator implements DataGenerator {

    static final PropertyConfig ELEMENT_SEPARATOR =
        new PropertyConfig("text.generator.element.separator",
            "Element separator",
            PropertyConfig.ValueType.STRING,
            "",
            Collections.emptyList());

    static final PropertyConfig KEY_VALUE_SEPARATOR =
        new PropertyConfig("text.generator.key.value.separator",
            "Key value separator",
            PropertyConfig.ValueType.STRING,
            "=",
            Collections.emptyList());

    static final PropertyConfig OUTPUT_KEYS =
        new PropertyConfig("text.generator.output.keys",
            "Output keys",
            ValueType.BOOLEAN,
            "false",
            Collections.emptyList());

    @Override
    public void generate(Dataset dataset, OutputStream outputStream) throws Exception {
        generate(dataset, outputStream, JobConfig.EMPTY);
    }

    @Override
    public void generate(Dataset dataset, OutputStream outputStream, JobConfig config) throws Exception {
        dataset.processAllValues(fv -> {
            fv.forEach(value -> {
                    try {
                        if (Boolean.parseBoolean(config.getConfigProperty(OUTPUT_KEYS).toString())) {
                            outputStream.write(value.getField().getName().getBytes());
                            outputStream.write(config.getConfigProperty(KEY_VALUE_SEPARATOR).toString().getBytes());
                        }
                        outputStream.write(value.getValue().toString().getBytes());
                        outputStream.write(config.getConfigProperty(ELEMENT_SEPARATOR).toString().getBytes());
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            );
        });
    }

    @Override
    public FormatType getDataType() {
        return FormatType.TEXT;
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(ELEMENT_SEPARATOR, KEY_VALUE_SEPARATOR, OUTPUT_KEYS);
    }
}

```

### Interface class

```java
/**
 * Generate data based on a dataset.
 */
public interface DataGenerator extends Configurable {

    ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    void generate(Dataset dataset, OutputStream outputStream) throws Exception;

    default void generate(Dataset dataset, OutputStream outputStream, JobConfig config) throws Exception {
        generate(dataset, outputStream);
    }

    FormatType getDataType();

    default Object parseExpression(String expression, Dataset dataset) {
        EvaluationContext evaluationContext = new StandardEvaluationContext();

        evaluationContext.setVariable("dataset", dataset);
        // evaluationContext.setVariable("dataJob", config.getGenerateDataJob());
        // evaluationContext.setVariable("jobExecution", config.getJobExecution());

        Expression exp = EXPRESSION_PARSER.parseExpression(expression);
        //return exp.getValue();
        return exp.getValue(evaluationContext);
    }
}
```



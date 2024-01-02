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

package ai.datamaker.service;

import ai.datamaker.generator.DataGenerator;
import ai.datamaker.model.ComponentConfig;
import ai.datamaker.model.Configurable;
import ai.datamaker.model.DataGeneratorType;
import ai.datamaker.model.DataOutputSinkType;
import ai.datamaker.model.DatasetProcessorType;
import ai.datamaker.model.FieldFormatterType;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import ai.datamaker.model.field.formatter.FieldFormatter;
import ai.datamaker.processor.DatasetProcessor;
import ai.datamaker.repository.FieldRepository;
import ai.datamaker.sink.DataOutputSink;
import ai.datamaker.sink.filter.CompressFilter;
import ai.datamaker.sink.filter.EncryptFilter;
import com.google.common.base.CaseFormat;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.StreamSupport;

@Service
public class ComponentConfigurationService {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private FieldRepository fieldRepository;

    // TODO refactor to list
    @Value("${base.component.package}")
    private String baseComponentPackage;

    @SuppressWarnings("unchecked")
    public Map<String, Collection<ComponentConfig>> getAllFor(Locale locale) throws Exception {
        Multimap<String, ComponentConfig> components = ArrayListMultimap.create();

        // Create scanner and disable default filters (that is the 'false' argument)
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                false);
        // Add include filters which matches all the classes (or use your own)
        //provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));
        provider.addIncludeFilter(new AssignableTypeFilter(Configurable.class));
        provider.addExcludeFilter(new AnnotationTypeFilter(Deprecated.class, true, true));

        //provider.addIncludeFilter(new AnnotationTypeFilter(FieldType.class));

        // Get matching classes defined in the package
        // TODO support more base packages, override in config?
        final Set<BeanDefinition> classes = provider.findCandidateComponents(baseComponentPackage);

        // This is how you can load the class type from BeanDefinition instance
        for (BeanDefinition bean : classes) {
            Class<?> clazz = Class.forName(bean.getBeanClassName());

            // Filtered out deprecated
            if (clazz.isAnnotationPresent(Deprecated.class)) {
                continue;
            }
            String key = "";
            String removeSuffix = "";
            String description = "";
            String localizationKey = "";
            String name = "";
            String group = "";
            String type = "";

            List<PropertyConfig> configProperties = (List<PropertyConfig>) clazz.getMethod(
                    "getConfigProperties").invoke(clazz.getDeclaredConstructor().newInstance());

            if (DataGenerator.class.isAssignableFrom(clazz)) {
                key = "generators";
                removeSuffix = "Generator";

                if (clazz.isAnnotationPresent(DataGeneratorType.class)) {
                    DataGeneratorType dataGeneratorType = clazz.getAnnotation(DataGeneratorType.class);
                    description = dataGeneratorType.description();
                    localizationKey = dataGeneratorType.localizationKey();
                    name = dataGeneratorType.name();
                }

            } else if (Field.class.isAssignableFrom(clazz)) {
                key = "fields";
                removeSuffix = "Type";
                if (clazz.isAnnotationPresent(FieldType.class)) {
                    FieldType fieldType = clazz.getAnnotation(FieldType.class);
                    description = fieldType.description();
                    localizationKey = fieldType.localizationKey();
                    name = fieldType.name();
                    group = fieldType.group().toString();
                }

            } else if (DataOutputSink.class.isAssignableFrom(clazz)) {
                key = "sinks";
                removeSuffix = "OutputSink";
                group = clazz.getPackageName().substring(clazz.getPackageName().lastIndexOf('.') + 1);

                if (clazz.isAnnotationPresent(DataOutputSinkType.class)) {
                    DataOutputSinkType dataOutputSinkType = clazz.getAnnotation(DataOutputSinkType.class);
                    description = dataOutputSinkType.description();
                    localizationKey = dataOutputSinkType.localizationKey();
                    name = dataOutputSinkType.name();
                    if (!dataOutputSinkType.group().isEmpty()) {
                        group = dataOutputSinkType.group();
                    }
                    // Add encryption properties
                    // Add compression properties
                    if (dataOutputSinkType.compressed()) {
                        configProperties.add(CompressFilter.COMPRESSION_FORMAT);
                        configProperties.add(CompressFilter.UNCOMPRESSED_FILENAME);
                    }
                    if (dataOutputSinkType.encrypted()) {
                        EncryptFilter.addDefaultProperties(configProperties);
                    }
                }

            } else if (DatasetProcessor.class.isAssignableFrom(clazz)) {
                key = "processors";
                removeSuffix = "Processor";

                if (clazz.isAnnotationPresent(DatasetProcessorType.class)) {
                    DatasetProcessorType datasetProcessorType = clazz.getAnnotation(DatasetProcessorType.class);
                    description = datasetProcessorType.description();
                    localizationKey = datasetProcessorType.localizationKey();
                    name = datasetProcessorType.name();
                }
            } else if (FieldFormatter.class.isAssignableFrom(clazz)) {
                key = "formatters";
                removeSuffix = "Formatter";

                if (clazz.isAnnotationPresent(FieldFormatterType.class)) {
                    FieldFormatterType fieldFormatterType = clazz.getAnnotation(FieldFormatterType.class);
                    description = fieldFormatterType.description();
                    localizationKey = fieldFormatterType.localizationKey();
                    name = fieldFormatterType.name();
                }
            }

            String defaultName = WordUtils.capitalizeFully(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,
                                                                                     clazz.getSimpleName().replace(removeSuffix, ""))
                                                                   .replaceAll("_", " "));
            if (StringUtils.isNotBlank(localizationKey)) {
                description = messageSource.getMessage(localizationKey + ".description",null, description, locale);
                name = messageSource.getMessage(localizationKey, null,
                                                       defaultName,
                                                       locale);
            } else {
                localizationKey =  CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName()).replaceAll("_", ".");

                if (StringUtils.isBlank(name)) {
                    name = defaultName;
                }
            }

            ComponentConfig componentConfig = new ComponentConfig(name, description, clazz.getName(), localizationKey);
            componentConfig.setGrouping(group);
            componentConfig.getConfigProperties().addAll(configProperties);

            components.put(key, componentConfig);
        }

        // TODO merge with field aliases
        // Field group = Alias
        // components.get("fields").addAll()
        StreamSupport.stream(fieldRepository.findAllAliases().spliterator(), false).forEach(
                f -> {
                    ComponentConfig componentConfig = new ComponentConfig(StringUtils.capitalize(f.getName()),
                                                                          f.getDescription(),
                                                                          f.getClass().getName() + "-" + f.getName(),
                                                                          "field.alias." + f.getName());
                    componentConfig.setGrouping(FieldGroup.ALIAS.toString());

                    List<PropertyConfig> currentProperties = Lists.newArrayList();
                    f.getConfigProperties().forEach(cp -> {
                        Object value = f.getConfig().getConfigProperty(cp);
                        currentProperties.add(new PropertyConfig(cp.getKey(), cp.getDescription(), cp.getType(), value, Collections.emptyList()));
                    });

                    componentConfig.getConfigProperties().addAll(currentProperties);
                    components.get("fields").add(componentConfig);
                }
        );

        return components.asMap();
    }

}

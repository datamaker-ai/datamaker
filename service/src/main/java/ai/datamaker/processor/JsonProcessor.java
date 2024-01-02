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

package ai.datamaker.processor;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.SupportedMediaType;
import ai.datamaker.model.field.Field;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
@Slf4j
public class JsonProcessor extends DatasetProcessor {

    final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public Optional<Dataset> process(InputStream jsonInput, JobConfig config) {

        String datasetName = (String) config.getConfigProperty(INPUT_FILENAME_PROPERTY);
        Locale locale = getLocale(config);
        Dataset dataset = new Dataset(datasetName, locale);

        try {
            jsonInput.mark(0);
            String firstBytes = new String(jsonInput.readNBytes(1));
            jsonInput.reset();

            if (firstBytes.contains("[")) {
                TypeReference<List> typeRef = new TypeReference<>(){};
                List objects = OBJECT_MAPPER.readValue(jsonInput, typeRef);
                dataset.setExportHeader(false);

                if (CollectionUtils.isNotEmpty(objects)) {
                    Object o = objects.get(0);
                    Optional<Field> f = fieldDetectorService.detectTypeOnValue(
                            "object",
                            locale,
                            Lists.newArrayList(o instanceof Iterable ? (Iterable)o : o));
                    f.ifPresent(dataset::addField);
                }

            } else if (firstBytes.contains("{")) {
                TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
                Map<String, Object> mapObjects = OBJECT_MAPPER.readValue(jsonInput, typeRef);

                mapObjects.entrySet().forEach(e -> {

                    Optional<Field> detectedOnName = fieldDetectorService.detectTypeOnName(e.getKey(), locale);

                    List<Object> values = new ArrayList<>();
                    values.add(e.getValue());
                    Optional<Field> detectTypeOnValue = fieldDetectorService.detectTypeOnValue(e.getKey(), locale, values);

                    Optional<Field> bestMatch = fieldDetectorService.findBestMatch(detectedOnName, detectTypeOnValue);

                    bestMatch.ifPresent(dataset::addField);
                });
            } else {
                TypeReference typeRef = new TypeReference<>(){};
                Object o = OBJECT_MAPPER.readValue(jsonInput, typeRef);
                dataset.setExportHeader(false);

                Optional<Field> f = fieldDetectorService.detectTypeOnValue("object", locale, Lists.newArrayList(o));
                f.ifPresent(dataset::addField);
            }

        } catch (IOException e) {
            log.error("Error while processing json file", e);
            throw new IllegalStateException("Error while processing json file", e);
        }

        return Optional.of(dataset);
    }

    @Override
    public Set<SupportedMediaType> supportedTypes() {
        return Sets.newHashSet(SupportedMediaType.JSON);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(INPUT_FILENAME_PROPERTY,
                                  LOCALE_PROPERTY);
    }
}

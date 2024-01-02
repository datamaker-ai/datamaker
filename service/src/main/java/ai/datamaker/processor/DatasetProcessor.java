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

import ai.datamaker.model.*;
import ai.datamaker.model.field.Field;
import ai.datamaker.service.FieldDetectorService;
import java.io.InputStream;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Process input source to generate dataset automatically.
 * Determine data type based on values.
 * Apply default rules.
 */
// TODO implement strategy pattern
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

    // FIXME should throw exception???
    public abstract Optional<Dataset> process(InputStream input, JobConfig config);

    public abstract Set<SupportedMediaType> supportedTypes();

    protected Optional<Field> detectField(String name, Collection<Object> values) {
        return Optional.empty();
    }
}

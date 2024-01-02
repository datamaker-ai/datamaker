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

package ai.datamaker.model.mapper;

import ai.datamaker.model.JobConfig;
import org.mapstruct.Qualifier;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;
import java.util.UUID;

public class MapperUtils {

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface LocaleToString { }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface StringToLocale { }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface UuidToString { }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface JobConfigToJobConfig { }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface JobConfigToJobConfigResponse { }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.CLASS)
    public @interface FieldConfigToFieldConfigResponse { }

    @LocaleToString
    public static String localeToString(Locale locale) {
        return locale.toLanguageTag();
    }

    @StringToLocale
    public static Locale stringToLocale(String languageTag) {
        return Locale.forLanguageTag(languageTag);
    }

    @UuidToString
    public static String uuidToString(UUID externalId) {
        return externalId.toString();
    }

    @JobConfigToJobConfig
    public static JobConfig jobToJobConfig(JobConfig config) {
        return config;
    }

//    @JobConfigToJobConfigResponse
//    public static JobConfig jobToJobConfigResponse(JobConfig config) {
//        config.keySet().removeIf(k -> k.endsWith("password"));
//        return config;
//    }
//
//    @FieldConfigToFieldConfigResponse
//    public static FieldConfig fieldConfigToFieldConfigResponse(FieldConfig config) {
//        config.keySet().removeIf(k -> k.endsWith("password"));
//        return config;
//    }

}

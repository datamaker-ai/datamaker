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

package ai.datamaker.sink.google;

import ai.datamaker.model.PropertyConfig;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public interface GcpCommon {

    PropertyConfig GOOGLE_CLOUD_PROJECT_ID =
            new PropertyConfig("google.cloud.sink.project.id",
                               "Project ID",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    PropertyConfig GOOGLE_CLOUD_JSON_KEY =
            new PropertyConfig("google.cloud.sink.json.key",
                               "Service account JSON key",
                               PropertyConfig.ValueType.STRING,
                               "{}",
                               Collections.emptyList());

    PropertyConfig GOOGLE_CLOUD_OAUTH_TOKEN_VALUE =
            new PropertyConfig("google.cloud.sink.access.token",
                               "OAuth token value",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    PropertyConfig GOOGLE_CLOUD_AUTHENTICATION_TYPE =
            new PropertyConfig("google.cloud.sink.authentication.type",
                               "Authentication method",
                               PropertyConfig.ValueType.STRING,
                               "SERVICE_ACCOUNT",
                               Lists.newArrayList("OAUTH_TOKEN", "PLATFORM", "SERVICE_ACCOUNT"));

    default List<PropertyConfig> addDefaultProperties(List<PropertyConfig> properties) {
        List<PropertyConfig> defaultProperties = Lists.newArrayList();
        defaultProperties.add(GOOGLE_CLOUD_PROJECT_ID);
        defaultProperties.add(GOOGLE_CLOUD_JSON_KEY);
        defaultProperties.add(GOOGLE_CLOUD_OAUTH_TOKEN_VALUE);
        defaultProperties.add(GOOGLE_CLOUD_AUTHENTICATION_TYPE);

        defaultProperties.addAll(properties);
        return defaultProperties;
    }
}

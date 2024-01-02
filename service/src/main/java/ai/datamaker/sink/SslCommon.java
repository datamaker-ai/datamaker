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

import ai.datamaker.model.PropertyConfig;

import java.util.Collections;
import java.util.List;

/**
 * Trustore: certificate authority
 * Keystore: two-way authentication
 */
public interface SslCommon {
    PropertyConfig TRUSTSTORE_FILENAME =
            new PropertyConfig("truststore.filename",
                    "Truststore filename",
                    PropertyConfig.ValueType.STRING,
                    "",
                    Collections.emptyList());

    PropertyConfig TRUSTSTORE_PASSWORD =
            new PropertyConfig("truststore.password",
                    "Truststore password",
                    PropertyConfig.ValueType.PASSWORD,
                    "changeit",
                    Collections.emptyList());

    PropertyConfig KEYSTORE_FILENAME =
            new PropertyConfig("keystore.filename",
                               "Keystore filename",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    PropertyConfig KEYSTORE_PASSWORD =
            new PropertyConfig("keystore.password",
                               "Keystore password",
                               PropertyConfig.ValueType.PASSWORD,
                               "changeit",
                               Collections.emptyList());

    default List<PropertyConfig> addDefaultProperties(List<PropertyConfig> properties) {
        properties.add(TRUSTSTORE_FILENAME);
        properties.add(TRUSTSTORE_PASSWORD);
        properties.add(KEYSTORE_FILENAME);
        properties.add(KEYSTORE_PASSWORD);
        return properties;
    }
}

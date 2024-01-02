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

package ai.datamaker.sink.azure;

import ai.datamaker.model.PropertyConfig;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

public interface AzureCommon {
     PropertyConfig AZURE_STORAGE_SAS_TOKEN =
            new PropertyConfig("azure.common.sink.sas.token",
                               "Storage SAS TOKEN",
                               PropertyConfig.ValueType.PASSWORD,
                               "",
                               Collections.emptyList());

     PropertyConfig AZURE_STORAGE_ACCOUNT_NAME =
            new PropertyConfig("azure.storage.sink.account.name",
                               "Storage account name",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

     PropertyConfig AZURE_USERNAME =
             new PropertyConfig("azure.sink.username",
                                "Username",
                                PropertyConfig.ValueType.STRING,
                                "",
                                Collections.emptyList());

     PropertyConfig AZURE_PASSWORD =
             new PropertyConfig("azure.sink.password",
                                "Password",
                                PropertyConfig.ValueType.PASSWORD,
                                "",
                                Collections.emptyList());

     PropertyConfig AZURE_STORAGE_ACCOUNT_KEY =
            new PropertyConfig("azure.storage.sink.account.key",
                               "Storage account key",
                               PropertyConfig.ValueType.PASSWORD,
                               "",
                               Collections.emptyList());

     default List<PropertyConfig> addDefaultProperties(List<PropertyConfig> properties) {
          List<PropertyConfig> defaultProperties = Lists.newArrayList();

          defaultProperties.add(AZURE_STORAGE_SAS_TOKEN);
          defaultProperties.add(AZURE_STORAGE_ACCOUNT_NAME);
          defaultProperties.add(AZURE_USERNAME);
          defaultProperties.add(AZURE_PASSWORD);
          defaultProperties.add(AZURE_STORAGE_ACCOUNT_KEY);
          defaultProperties.addAll(properties);

          return defaultProperties;
     }
}

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

package ai.datamaker.model.field.type;

import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.PropertyConfig.ValueType;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Indexed
@FieldType(description = "IPv4/IPv6/MAC address", localizationKey = "field.group.networkAddress", group = FieldGroup.NETWORK)
public class NetworkField extends Field<String> {

    static final PropertyConfig NETWORK_ADDRESS_TYPE_PROPERTY =
        new PropertyConfig("field.network.type",
            "Network address type",
            PropertyConfig.ValueType.STRING,
            NetworkAddressType.IPv4.toString(),
            Arrays.stream(NetworkAddressType.values()).map(NetworkAddressType::toString).collect(Collectors.toList()));

    static final PropertyConfig PRIVATE_RANGE_PROPERTY =
        new PropertyConfig("field.network.address.private.range",
            "Use private range only",
            ValueType.BOOLEAN,
            false,
            Collections.emptyList());

    public enum NetworkAddressType {
        MAC, IPv4, IPv6, HOSTNAME, IPv4_CIDR, IPv6_CIDR;
    }

    public NetworkField(String name, Locale locale) {
        super(name,
              locale);
    }

    public void setType(NetworkAddressType networkAddressType) {
        config.put(NETWORK_ADDRESS_TYPE_PROPERTY, networkAddressType.toString());
    }

    public void setPrivateRange(boolean privateRange) {
        config.put(PRIVATE_RANGE_PROPERTY, privateRange);
    }

    @Override
    protected String generateData() {
        NetworkAddressType type = NetworkAddressType.valueOf((String) config.getConfigProperty(NETWORK_ADDRESS_TYPE_PROPERTY));

        switch (type) {
            case MAC:
                return faker.internet().macAddress();
            default:
            case IPv4:
                boolean privateRange = (boolean) config.getConfigProperty(PRIVATE_RANGE_PROPERTY);
                return privateRange ? faker.internet().privateIpV4Address() : faker.internet().publicIpV4Address();
            case IPv6:
                return faker.internet().ipV6Address();
            case HOSTNAME:
                return faker.internet().domainName();
            case IPv4_CIDR:
                return faker.internet().ipV4Cidr();
            case IPv6_CIDR:
                return faker.internet().ipV6Cidr();
        }
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(NETWORK_ADDRESS_TYPE_PROPERTY, PRIVATE_RANGE_PROPERTY);
    }
}

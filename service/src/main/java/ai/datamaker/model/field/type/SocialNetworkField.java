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
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import javax.persistence.Entity;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Indexed;

@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Social network", localizationKey = "field.group.social.network", group = FieldGroup.IDENTITY)
public class SocialNetworkField extends Field<String> {

    static final PropertyConfig SOCIAL_NETWORK_TYPE_PROPERTY =
        new PropertyConfig("field.socialnetwork.type",
            "Social network type",
            PropertyConfig.ValueType.STRING,
            SocialNetwork.PROFILE_NAME.toString(),
            Arrays.stream(SocialNetwork.values()).map(SocialNetwork::toString).collect(Collectors.toList()));

    private enum SocialNetwork {
        PROFILE_PICTURE, SOCIAL_HANDLE, PROFILE_NAME
    }

    public SocialNetworkField(String name, Locale locale) {
        super(name,
              locale);
    }

    @Override
    protected String generateData() {

        SocialNetwork socialNetwork = SocialNetwork.valueOf((String) config.getConfigProperty(SOCIAL_NETWORK_TYPE_PROPERTY));

        switch (socialNetwork) {
            case PROFILE_PICTURE:
                return faker.internet().avatar();
            case SOCIAL_HANDLE:
            case PROFILE_NAME:
                return faker.lorem().word();
        }
        return faker.lorem().word();
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(SOCIAL_NETWORK_TYPE_PROPERTY);
    }

}

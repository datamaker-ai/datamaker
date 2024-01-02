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

package ai.datamaker.sink.amazon;

import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import com.google.common.collect.Lists;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public interface AmazonAwsCommon {
    PropertyConfig AMAZON_AWS_ACCESS_KEY_ID
            = new PropertyConfig(
            "amazon.sink.access.key.id",
            "AWS Credentials access key id",
            PropertyConfig.ValueType.STRING,
            "",
            Collections.emptyList());

    PropertyConfig AMAZON_AWS_SECRET_ACCESS_KEY
            = new PropertyConfig(
            "amazon.sink.access.key.secret",
            "AWS Credentials secret access key",
            PropertyConfig.ValueType.PASSWORD,
            "",
            Collections.emptyList());

    PropertyConfig AMAZON_AWS_REGION
            = new PropertyConfig(
            "amazon.sink.region",
            "AWS Region",
            PropertyConfig.ValueType.STRING,
            Region.US_EAST_1.toString(),
            Region.regions().stream().map(Region::id).collect(Collectors.toList()));

    default List<PropertyConfig> addDefaultProperties(List<PropertyConfig> properties) {
        List<PropertyConfig> defaultProperties = Lists.newArrayList();

        defaultProperties.add(AMAZON_AWS_ACCESS_KEY_ID);
        defaultProperties.add(AMAZON_AWS_SECRET_ACCESS_KEY);
        defaultProperties.add(AMAZON_AWS_REGION);
        defaultProperties.addAll(properties);

        return defaultProperties;
    }

    default AwsCredentialsProvider getCredentials(JobConfig config) {
        return config.containsKey(AMAZON_AWS_ACCESS_KEY_ID.getKey()) ? StaticCredentialsProvider.create(AwsBasicCredentials.create((String) config.getConfigProperty(AMAZON_AWS_ACCESS_KEY_ID),
                                                                                                                                   (String) config.getConfigProperty(AMAZON_AWS_SECRET_ACCESS_KEY)))
                : DefaultCredentialsProvider.builder().build();
    }

}

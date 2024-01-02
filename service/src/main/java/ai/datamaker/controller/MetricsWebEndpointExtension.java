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

package ai.datamaker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint.MetricResponse;
import org.springframework.boot.actuate.metrics.MetricsEndpoint.Sample;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
//@EndpointWebExtension(endpoint = MetricsEndpoint.class)
@Endpoint(id = "monitor")
public class MetricsWebEndpointExtension {

    @Autowired
    private MetricsEndpoint delegate;

    @Value("#{'${actuator.exposed.metrics}'.split(',')}")
    private Set<String> exposedMetrics;

    @ReadOperation
    public Map<String, List<Sample>> listValues() {

        return delegate
                .listNames()
                .getNames()
                .stream()
                .map(m -> delegate.metric(m, null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(MetricResponse::getName, MetricResponse::getMeasurements));

//        return exposedMetrics
//            .stream()
//            .map(m -> delegate.metric(m, null))
//            .filter(Objects::nonNull)
//            .collect(Collectors.toMap(MetricResponse::getName, MetricResponse::getMeasurements));
    }


}
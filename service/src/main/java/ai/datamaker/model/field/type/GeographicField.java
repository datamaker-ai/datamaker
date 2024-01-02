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

import ai.datamaker.model.field.constraint.BoundingBox;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.PropertyConfig.ValueType;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a GeoJSON coordinates.
 *
 * Degrees of latitude are parallel so the distance between each degree remains almost constant but since degrees of longitude are farthest apart at the equator and converge at the poles, their distance varies greatly.
 *
 * Each degree of latitude is approximately 69 miles (111 kilometers) apart. The range varies (due to the earth's slightly ellipsoid shape) from 68.703 miles (110.567 km) at the equator to 69.407 (111.699 km) at the poles. This is convenient because each minute (1/60th of a degree) is approximately one [nautical] mile.
 *
 * A degree of longitude is widest at the equator at 69.172 miles (111.321) and gradually shrinks to zero at the poles. At 40° north or south the distance between a degree of longitude is 53 miles (85 km)
 *
 * What is the “right-hand-rule"?
 * When you construct a polygon, you can order the coordinates in one direction or another. If you’re drawing a circle, you might start on the left and go counter-clockwise around to meet the original point. Or, you might go clockwise.
 *
 * Here is the specification:
 *
 * A linear ring MUST follow the right-hand rule with respect to the area it bounds, i.e., exterior rings are counterclockwise, and holes are clockwise.
 */
@Entity
@NoArgsConstructor
@Indexed
@FieldType(description = "Represents GeoJSON object", localizationKey = "field.group.geographic", group = FieldGroup.PHYSICAL_LOCATION)
public class GeographicField extends Field<Object> {

    static final PropertyConfig SIZE_PROPERTY =
        new PropertyConfig("field.geographic.size",
            "Object size",
            ValueType.NUMERIC,
            10,
            Lists.newArrayList(0, Short.MAX_VALUE));

    static final PropertyConfig DELTA_FACTOR_PROPERTY =
        new PropertyConfig("field.geographic.delta.factor",
            "Steps delta factor",
            ValueType.NUMERIC,
            1.0f,
            Lists.newArrayList(0, Float.MAX_VALUE));

    static final PropertyConfig GEOGRAPHIC_TYPE_PROPERTY =
        new PropertyConfig("field.geography.type",
            "Geographic type",
            PropertyConfig.ValueType.STRING,
            GeographicType.POINT.toString(),
            Arrays.stream(GeographicType.values()).map(GeographicType::toString).collect(Collectors.toList()));

    static final PropertyConfig COUNTRY_BOUNDING_BOX_PROPERTY =
        new PropertyConfig("field.geographic.country.bounding.box",
            "Country bounding box",
            PropertyConfig.ValueType.STRING,
            null,
            Arrays.stream(BoundingBox.values()).map(BoundingBox::getName).collect(Collectors.toList()));

    static final PropertyConfig CUSTOM_BOUNDING_BOX_PROPERTY =
        new PropertyConfig("field.geographic.custom.bounding.box",
            "Custom bounding box",
            PropertyConfig.ValueType.STRING,
            "",
            Collections.emptyList());


    public GeographicField(String name, Locale locale) {
        super(name, locale);
    }

    public enum GeographicType {
        LATITUDE, LONGITUDE, COORDINATES, POINT, LINE_STRING, MULTI_LINE_STRING, POLYGON, MULTI_POLYGON, FEATURE, FEATURE_COLLECTION
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(
            GEOGRAPHIC_TYPE_PROPERTY,
            SIZE_PROPERTY,
            DELTA_FACTOR_PROPERTY,
            COUNTRY_BOUNDING_BOX_PROPERTY,
            CUSTOM_BOUNDING_BOX_PROPERTY
        );
    }

    @Override
    protected Object generateData() {

        int size = (int) config.getConfigProperty(SIZE_PROPERTY);
        float deltaFactor = (float) config.getConfigProperty(DELTA_FACTOR_PROPERTY);

        GeographicType type = GeographicType.valueOf((String) config.getConfigProperty(GEOGRAPHIC_TYPE_PROPERTY));

        switch (type) {
            default:
            case LATITUDE:
                return latitude();
            case LONGITUDE:
                return longitude();
            case COORDINATES:
            case POINT:
                return new Double[]{
                        longitude(),
                        latitude()
                };
            case LINE_STRING:
                Double[][] line = new Double[size][2];

                line[0] = new Double[]{
                  longitude(), latitude()
                };
                for (int i=1; i<size; i++) {
                    Double deltaLat = ThreadLocalRandom.current().nextDouble() / 100D;
                    Double deltaLong = ThreadLocalRandom.current().nextDouble();

                    // TODO Test if point is inside bounding box or radius
                    line[i] = new Double[]{line[i-1][0] + deltaLong, line[i-1][1] + deltaLat};
                }

                return line;
            case POLYGON:
                // Polygon is actually [][][] (inside polygon)
                // Close the loop
                // TODO make sure the edges doesn't overlap
                Double[][][] polygon = new Double[1][size+1][2];
                polygon[0][0] = new Double[]{
                        longitude(), latitude()
                };
                IntStream.range(1, size).forEach((i) -> {
                    Double deltaLat = ThreadLocalRandom.current().nextDouble();
                    Double deltaLong = ThreadLocalRandom.current().nextDouble();

                    // keep delta to 0
                    if (i != 1) {
                        int pos = faker.number().numberBetween(0, 4);
                        if (pos == 0) {
                            deltaLat = 0.0;
                        } else if (pos == 1) {
                            deltaLong = 0.0;
                        } else if (pos == 2) {
                            deltaLat = -1.0 * deltaLat;
                        } else if (pos == 3) {
                            deltaLong = -1.0 * deltaLong;
                        }
                    }

                    polygon[0][i] = new Double[]{polygon[0][i-1][0] + deltaLong, polygon[0][i-1][1] + deltaLat};

                });
                polygon[0][size] = polygon[0][0];
                return polygon;
        }
    }

    private Double latitude() {

        if (config.containsKey(COUNTRY_BOUNDING_BOX_PROPERTY.getKey())) {

            Optional<BoundingBox> geoFenceConstraintOptional = BoundingBox.fromName((String) config.getConfigProperty(COUNTRY_BOUNDING_BOX_PROPERTY));

            if (geoFenceConstraintOptional.isPresent()) {

                return ThreadLocalRandom.current().nextDouble(
                    geoFenceConstraintOptional.get().getSouthLatitude(),
                    geoFenceConstraintOptional.get().getNorthLatitude());
            }
        }

        return (ThreadLocalRandom.current().nextDouble() * 180) - 90;
    }

    private Double longitude() {
        if (config.containsKey(COUNTRY_BOUNDING_BOX_PROPERTY.getKey())) {

            Optional<BoundingBox> geoFenceConstraintOptional = BoundingBox.fromName((String) config.getConfigProperty(COUNTRY_BOUNDING_BOX_PROPERTY));

            if (geoFenceConstraintOptional.isPresent()) {

                return ThreadLocalRandom.current().nextDouble(
                        geoFenceConstraintOptional.get().getWestLongitude(),
                        geoFenceConstraintOptional.get().getEastLongitude());
            }
        }

        return (ThreadLocalRandom.current().nextDouble() * 360) - 180;
    }

    public void setSize(int size) {
        config.put(SIZE_PROPERTY, size);
    }

    public void setType(GeographicType geographicType) {
        config.put(GEOGRAPHIC_TYPE_PROPERTY, geographicType.toString());
    }

}

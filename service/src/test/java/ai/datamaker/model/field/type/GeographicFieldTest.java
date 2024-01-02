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
import ai.datamaker.model.field.formatter.GeographyFormatter;
import ai.datamaker.model.field.formatter.GeographyFormatter.FormatType;
import ai.datamaker.model.field.type.GeographicField;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GeographicFieldTest {

    @Test
    void generateData_point() {
        GeographicField field = new GeographicField();
        Object geo = field.generateData();
        assertNotNull(geo);
        assertTrue(Arrays.isArray(geo));
        assertEquals(2, ((Object[])geo).length);
    }

    @Test
    void generateData_line() {
        GeographicField field = new GeographicField();
        field.setSize(10);
        field.setType(GeographicField.GeographicType.LINE_STRING);
        Double[][] geo = (Double[][]) field.generateData();

        assertNotNull(geo);
        //System.out.println(java.util.Arrays.deepToString(geo));
        assertEquals(10, geo.length);
    }

    @Test
    void generateData_line_Canada() {
        GeographicField field = new GeographicField();
        field.setSize(10);
        field.getConfig().put(GeographicField.COUNTRY_BOUNDING_BOX_PROPERTY.getKey(), BoundingBox.CA.getName());
        field.setType(GeographicField.GeographicType.LINE_STRING);
        Double[][] geo = (Double[][]) field.generateData();

        assertNotNull(geo);
        assertEquals(10, geo.length);
    }

    @Test
    void getData_point_formatted() {
        GeographicField field = new GeographicField("coordinates", Locale.ENGLISH);

        field.getConfig().put(GeographyFormatter.GEOGRAPHY_FORMAT_TYPE_PROPERTY.getKey(),
                              FormatType.AS_STRING.toString());
        GeographyFormatter formatter = new GeographyFormatter();
        field.setFormatter(formatter);

        Object response = field.getData();
        Assertions.assertTrue(response instanceof String);
        Assertions.assertTrue(response.toString().matches("-?\\d+(.\\d+)?,-?\\d+(.\\d+)?"));
    }

    @Test
    void generateData_polygon_Canada() {
        GeographicField field = new GeographicField();
        GeographyFormatter formatter = new GeographyFormatter();

        field.getConfig().put(GeographyFormatter.GEOGRAPHY_FORMAT_TYPE_PROPERTY.getKey(),
                              FormatType.GEO_JSON.toString());
        field.getConfig().put(GeographyFormatter.TEMPLATE_PROPERTY.getKey(),
                              GeographyFormatter.POLYGON_TEMPLATE.toString());

        field.setFormatter(formatter);
        field.setSize(50);
        field.getConfig().put(GeographicField.COUNTRY_BOUNDING_BOX_PROPERTY.getKey(), BoundingBox.CA.getName());
        field.setType(GeographicField.GeographicType.POLYGON);

        Double[][][] geo = (Double[][][]) field.generateData();
        assertNotNull(geo);
        //System.out.println(java.util.Arrays.deepToString(geo));

        Object object = field.getData();
        assertTrue(object.toString().contains("Polygon"));
    }

}
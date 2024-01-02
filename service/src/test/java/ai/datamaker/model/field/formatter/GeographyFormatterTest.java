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

package ai.datamaker.model.field.formatter;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import ai.datamaker.model.field.FieldConfig;
import ai.datamaker.model.field.formatter.GeographyFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GeographyFormatterTest {

    @Test
    void format() {
        GeographyFormatter formatter = new GeographyFormatter();

        Double[] values = new Double[]{
                -105.01621,
                39.57422
        };

        Object response = formatter.format(values, new FieldConfig());

        assertTrue(response instanceof Double[]);
    }

    @Test
    void format_latitude() {
        GeographyFormatter formatter = new GeographyFormatter();
        FieldConfig fieldConfig = new FieldConfig();
        fieldConfig.put(GeographyFormatter.GEOGRAPHY_FORMAT_TYPE_PROPERTY.getKey(),
                        GeographyFormatter.FormatType.LATITUDE.toString());


        Double[] values = new Double[]{
                -105.01621,
                39.57422
        };

        Object response = formatter.format(values, fieldConfig);
        assertEquals(values[1], (Double) response);
    }

    @Test
    void format_longitude() {
        GeographyFormatter formatter = new GeographyFormatter();
        FieldConfig fieldConfig = new FieldConfig();
        fieldConfig.put(GeographyFormatter.GEOGRAPHY_FORMAT_TYPE_PROPERTY.getKey(),
                        GeographyFormatter.FormatType.LONGITUDE.toString());

        Double[] values = new Double[]{
                -105.01621,
                39.57422
        };

        Object response = formatter.format(values, fieldConfig);
        assertEquals(values[0], (Double) response);
    }

    @Test
    void format_asString() {

        GeographyFormatter formatter = new GeographyFormatter();
        FieldConfig fieldConfig = new FieldConfig();
        fieldConfig.put(GeographyFormatter.GEOGRAPHY_FORMAT_TYPE_PROPERTY.getKey(),
                        GeographyFormatter.FormatType.AS_STRING.toString());

        Double[] values = new Double[]{
                -105.01621,
                39.57422
        };

        Object response = formatter.format(values, fieldConfig);

        assertTrue(response instanceof String);
        assertTrue(response.toString().matches("-?\\d+(.\\d+)?,-?\\d+(.\\d+)?"));
    }

    @Test
    void format_withTemplate() {

        GeographyFormatter formatter = new GeographyFormatter();
        FieldConfig fieldConfig = new FieldConfig();
        fieldConfig.put(GeographyFormatter.GEOGRAPHY_FORMAT_TYPE_PROPERTY.getKey(),
                        GeographyFormatter.FormatType.GEO_JSON.toString());
        fieldConfig.put(GeographyFormatter.TEMPLATE_PROPERTY.getKey(),
                        GeographyFormatter.POLYGON_TEMPLATE);

        Double[][] values = new Double[][]{
                new Double[]{100.0, 0.0},
                new Double[]{101.0, 0.0},
                new Double[]{101.0, 1.0},
                new Double[]{100.0, 1.0},
                new Double[]{100.0, 0.0}};

        Object response = formatter.format(values, fieldConfig);

        assertTrue(response instanceof String);

        assertTrue(response.toString().contains("Polygon"));
    }

    @Test
    void format_invalidValues() {

        GeographyFormatter formatter = new GeographyFormatter();

        Integer values = 65;

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            formatter.format(values, new FieldConfig());
        });
    }
}
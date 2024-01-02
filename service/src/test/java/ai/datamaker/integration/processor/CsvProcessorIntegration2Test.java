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

package ai.datamaker.integration.processor;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.field.type.AddressField;
import ai.datamaker.model.field.type.DateTimeField;
import ai.datamaker.model.field.type.DemographicField;
import ai.datamaker.model.field.type.DoubleField;
import ai.datamaker.model.field.type.EmailField;
import ai.datamaker.model.field.type.EmptyField;
import ai.datamaker.model.field.type.JobField;
import ai.datamaker.model.field.type.LongField;
import ai.datamaker.model.field.type.NameField;
import ai.datamaker.model.field.type.NetworkField;
import ai.datamaker.model.field.type.SequenceField;
import ai.datamaker.model.field.type.TextField;
import ai.datamaker.model.field.type.UrlField;
import ai.datamaker.processor.CsvProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.InputStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
//@AutoConfigureTestDatabase
public class CsvProcessorIntegration2Test {

    @Autowired
    private CsvProcessor datasetProcessor;

    @Test
    void process() {
        InputStream input = getClass().getClassLoader().getResourceAsStream("userdata1.csv");

        Optional<Dataset> datasetOptional = datasetProcessor.process(input);

        assertTrue(datasetOptional.isPresent());

        assertThat(datasetOptional.get().getFields())
            .hasSize(13)
            .extracting("name")
            .containsExactly("registration_dttm",
                "id",
                "first_name",
                "last_name",
                "email",
                "gender",
                "ip_address",
                "cc",
                "country",
                "birthdate",
                "salary",
                "title",
                "comments");

        assertThat(datasetOptional.get().getFields())
            .hasSize(13)
            .extractingResultOf("getClass")
            .containsExactly(DateTimeField.class,
                SequenceField.class,
                NameField.class,
                NameField.class,
                EmailField.class,
                DemographicField.class,
                NetworkField.class,
                LongField.class,
                AddressField.class,
                DateTimeField.class,
                DoubleField.class,
                JobField.class,
                TextField.class);
    }

    @Test
    void process_complex_case() {
        InputStream input = getClass().getClassLoader().getResourceAsStream("WATCHLIST.csv");

        Optional<Dataset> datasetOptional = datasetProcessor.process(input);

        assertTrue(datasetOptional.isPresent());

        assertThat(datasetOptional.get().getFields())
            .hasSize(17)
            .extracting("name")
            .containsExactly("Position",
                "Const",
                "Created",
                "Modified",
                "Description",
                "Title",
                "URL",
                "Title Type",
                "IMDb Rating",
                "Runtime (mins)",
                "Year",
                "Genres",
                "Num Votes",
                "Release Date",
                "Directors",
                "Your Rating",
                "Date Rated");

        assertThat(datasetOptional.get().getFields())
            .hasSize(17)
            .extractingResultOf("getClass")
            .containsExactly(LongField.class,
                TextField.class,
                DateTimeField.class,
                DateTimeField.class,
                EmptyField.class,
                JobField.class,
                UrlField.class,
                TextField.class,
                DoubleField.class,
                LongField.class,
                LongField.class,
                TextField.class,
                LongField.class,
                DateTimeField.class,
                TextField.class,
                EmptyField.class,
                EmptyField.class);
    }
}

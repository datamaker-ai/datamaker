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

package ai.datamaker.e2e.client;

import ai.datamaker.client.PixabayRestClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Disabled
class PixabayRestClientTest {

    // FIXME mock

    @Test
    void getImage() throws Exception {
        PixabayRestClient imageClient = new PixabayRestClient();

        ReflectionTestUtils.setField(imageClient, "apiKey","14719005-4b725ac1565332966a2f910f7");
        ReflectionTestUtils.setField(imageClient, "endpoint","https://pixabay.com/api");

        byte[] result = imageClient.getImage(480,
                             640,
                             Locale.ENGLISH,
                             "motor");

        Path image = Files.createTempFile("img", "img");
        Files.write(image, result);
    }
}
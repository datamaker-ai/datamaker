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

package ai.datamaker.integration;

import ai.datamaker.model.field.type.ImageField;
import ai.datamaker.model.field.type.ImageField.FormatType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("integration")
class ImageFieldIntegrationTest {

  @Test
  void generateData() throws IOException {
    ImageField imageField = new ImageField();
    imageField.setWidth(640);
    imageField.setHeight(320);
    imageField.setFormatType(FormatType.TIF);
    File tempFile = File.createTempFile("img", "image.tif");
    tempFile.deleteOnExit();

    Files.write(tempFile.toPath(), (byte[]) imageField.getData());
  }

  @Test
  @Disabled
  void generateData_withProvider() throws IOException {
    ImageField imageField = new ImageField();
    imageField.setWidth(640);
    imageField.setHeight(320);
    imageField.setFormatType(FormatType.JPEG);
    imageField.setGeneratedImage(false);

    //FileOutputStream fos = new FileOutputStream(new File("./temp/image.png"));
    //fos.write(imageField.generateData());
    //System.out.println(Arrays.toString(ImageIO.getWriterFormatNames()));

    File tempFile = File.createTempFile("", "image.tif");
    tempFile.deleteOnExit();

    Files.write(tempFile.toPath(), (byte[]) imageField.getData());
  }
}
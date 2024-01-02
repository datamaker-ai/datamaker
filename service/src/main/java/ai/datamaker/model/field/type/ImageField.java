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

import ai.datamaker.client.ContentProvider;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.PropertyConfig.ValueType;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import ai.datamaker.service.BeanService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.search.annotations.Indexed;

import javax.imageio.ImageIO;
import javax.persistence.Entity;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Data
@Slf4j
@NoArgsConstructor
@Entity
@Indexed
@FieldType(description = "Image content", localizationKey = "field.group.multimedia.image", group = FieldGroup.MULTIMEDIA)
public class ImageField extends Field<byte[]> {

  static final PropertyConfig IMAGE_FORMAT_TYPE_PROPERTY =
      new PropertyConfig("field.image.format.type",
          "Image format type",
          PropertyConfig.ValueType.STRING,
          FormatType.JPEG.toString(),
          Arrays.stream(FormatType.values()).map(FormatType::toString).collect(Collectors.toList()));

  static final PropertyConfig IMAGE_WIDTH_PROPERTY =
      new PropertyConfig("field.image.width",
          "Image width",
          PropertyConfig.ValueType.NUMERIC,
          640,
          Collections.emptyList());

  static final PropertyConfig IMAGE_HEIGHT_PROPERTY =
      new PropertyConfig("field.image.height",
          "Image height",
          PropertyConfig.ValueType.NUMERIC,
          480,
          Collections.emptyList());

  static final PropertyConfig SEARCH_TERM_PROPERTY =
      new PropertyConfig("field.image.search.term",
          "Search term",
          ValueType.STRING,
          "",
          Collections.emptyList());

  static final PropertyConfig IMAGE_GENERATED_PROPERTY =
      new PropertyConfig("field.image.generated",
          "Computer generated image or use stock images",
          ValueType.BOOLEAN,
          true,
          Collections.emptyList());

  public enum FormatType {
    JPEG, PNG, GIF, BMP, WBMP, TIF, TIFF;
  }

  @Override
  protected byte[] generateData() {
    boolean generatedImage = (boolean) config.getConfigProperty(IMAGE_GENERATED_PROPERTY);

    return generatedImage ? getGeneratedImage() : getImageFromProvider();
  }

  @Override
  public List<PropertyConfig> getConfigProperties() {
    return Lists.newArrayList(
        IMAGE_FORMAT_TYPE_PROPERTY,
        IMAGE_WIDTH_PROPERTY,
        IMAGE_HEIGHT_PROPERTY
    );
  }
  private byte[] getGeneratedImage() {

    int width = (int) config.getConfigProperty(IMAGE_WIDTH_PROPERTY);

    int height = (int) config.getConfigProperty(IMAGE_HEIGHT_PROPERTY);

    //create buffered image object img
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

    //create random image pixel by pixel
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int a = ThreadLocalRandom.current().nextInt() * 256;   //alpha
        int r = ThreadLocalRandom.current().nextInt() * 256;   //red
        int g = ThreadLocalRandom.current().nextInt() * 256;   //green
        int b = ThreadLocalRandom.current().nextInt() * 256;   //blue

        int p = (a << 24) | (r << 16) | (g << 8) | b; //pixel

        img.setRGB(x, y, p);
      }
    }

    //write image
    try {
      FormatType formatType = FormatType.valueOf((String) config.getConfigProperty(IMAGE_FORMAT_TYPE_PROPERTY));

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ImageIO.write(img, formatType.name().toLowerCase(), bos);
      return bos.toByteArray();
    } catch (IOException e) {
      log.error("Cannot create image", e);
    }
    return new byte[0];
  }

  @VisibleForTesting
  byte[] getImageFromProvider() {
    ContentProvider contentProvider = BeanService.getBean(ContentProvider.class);

    int width = (int) config.getConfigProperty(IMAGE_WIDTH_PROPERTY);

    int height = (int) config.getConfigProperty(IMAGE_HEIGHT_PROPERTY);

    String searchTerm = (String) config.getConfigProperty(SEARCH_TERM_PROPERTY);

    // TODO convert type
    return contentProvider.getImage(width, height, getLocale(), searchTerm);
  }

  public void setWidth(int width) {
    config.put(IMAGE_WIDTH_PROPERTY, width);
  }

  public void setHeight(int height) {
    config.put(IMAGE_HEIGHT_PROPERTY, height);
  }

  public void setGeneratedImage(boolean generated) {
    config.put(IMAGE_GENERATED_PROPERTY, generated);
  }

  public void setFormatType(FormatType formatType) {
    config.put(IMAGE_FORMAT_TYPE_PROPERTY, formatType.toString());
  }
}

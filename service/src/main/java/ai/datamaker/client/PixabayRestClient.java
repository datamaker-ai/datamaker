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

package ai.datamaker.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(name = "media.content.provider.image", havingValue = "pixabay", matchIfMissing = true)
@Slf4j
public class PixabayRestClient implements ContentProvider {

  // FIXME Build internal cache or download images locally/DB

  @Value("${media.content.provider.pixabay.api.key}")
  private String apiKey;

  @Value("${media.content.provider.pixabay.endpoint.url=https://pixabay.com/api}")
  private String endpoint;

  @Override
  public byte[] getImage(int width, int height, Locale locale, String searchTerm) {
    try {

      URL url = new URL(String.format("%s/?key=%s&q=%s&image_type=photo&min_width=%d&min_height=%d&lang=%s",
                                      endpoint,
                                      apiKey,
                                      searchTerm == null ? "" : searchTerm,
                                      width,
                                      height,
                                      locale.getLanguage()));
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Accept", "application/json");

      conn.getHeaderFields().forEach((k,v) -> log.debug(k + "=" + v));

      if (conn.getResponseCode() != 200) {
        String response = IOUtils.toString(conn.getInputStream(), StandardCharsets.UTF_8.name());
        log.error("Error while calling rest server: Http error code: {}, message: {}",
            conn.getResponseCode(),
            response);
        return new byte[0];
      }

      ObjectMapper objectMapper = new ObjectMapper();
      WebResponse webResponse = objectMapper.readValue(conn.getInputStream(), WebResponse.class);

      conn.disconnect();

      if (webResponse != null && webResponse.getHits().size() > 0) {
        URL imageUrl = new URL(webResponse.getHits().get(0).getLargeImageURL());
        try (InputStream in = new BufferedInputStream(imageUrl.openStream());
            ByteArrayOutputStream out = new ByteArrayOutputStream()) {

          byte[] buf = new byte[1024];
          int n = 0;
          while (-1 != (n = in.read(buf))) {
            out.write(buf, 0, n);
          }
          return out.toByteArray();
        }
      }

    } catch (Exception e) {
      log.error("Error while calling rest server", e);
    }

    return new byte[0];
  }

  @Override
  public List<byte[]> getImages(int count,
                                int width,
                                int height,
                                Locale locale,
                                String searchTerm) {
    return null;
  }

  @Data
  public static class WebResponse {

    private int totalHits;
    private List<ImageResult> hits;
    private int total;
  }

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ImageResult {

    private String largeImageURL;
    private int webformatHeight;
    private int webformatWidth;
    private int likes;
    private int imageWidth;
    private int id;
    private int user_id;
    private int views;
    private int comments;
    private String pageURL;
    private int imageHeight;
    private String webformatURL;
    private String type;
    private int previewHeight;
    private String tags;
    private int downloads;
    private String user;
    private int favorites;
    private int imageSize;
    private int previewWidth;
    private String userImageURL;
    private String previewURL;
  }
}

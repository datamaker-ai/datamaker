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

import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@Entity
@Indexed
@FieldType(description = "Multimedia content", localizationKey = "field.group.multimedia", group = FieldGroup.CUSTOM)
public class MultimediaField extends Field<Object> {

  @Setter
  @Getter
  private MultimediaType type;

  public enum MultimediaType {
    IMAGE, VIDEO, SOUND;
  }

  // https://www.techradar.com/news/the-best-free-stock-video-sites

  // https://www.bensound.com/royalty-free-music

  // https://www.pacdv.com/sounds/ambience_sounds.html

  // http://soundbible.com/2188-Formula-1-Racing.html

  // https://www.online-tech-tips.com/computer-tips/free-sound-effects/
  @Override
  protected Object generateData() {
    // FIXME implements
    switch (type) {
      // TODO merge imagefiled or call
      case IMAGE:
        break;
      case VIDEO:
        break;
      case SOUND:
        break;
    }
    return null;
  }

  @Override
  public List<PropertyConfig> getConfigProperties() {
    return Collections.emptyList();
  }
}

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

package ai.datamaker.utils.bridge;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.TwoWayFieldBridge;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

public class PrimitiveCollectionBridge implements TwoWayFieldBridge {

    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        Collection<Object> collection = (Collection<Object>) value;

        // TODO improve
        luceneOptions.addFieldToDocument(name, StringUtils.join(collection, ","), document);
    }

    @Override
    public Object get(String name, Document document) {
        String value = document.get(name);

        return value != null ? value.split(",") : Collections.EMPTY_LIST;
    }

    @Override
    public String objectToString(Object object) {
        // Collection<Object> collection = (Collection<Object>) object;

        if (object instanceof String) {
            return (String) object;
        } else if (object instanceof UUID) {
            return object.toString();
        }
        return object.toString();
    }
}

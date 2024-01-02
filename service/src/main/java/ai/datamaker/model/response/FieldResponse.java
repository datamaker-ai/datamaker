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

package ai.datamaker.model.response;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import lombok.Data;

@Data
public class FieldResponse implements Serializable {
    private String externalId;
    private String datasetId;
    private String name;
    private String description;
    private String languageTag;
    private Integer position;
    private Date dateCreated;
    private Date dateModified;
    private String className;
    private String formatterClassName;
    private String nullValue;
    private Boolean isNullable;
    private Boolean isAttribute;
    private Boolean isAlias;
    private Boolean isNested;
    protected Boolean isPrimaryKey;
    private Map<String, Object> config;
}

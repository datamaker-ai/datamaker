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

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class GenerateDataJobResponse {
    private String externalId;
    private String name;
    private String description;
    private Long numberOfRecords;
    private Boolean randomizeNumberOfRecords;
    private Boolean flushOnEveryRecord ;
    private Boolean streamForever;
    private String schedule;
    private Boolean runStatus;
    private Long size;
    private Boolean useBuffer;
    private Integer bufferSize;
    private Integer threadPoolSize;
    private String generator;
    private Map<String, Object> config;
    private List<String> sinks;
    private List<String> datasets;
    private String workspaceId;
    private Date dateCreated;
    private Date dateModified;
    private Boolean replayable;
    private Integer replayHistorySize;
}

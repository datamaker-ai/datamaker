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

import com.google.common.collect.Lists;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class JobExecutionResponse {
    private Boolean dataJobReplayable;
    private String externalId;
    private Boolean isSuccess;
    private String state;
    private Date startTime;
    private Date endTime;
    private Date cancelTime;
    private Long numberOfRecords;
    private Boolean replay = false;
    private Boolean replayLogFound = false;
    private List<String> errors = Lists.newArrayList();
    private List<String> results = Lists.newArrayList();
}

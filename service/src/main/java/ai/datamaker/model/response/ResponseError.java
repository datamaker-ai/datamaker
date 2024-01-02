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

import lombok.Builder;
import lombok.Getter;

@Getter
public class ResponseError extends ApiResponse {
    // Human-readable description of the specific error
    private String detail;

    // A URL to a document describing the error condition (optional, and "about:blank" is assumed if none is provided; should resolve to a human-readable document).
    private String type = "about:blank";

    // A short, human-readable title for the general error type; the title should not change for given types.
    private String title;

    // Conveying the HTTP status code; this is so that all information is in one place, but also to correct for changes in the status code due to the usage of proxy servers.
    // The status member, if present, is only advisory as generators MUST use the same status code in the actual HTTP response to assure that generic HTTP software that does not understand this format still behaves correctly.
    private int status;

    // This optional key may be present, with a unique URI for the specific error; this will often point to an error log for that specific response.
    private String instance;

    @Builder
    public ResponseError(String localizationKey, String detail, String type, String title, int status, String instance) {
        super(localizationKey);
        this.success = false;
        this.detail = detail;
        this.type = type;
        this.title = title;
        this.status = status;
        this.instance = instance;
    }
}

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

package ai.datamaker.utils;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CapturingMatcher extends BaseMatcher<Object> {

    private List<Object> matchedList = new ArrayList<>();

    @Override
    public boolean matches(Object matched) {
        matchedList.add(matched);
        return true;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("any object");
    }

    /**
     * The last value matched.
     */
    public Object getLastMatched() {
        return matchedList.get(matchedList.size() - 1);
    }

    /**
     * All the values matched, in the order they were requested for
     * matching.
     */
    public List<Object> getAllMatched() {
        return Collections.unmodifiableList(matchedList);
    }
}
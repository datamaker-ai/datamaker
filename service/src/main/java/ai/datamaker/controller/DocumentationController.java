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

package ai.datamaker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class DocumentationController {

    @RequestMapping(path = {"/docs", "/docs/", "/docs/{directory}/"})
    public String documentation(@PathVariable(required = false) String directory, HttpServletRequest request) {
        if (directory != null) {
            return "forward:/docs/" + directory + "/index.html";
        }
        return "forward:/docs/index.html";

     //   return "docs/index.html";
    }

}

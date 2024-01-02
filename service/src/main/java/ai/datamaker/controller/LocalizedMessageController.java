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

import ai.datamaker.model.Constants;
import ai.datamaker.model.response.ApiResponse;
import ai.datamaker.model.response.ResponseSuccess;
import ai.datamaker.service.ComponentConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/messages")
public class LocalizedMessageController {

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ComponentConfigurationService componentConfigurationService;

    @GetMapping(path = "/bundle/{baseName}")
    public ResponseEntity<ApiResponse> getAllMessagesForBundle(@NotBlank @PathVariable String baseName, Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale);

        Iterable<String> iterable = () -> bundle.getKeys().asIterator();

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .objectType(Constants.MESSAGES_OBJECT)
                                         .payload(StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toMap(k -> k, bundle::getString)))
                                         .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllMessages(Locale locale) {
        return getAllMessagesForBundle("messages", locale);
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse> getMessage(@NotBlank @PathVariable String code, Locale locale) {
        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .objectType(Constants.MESSAGES_OBJECT)
                                         .payload(messageSource.getMessage(code, null, locale))
                                         .build());

    }

    @GetMapping("/components")
    public ResponseEntity<ApiResponse> getPropertiesForComponent(Locale locale) throws Exception {

        return ResponseEntity.ok(ResponseSuccess
                                         .builder()
                                         .objectType(Constants.COMPONENTS_OBJECT)
                                         .payload(componentConfigurationService.getAllFor(locale))
                                         .build());

    }
}

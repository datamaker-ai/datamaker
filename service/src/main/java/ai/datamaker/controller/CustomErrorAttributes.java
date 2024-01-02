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

import com.google.common.collect.Maps;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {

        // Let Spring handle the error first, we will modify later :)
        Map<String, Object> errorAttributes = Maps.newHashMap(); //super.getErrorAttributes(webRequest, includeStackTrace);

        errorAttributes.put("localizationKey", "");
        errorAttributes.put("success", false);
        errorAttributes.put("type", null);
        errorAttributes.put("title", "global error");
        Throwable t = getError(webRequest);
        errorAttributes.put("detail", t != null ? t.getMessage() : null);
        errorAttributes.put("timestamp", new Date());
        int status = getAttribute(webRequest, "javax.servlet.error.status_code");
        Object message = getAttribute(webRequest, "javax.servlet.error.message");
        if ((!StringUtils.isEmpty(message) || errorAttributes.get("detail") == null)
                && !(t instanceof BindingResult)) {
            errorAttributes.put("detail", StringUtils.isEmpty(message) ? "No message available" : message);
        }

        errorAttributes.put("status", status);
        errorAttributes.put("instance", null);

        return errorAttributes;
    }

    @SuppressWarnings("unchecked")
    private <T> T getAttribute(RequestAttributes requestAttributes, String name) {
        return (T) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
    }

}

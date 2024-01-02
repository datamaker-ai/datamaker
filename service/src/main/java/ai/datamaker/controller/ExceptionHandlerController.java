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

import ai.datamaker.exception.ForbiddenException;
import ai.datamaker.exception.InvalidParameterException;
import ai.datamaker.model.response.ApiResponse;
import ai.datamaker.model.response.ResponseError;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException.Forbidden;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RestControllerAdvice
@Slf4j
public class ExceptionHandlerController {

    public static final String DEFAULT_ERROR_VIEW = "error";

    @Autowired
    private MessageSource messageSource;

    @RequestMapping("/api/**")
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse handlerNotMappingRequest(HttpServletRequest request) {

        return ResponseError
                .builder()
                .title("path not found")
                .detail(request.getRequestURI())
                .status(HttpStatus.NOT_FOUND.value())
                .build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ApiResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseError
                .builder()
                .title("validation errors")
                .detail(Joiner.on(",").withKeyValueSeparator("=").join(errors))
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({DataIntegrityViolationException.class, IllegalArgumentException.class})
    public ApiResponse handleDataIntegrityException(RuntimeException ex) {
        Locale locale = LocaleContextHolder.getLocale();

        return ResponseError
            .builder()
            .title(messageSource.getMessage("validation.errors", null, locale))
//            .detail(ex instanceof DataIntegrityViolationException ?
//                            messageSource.getMessage("name.already.exists", new String[]{""}, locale) :
//                            ex.getLocalizedMessage())
            .detail(ex.getLocalizedMessage())
            .status(HttpStatus.BAD_REQUEST.value())
            .build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({InvalidParameterException.class})
    public ApiResponse handleIllegalArgumentException(InvalidParameterException ex) {
        Locale locale = LocaleContextHolder.getLocale();

        return ResponseError
                .builder()
                .title(messageSource.getMessage("validation.errors", null, locale))
                .detail(messageSource.getMessage(ex.getMessageProperty(), ex.getObjects(), locale))
                .status(HttpStatus.BAD_REQUEST.value())
                .build();
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NoSuchElementException.class, UsernameNotFoundException.class})
    public ApiResponse handleNotFoundExceptions(RuntimeException ex) {

        return ResponseError
                .builder()
                .title("element not found")
                .detail(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .build();
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({ ForbiddenException.class, Forbidden.class })
    public ApiResponse handleForbidden(Exception ex) {

        return ResponseError
            .builder()
            .title("user forbidden")
            .detail(ex.getMessage())
            .status(HttpStatus.FORBIDDEN.value())
            .build();
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(Unauthorized.class)
    public ApiResponse handleUnauthorized(Unauthorized ex) {

        return ResponseError
            .builder()
            .title("user unauthorized")
            .detail(ex.getMessage())
            .status(HttpStatus.UNAUTHORIZED.value())
            .build();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = {Exception.class, RuntimeException.class})
    public ApiResponse defaultErrorHandler(HttpServletRequest request, Exception e) {

        log.error("Error while processing request", e);

        return ResponseError
                .builder()
                .title("global error")
                .detail(e.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();
    }

}
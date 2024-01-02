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

package ai.datamaker.model.field;

import ai.datamaker.model.field.formatter.FieldFormatter;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.service.BeanService;
import ai.datamaker.service.EncryptionService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.persistence.Transient;
import java.util.HashMap;

/**
 * Configuration properties for:
 * {@link Field},
 * {@link FieldFormatter}
 */
public class FieldConfig extends HashMap<String, Object> {

    private static final long serialVersionUID = 4818011798533739005L;

    public static FieldConfig EMPTY = new FieldConfig();

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    @Getter
    @Setter
    @Transient
    private transient Dataset dataset;

    @Getter
    @Setter
    @Transient
    private transient Field field;

    public Object parseExpression(String expression) {
        EvaluationContext evaluationContext = new StandardEvaluationContext();

        evaluationContext.setVariable("dataset", dataset);
        evaluationContext.setVariable("field", field);

        // evaluationContext.setVariable("dataJob", generateDataJob);
        // evaluationContext.setVariable("jobExecution", jobExecution);

        Expression exp = EXPRESSION_PARSER.parseExpression(expression);
        //return exp.getValue();
        return exp.getValue(evaluationContext);
    }

    public String getProperty(String propertyName) {
        Object value = get(propertyName);

        return value != null ? value.toString() : null;
    }

    public String getProperty(String propertyName, String defaultValue) {
        Object value = getOrDefault(propertyName, defaultValue);

        return value != null ? value.toString() : null;
    }

    /**
     * Will return the default or computed value. Takes in charge of decryption.
     * @param propertyConfig
     * @return
     */
    public Object getConfigProperty(PropertyConfig propertyConfig) {
        Object value = getOrDefault(propertyConfig.getKey(), propertyConfig.getDefaultValue());

        if (propertyConfig.getType() == PropertyConfig.ValueType.EXPRESSION) {
            return parseExpression((String) value);
        }

        if (propertyConfig.getType() == PropertyConfig.ValueType.PASSWORD ||
                propertyConfig.getType() == PropertyConfig.ValueType.SECRET) {
            String originalValue = (String) value;
            if (originalValue.startsWith("enc-")) {
                EncryptionService encryptionService = BeanService.getBean(EncryptionService.class);
                return encryptionService.decrypt(originalValue.substring(4));
            }
        }

        if (propertyConfig.getType() == PropertyConfig.ValueType.NUMERIC && value instanceof String) {
            return Double.valueOf(value.toString());
        }

        if (propertyConfig.getType() == PropertyConfig.ValueType.BOOLEAN && value instanceof String) {
            return Boolean.valueOf(value.toString());
        }

        return value;
    }

    public void put(PropertyConfig propertyConfig, Object value) {
        put(propertyConfig.getKey(), value);
    }

}

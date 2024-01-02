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

package ai.datamaker.model.field.type;

import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.PropertyConfig.ValueType;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.FieldGroup;
import ai.datamaker.model.field.FieldType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.search.annotations.Indexed;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Indexed
@Entity
@NoArgsConstructor
@FieldType(description = "Scripting (Uses SpEL expression language)", localizationKey = "field.group.custom.script", group = FieldGroup.CUSTOM)
public class ScriptField extends Field<Object> {

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    public static final PropertyConfig EXPRESSION_PROPERTY =
        new PropertyConfig("field.script.expression",
                           "Script",
                           ValueType.STRING,
                           "T(java.lang.Math).random() * 100.0",
                           Collections.emptyList());

    public static final PropertyConfig EXPRESSION_VARIABLES_NAME =
            new PropertyConfig("field.script.variables.name",
                               "Script variable names",
                               ValueType.LIST,
                               Collections.emptyList(),
                               Collections.emptyList());

    public static final PropertyConfig EXPRESSION_VARIABLES_VALUE =
            new PropertyConfig("field.script.variables.values",
                               "Script variable values",
                               ValueType.LIST,
                               Collections.emptyList(),
                               Collections.emptyList());

    public ScriptField(String name, Locale locale) {
        super(name, locale);
    }

    @Override
    protected Object generateData() {
        final Map<String, Object> variables = Maps.newHashMap();
        if (config.containsKey(EXPRESSION_VARIABLES_NAME.getKey())) {
            List<String> variableNames = (List<String>) config.getConfigProperty(EXPRESSION_VARIABLES_NAME);
            List<String> variableValues = (List<String>) config.getConfigProperty(EXPRESSION_VARIABLES_VALUE);
            Assert.isTrue(variableNames.size() == variableValues.size(), "Number of header names and values should match");
            for (int i=0; i<variableNames.size(); i++) {
                variables.put(variableNames.get(i), parseExpression(variableValues.get(i), Collections.EMPTY_MAP));
            }
        }
        String expression = (String) config.getConfigProperty(EXPRESSION_PROPERTY);

        return parseExpression(expression, variables);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(
            EXPRESSION_PROPERTY,
            EXPRESSION_VARIABLES_NAME,
            EXPRESSION_VARIABLES_VALUE
        );
    }

    private Object parseExpression(String expression, Map<String, Object> variables) {
        if (StringUtils.isBlank(expression)) {
            return expression;
        }

        EvaluationContext evaluationContext = new StandardEvaluationContext();
        variables.forEach(evaluationContext::setVariable);
        //evaluationContext.setVariable("bag", variables);

        Expression exp = EXPRESSION_PARSER.parseExpression(expression);
        //return exp.getValue();
        return exp.getValue(evaluationContext);
    }

}

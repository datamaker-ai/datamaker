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

package ai.datamaker.model;

import ai.datamaker.generator.DataGenerator;
import ai.datamaker.model.job.GenerateDataJob;
import ai.datamaker.model.job.JobExecution;
import ai.datamaker.processor.DatasetProcessor;
import ai.datamaker.sink.DataOutputSink;
import ai.datamaker.service.BeanService;
import ai.datamaker.service.EncryptionService;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.persistence.Transient;
import java.util.HashMap;
import java.util.Locale;

/**
 * Configuration properties for:
 * {@link DataOutputSink},
 * {@link DataGenerator},
 * {@link DatasetProcessor}
 */
public class JobConfig extends HashMap<String, Object> {

    private static final long serialVersionUID = -2358836054943961375L;

    private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    @Getter
    @Setter
    @Transient
    private transient Dataset dataset;

    @Getter
    @Setter
    @Transient
    private transient JobExecution jobExecution;

    @Getter
    @Setter
    @Transient
    private transient GenerateDataJob generateDataJob;

    public static JobConfig EMPTY = new JobConfig();
    static {
        EMPTY.setDataset(new Dataset("empty", Locale.getDefault()));
        EMPTY.setGenerateDataJob(new GenerateDataJob());
        EMPTY.setJobExecution(new JobExecution());
    }

    public String getProperty(String propertyName) {
        return (String) get(propertyName);
    }

    public String getProperty(String propertyName, String defaultValue) {
        return (String) getOrDefault(propertyName, defaultValue);
    }

    public Object parseExpression(String expression) {
        if (StringUtils.isBlank(expression)) {
            return expression;
        }

        EvaluationContext evaluationContext = new StandardEvaluationContext();

        evaluationContext.setVariable("dataset", dataset);
        evaluationContext.setVariable("dataJob", generateDataJob);
        evaluationContext.setVariable("jobExecution", jobExecution);

        Expression exp = EXPRESSION_PARSER.parseExpression(expression);
        //return exp.getValue();
        return exp.getValue(evaluationContext);
    }

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
        return value;
    }

    public void put(PropertyConfig propertyConfig, Object value) {
        put(propertyConfig.getKey(), value);
    }

}

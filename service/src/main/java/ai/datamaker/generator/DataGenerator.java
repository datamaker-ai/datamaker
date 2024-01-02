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

package ai.datamaker.generator;

import ai.datamaker.model.Configurable;
import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.OutputStream;

/**
 * Generate data based on a dataset.
 */
public interface DataGenerator extends Configurable {

    ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    void generate(Dataset dataset, OutputStream outputStream) throws Exception;

    default void generate(Dataset dataset, OutputStream outputStream, JobConfig config) throws Exception {
        generate(dataset, outputStream);
    }

    FormatType getDataType();

    default Object parseExpression(String expression, Dataset dataset) {
        EvaluationContext evaluationContext = new StandardEvaluationContext();

        evaluationContext.setVariable("dataset", dataset);
        // evaluationContext.setVariable("dataJob", config.getGenerateDataJob());
        // evaluationContext.setVariable("jobExecution", config.getJobExecution());

        Expression exp = EXPRESSION_PARSER.parseExpression(expression);
        //return exp.getValue();
        return exp.getValue(evaluationContext);
    }
}

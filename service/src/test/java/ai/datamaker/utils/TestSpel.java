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

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class TestSpel {
    public static void main(String[] args) {

        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();

        Inventor tesla = new Inventor("Nikola Tesla", "Serbian");
        tesla.setPlaceOfBirth(new PlaceOfBirth("Smiljan"));

        context.setVariable("inventor", tesla);

        String city = (String) parser.parseExpression("#inventor.name + T(java.math.Random())").getValue(context);
        System.out.println(city);  // Smiljan

        tesla.setPlaceOfBirth(null);
        city = parser.parseExpression("PlaceOfBirth?.City").getValue(context, tesla, String.class);
        System.out.println(city);  // null - does not throw NullPointerException!!!

//        ExpressionParser parser = new SpelExpressionParser();
//        Expression exp = parser.parseExpression("new java.text.SimpleDateFormat(\"MM-dd-yyyy\").format(new java.util.Date())");
//        String message = (String) exp.getValue();
//        System.out.println(message);


    }
}

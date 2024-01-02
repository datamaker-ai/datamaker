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

import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.field.FieldValue;
import ai.datamaker.model.field.type.DateTimeField;
import ai.datamaker.model.field.type.NullField;
import com.google.common.collect.Lists;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SQL-like insert values query.
 * Options: database engine
 */
public class SqlGenerator implements DataGenerator {

    static final PropertyConfig HIVE_GENERATOR_MANAGED_TABLE =
            new PropertyConfig("sql.generator.managed.table",
                               "Managed table",
                               PropertyConfig.ValueType.BOOLEAN,
                               false,
                               Arrays.asList(true, false));

    static final PropertyConfig SQL_GENERATOR_END_OF_LINE =
            new PropertyConfig("sql.generator.line.ending",
                               "Line ending",
                               PropertyConfig.ValueType.STRING,
                               "\n",
                               Collections.emptyList());

    static final PropertyConfig SQL_DIALECT =
            new PropertyConfig("sql.generator.dialect",
                               "SQL Dialect",
                               PropertyConfig.ValueType.STRING,
                               SqlDialect.SQL_1999.toString(),
                               Arrays.stream(SqlDialect.values()).map(SqlDialect::toString).collect(Collectors.toList()));

    public enum SqlDialect {SQL_1999, SQL_2006, PL_pgSQL, Transact_SQL, PL_SQL, SQL_SERVER, POSTGRES, MYSQL, HIVE}

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(
                HIVE_GENERATOR_MANAGED_TABLE,
                SQL_GENERATOR_END_OF_LINE,
                SQL_DIALECT);
    }

    // TODO evaluate INSERT INTO vs LOAD (can we load remote file easily?)

    @Override
    public void generate(Dataset dataset, OutputStream outputStream, JobConfig config) throws Exception {

        boolean managedTable = (boolean) config.getConfigProperty(HIVE_GENERATOR_MANAGED_TABLE);

        // Create STRUCT

        Table table = new Table(dataset.getName());

        Insert insertStatement = new Insert();
        insertStatement.setTable(table);

        SqlDialect sqlDialect = SqlDialect.valueOf((String) config.getConfigProperty(SQL_DIALECT));
        if (SqlDialect.MYSQL.equals(sqlDialect)) {
            insertStatement.setColumns(dataset.getFields()
                                               .stream()
                                               .map(f -> new Column(table, f.getName()))
                                               .collect(Collectors.toList()));
        }

        MultiExpressionList multiExpressionList = new MultiExpressionList();

        dataset.processAllValues(fv -> {
            if (dataset.getFlushOnEveryRecord()) {
                insertStatement.setItemsList(getExpressionList(fv));
                sendQuery(dataset, outputStream, config, insertStatement);
            } else {
                multiExpressionList.addExpressionList(getExpressionList(fv));
            }
        });

        if (!dataset.getFlushOnEveryRecord()) {
            insertStatement.setItemsList(multiExpressionList);
            sendQuery(dataset, outputStream, config, insertStatement);
        }
    }

    private void sendQuery(Dataset dataset, OutputStream outputStream, JobConfig config, Insert insertStatement) {
        try {
            outputStream.write(insertStatement.toString().getBytes());

            if (config.containsKey(SQL_GENERATOR_END_OF_LINE.getKey())) {
                outputStream.write(config.getConfigProperty(SQL_GENERATOR_END_OF_LINE).toString().getBytes());
            }

            if (dataset.getFlushOnEveryRecord()) {
                outputStream.flush();
            }

        } catch (IOException e) {
            throw new IllegalStateException("Cannot generate sql insert statement", e);
        }
    }

//    private void sendQuery(Insert insertStatement) {
//
//    }

    private ExpressionList getExpressionList(List<FieldValue> fv) {
        ExpressionList expressionList = new ExpressionList();
        //ValueListExpression values = new ValueListExpression();
        expressionList.setExpressions(fv.stream().map(f -> {
            //Column column = new Column(table, f.getField().getName());
            //return column;
            // FIXME handle List and Map
            // List:CSV ???
            // Map: key=value,
            // FIXME other types

            if (f.getField() instanceof NullField) {
                return new NullValue();
            }
            if (f.getField() instanceof DateTimeField) {
                DateTimeField dateTimeField = (DateTimeField)f.getField();
                DateTimeField.DateType type = DateTimeField.DateType.valueOf((String) f.getField().getConfig().getConfigProperty(DateTimeField.DATETIME_TYPE_PROPERTY));

                if (type == DateTimeField.DateType.TIME_ONLY) {
                    TimeValue time = new TimeValue("'00:00:00'");
                    time.setValue((java.sql.Time) java.sql.Time.from(((Date) dateTimeField.getData()).toInstant()));
                } else if (type == DateTimeField.DateType.DATE_ONLY) {
                    DateValue dateValue = new DateValue("'1970-01-01'");
                    dateValue.setValue((java.sql.Date) java.sql.Date.from(((Date) dateTimeField.getData()).toInstant()));
                } else {
                    TimestampValue timestamp = new TimestampValue("'1970-01-01 00:00:00'");
                    timestamp.setValue((java.sql.Timestamp) java.sql.Timestamp.from(((Date) dateTimeField.getData()).toInstant()));
                }
                //A more language-independent choice for string literals is the international standard ISO 8601 format "YYYY-MM-DDThh:mm:ss"
            }
            if (f.getField().getObjectType().equals(Double.class)) {
                return new DoubleValue(f.getValue().toString());
            }
            if (f.getField().getObjectType().equals(Float.class)) {
                return new DoubleValue(f.getValue().toString());
            }
            if (f.getField().getObjectType().equals(Integer.class)) {
                return new LongValue((Integer) f.getValue());
            }
            if (f.getField().getObjectType().equals(Long.class)) {
                return new LongValue((Long) f.getValue());
            }
            if (f.getField().getObjectType().equals(Boolean.class)) {
                return new BooleanValue((Boolean) f.getValue());
            }

            // TODO implement array or struct
/**
 *                     .createStruct("address", new Object[] { "Work",
 *
 *                             "3321 Elm Street", "Suite 374A", "Sudbury", "MA",
 *
 *                             "01976" }); // Create an SQL Struct for an address
 */
            return new StringValue(f.getValue().toString());
        }).collect(Collectors.toList()));
        return expressionList;
    }

    @Override
    public void generate(Dataset dataset, OutputStream outputStream) throws Exception {
        generate(dataset, outputStream, JobConfig.EMPTY);
    }

    @Override
    public FormatType getDataType() {
        return FormatType.SQL;
    }

    public static class BooleanValue extends ASTNodeAccessImpl implements Expression {

        private final Boolean value;

        public BooleanValue(boolean value) {
            this.value = value;
        }

        @Override
        public void accept(ExpressionVisitor expressionVisitor) {

        }

        @Override
        public String toString() {
            return value.toString().toUpperCase();
        }
    }


}

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

package ai.datamaker.processor;

import ai.datamaker.model.Dataset;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.model.SupportedMediaType;
import ai.datamaker.model.field.Field;
import ai.datamaker.model.field.type.DateTimeField;
import ai.datamaker.model.field.type.DoubleField;
import ai.datamaker.model.field.type.IntegerField;
import ai.datamaker.model.field.type.TextField;
import ai.datamaker.model.field.type.TimestampField;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@see https://github.com/JSQLParser/JSqlParser}
 * {@see http://www.sqlparser.com/features/introduce.php?utm_source=github-jsqlparser&utm_medium=text-general}
 */
@Component
@Slf4j
public class SqlProcessor extends DatasetProcessor {

    private static final Pattern EXTRACT_CREATE_STATEMENT = Pattern.compile("(CREATE.*)",
                                                                            Pattern.MULTILINE | Pattern.DOTALL);

    @Override
    public Optional<Dataset> process(InputStream input, JobConfig config) {
        try {
            String sqlStatement = new String(input.readAllBytes(),
                                             StandardCharsets.UTF_8);
            Matcher matcher = EXTRACT_CREATE_STATEMENT.matcher(sqlStatement);
            if (matcher.find()) {

                sqlStatement = matcher.group(1)
                        .replaceAll("(?si)\\s*CLUSTERED BY.*", "")
                        .replaceAll("(?si)\\s*SKEWED BY.*", "")
                        .replaceAll("(?si)\\s*ROW FORMAT.*", "")
                        .replaceAll("(?si)\\s*LOCATION.*", "")
                        .replaceAll("(?si)\\s*TBLPROPERTIES.*", "");

                Statement stmt = CCJSqlParserUtil.parse(sqlStatement);
                String datasetName = (String) config.getConfigProperty(INPUT_FILENAME_PROPERTY);
                Locale locale = getLocale(config);
                Dataset dataset = new Dataset(datasetName, locale);

                stmt.accept(new StatementVisitorAdapter() {
                    @Override
                    public void visit(CreateTable createTable) {
                        dataset.setName(createTable.getTable().getName());
                        createTable.getColumnDefinitions().forEach(cf -> {
                            Field field;
                            switch (cf.getColDataType().getDataType().toUpperCase()) {
                                case "CHAR":
                                case "VARCHAR":
                                case "VARCHAR2":
                                case "STRING":
                                    // TODO detect on comments as well
                                    field = fieldDetectorService.detectTypeOnName(cf.getColumnName(), locale)
                                        .orElse(new TextField(cf.getColumnName(), locale));
                                    break;
                                case "DATE":
                                case "TIME":
                                case "DATETIME":
                                    // TODO format date or time
                                    field = new DateTimeField(cf.getColumnName(), locale);
                                    break;
                                case "DECIMAL":
                                case "REAL":
                                    field = new DoubleField(cf.getColumnName(), locale);
                                    break;
                                case "NUMBER":
                                case "INT":
                                case "SMALLINT":
                                case "INTEGER":
                                case "BIGINT":
                                case "BIGINTEGER":
                                    field = new IntegerField(cf.getColumnName(), locale);
                                    break;
                                case "TIMESTAMP":
                                    field = new TimestampField(cf.getColumnName(), locale);
                                    break;
                                default:
                                    field = new TextField(cf.getColumnName(), locale);

                            }
                            dataset.addField(field);
                        });
                    }
                });
                return Optional.of(dataset);
            }
        } catch (Exception e) {
            log.error("Cannot parse input statement", e);
            throw new IllegalStateException("Cannot parse input statement", e);
        }

        return Optional.empty();
    }

    @Override
    public Set<SupportedMediaType> supportedTypes() {
        return Sets.newHashSet(SupportedMediaType.SQL);
    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(INPUT_FILENAME_PROPERTY,
                                  LOCALE_PROPERTY);
    }
}

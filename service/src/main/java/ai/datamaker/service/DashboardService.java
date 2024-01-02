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

package ai.datamaker.service;

import ai.datamaker.model.response.DashboardResponse;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@CacheConfig
@Service
public class DashboardService {

    private static final String MYSQL_JOB_EXECUTIONS_QUERY = "select sum(total) as total, timestampdiff(week, timestampadd(month, -6, current_timestamp()), start_time) + 1 as weeknumber"
            + " from (select start_time, count(*) as total from job_execution group by start_time) a"
            + " where start_time > timestampadd(month, -6, current_timestamp())"
            + " group by weeknumber";

    private static final String H2_JOB_EXECUTIONS_QUERY = "select sum(total) as total, datediff('week', dateadd('month', -6, current_timestamp()), start_time) + 1 as weeknumber"
            + " from (select start_time, count(*) as total from job_execution group by start_time)"
            + " where start_time > dateadd('month', -6, current_timestamp())"
            + " group by weeknumber";

    private static final String MYSQL_FAILED_JOB_EXECUTIONS_QUERY = "select sum(total) as total, timestampdiff(week, timestampadd(month, -6, current_timestamp()), start_time) + 1 as weeknumber"
            + " from (select start_time, count(*) as total from job_execution where state = 'FAILED' group by start_time) a"
            + " where start_time > timestampadd(month, -6, current_timestamp())"
            + " group by weeknumber";

    private static final String H2_FAILED_JOB_EXECUTIONS_QUERY = "select sum(total) as total, datediff('week', dateadd('month', -6, current_timestamp()), start_time) + 1 as weeknumber"
            + " from (select start_time, count(*) as total from job_execution where state = 'FAILED' group by start_time)"
            + " where start_time > dateadd('month', -6, current_timestamp())"
            + " group by weeknumber";

    private static final String MYSQL_RECORDS_GENERATED_QUERY = "select sum(total) as total, timestampdiff(week, timestampadd(month, -6, current_timestamp()), start_time) + 1 as weeknumber"
            + " from (select start_time, sum(number_of_records) as total from job_execution group by start_time) a"
            + " where start_time > timestampadd(month, -6, current_timestamp())"
            + " group by weeknumber";

    private static final String H2_RECORDS_GENERATED_QUERY = "select sum(total) as total, datediff('week', dateadd('month', -6, current_timestamp()), start_time) + 1 as weeknumber"
            + " from (select start_time, sum(number_of_records) as total from job_execution group by start_time)"
            + " where start_time > dateadd('month', -6, current_timestamp())"
            + " group by weeknumber";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.driverClassName}")
    private String driver;

    @Cacheable("dashboard")
    public DashboardResponse dashboardResponse() {

        List<Long> jobExecutionsPerWeeks = Lists.newArrayList();
        List<Long> failedJobExecutionsPerWeeks = Lists.newArrayList();
        List<Long> recordsGeneratedPerWeeks = Lists.newArrayList();
        if (driver.startsWith("org.h2.Driver")) {
            jdbcTemplate.query(H2_JOB_EXECUTIONS_QUERY, rs -> {
                jobExecutionsPerWeeks.add(rs.getLong("total"));
            });
            jdbcTemplate.query(H2_FAILED_JOB_EXECUTIONS_QUERY, rs -> {
                failedJobExecutionsPerWeeks.add(rs.getLong("total"));
            });
            jdbcTemplate.query(H2_RECORDS_GENERATED_QUERY, rs -> {
                recordsGeneratedPerWeeks.add(rs.getLong("total"));
            });
        } else {
            jdbcTemplate.query(MYSQL_JOB_EXECUTIONS_QUERY, rs -> {
                jobExecutionsPerWeeks.add(rs.getLong("total"));
            });
            jdbcTemplate.query(MYSQL_FAILED_JOB_EXECUTIONS_QUERY, rs -> {
                failedJobExecutionsPerWeeks.add(rs.getLong("total"));
            });
            jdbcTemplate.query(MYSQL_RECORDS_GENERATED_QUERY, rs -> {
                recordsGeneratedPerWeeks.add(rs.getLong("total"));
            });
        }

        Map<String, Long> sinksPerType = Maps.newHashMap();
        jdbcTemplate.query("select sink_names, count(*) as Total from generate_data_job join generate_data_job_sink_names on generate_data_job.id = generate_data_job_sink_names.generate_data_job_id group by sink_names",
                           rs -> {
                                String sinkName = rs.getString("sink_names");
                                if (StringUtils.isNotBlank(sinkName)) {
                                    sinksPerType.put(sinkName.replaceAll(".*\\.", ""), rs.getLong("Total"));
                                }
                           });
        Map<String, Long> generatorsPerType = Maps.newHashMap();
        jdbcTemplate.query("select generator_name, count(*) as Total from generate_data_job group by generator_name",
                           rs -> {
                               String generatorName = rs.getString("generator_name");
                               if (StringUtils.isNotBlank(generatorName)) {
                                   generatorsPerType.put(generatorName.replaceAll(".*\\.", ""), rs.getLong("Total"));
                               }
                           });
        Map<String, Long> fieldsPerType = Maps.newHashMap();
        jdbcTemplate.query("select dtype, count(*) as Total from field group by dtype",
                           rs -> {
                               fieldsPerType.put(rs.getString("dtype").replaceAll(".*\\.", ""), rs.getLong("Total"));
                           });

        return DashboardResponse
                .builder()
                .totalRecordsGenerated(jdbcTemplate.queryForObject("select coalesce(sum(number_of_records), 0) as Total from job_execution", Long.class))
                .activeJobExecutions(jdbcTemplate.queryForObject("select count(*) from job_execution where state = 'RUNNING'", Long.class))
                .pendingJobExecutions(jdbcTemplate.queryForObject("select count(*) from generate_data_job where schedule != 'once'", Long.class))
                .failedJobExecutions(jdbcTemplate.queryForObject("select count(*) from job_execution where state = 'FAILED'", Long.class))
                .failedJobExecutionsPerWeeks(failedJobExecutionsPerWeeks.toArray(Long[]::new))
                .jobExecutionsPerWeeks(jobExecutionsPerWeeks.toArray(Long[]::new))
                .recordsGeneratedPerWeeks(recordsGeneratedPerWeeks.toArray(Long[]::new))
                .totalWorkspaces(jdbcTemplate.queryForObject("select count(*) from workspace", Long.class))
                .totalUsers(jdbcTemplate.queryForObject("select count(*) from user", Long.class))
                .totalGroups(jdbcTemplate.queryForObject("select count(*) from user_groups", Long.class))
                .totalConfiguredDataJobs(jdbcTemplate.queryForObject("select count(*) from generate_data_job", Long.class))
                .totalGenerators(jdbcTemplate.queryForObject("select count(generator_name) from generate_data_job", Long.class))
                .totalGeneratorsPerType(generatorsPerType)
                .totalSinksPerType(sinksPerType)
                .totalSinks(jdbcTemplate.queryForObject("select count(*) as Total from generate_data_job_sink_names", Long.class))
                .totalFields(jdbcTemplate.queryForObject("select count(*) from field", Long.class))
                .totalFieldMappings(jdbcTemplate.queryForObject("select count(*) from field_mapping", Long.class))
                .totalFieldsPerType(fieldsPerType)
                .totalDatasets(jdbcTemplate.queryForObject("select count(*) from dataset", Long.class))
                .totalJobExecutions(jdbcTemplate.queryForObject("select count(*) from job_execution", Long.class))
                .build();
    }

}

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

import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum SupportedMediaType {

    AVRO(Sets.newHashSet("application/avro-binary", "avro/binary"), Sets.newHashSet("avro")),
    BINARY(Sets.newHashSet("application/octet-stream"), Sets.newHashSet("bin")),
    CSV(Sets.newHashSet("text/csv"), Sets.newHashSet("csv")),
    EXCEL(Sets.newHashSet(
            "application/x-msexcel",
            "application/vnd.ms-excel",
            "application/excel",
            "application/x-excel"), Sets.newHashSet("xls", "xlsx")),
    JSON(Sets.newHashSet("application/json"), Sets.newHashSet("json", "js")),
    JSON_SCHEMA(Sets.newHashSet("application/schema+json", "application/schema+json"), Sets.newHashSet("json", "schema.json")),
    PARQUET(Sets.newHashSet("application/octet-stream", "application/parquet"), Sets.newHashSet("parquet")),
    ORC(Sets.newHashSet("application/octet-stream", "application/orc"), Sets.newHashSet("orc")),
    TEXT(Sets.newHashSet(
            "text/plain",
            "application/plain"), Sets.newHashSet("txt", "text")),
    TSV(Sets.newHashSet("text/tsv"), Sets.newHashSet("tsv")),
    SQL(Sets.newHashSet("application/sql"), Sets.newHashSet("sql", "ddl", "hql")),
    XML(Sets.newHashSet(
            "application/xml",
            "text/xml"), Sets.newHashSet("xml")),
    XML_SCHEMA(Sets.newHashSet(
            "application/xsd",
        "application/xml",
        "text/xml"), Sets.newHashSet("xsd"));

    private Set<String> mediaTypes;

    private Set<String> fileExtensions;

    SupportedMediaType(Set<String> mediaTypes, Set<String> fileExtensions) {
        this.mediaTypes = mediaTypes;
        this.fileExtensions = fileExtensions;
    }

    /**
     *
     * @param mimeType
     * @return or null if no match found
     */
    public static SupportedMediaType from(String mimeType, String fileExtension) {

        List<SupportedMediaType> foundAny = Stream.of(SupportedMediaType.values())
                .filter(s -> s.mediaTypes.contains(mimeType))
                .collect(Collectors.toList());

        if (foundAny.size() != 1)
        {
            foundAny = Stream.of(SupportedMediaType.values())
                    .filter(s -> s.fileExtensions.contains(fileExtension))
                    .collect(Collectors.toList());
        }

        return foundAny.isEmpty() ? null : foundAny.get(0);
    }
}

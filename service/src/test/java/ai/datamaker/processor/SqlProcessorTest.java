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
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SqlProcessorTest extends AbstractDatasetProcessorTest {

    private final static String EXAMPLE = "CREATE EXTERNAL TABLE IF NOT EXISTS sic_brute.tfopfrr( NUM_INSN STRING COMMENT 'Numero institution', NUM_CAIS STRING COMMENT 'Numero transit', NUM_PRFL STRING COMMENT 'Numero profil', NUM_FOLI STRING COMMENT 'Numero folio', IND_MBRE_CONT_FOPF STRING COMMENT 'Indicateur membre contact folio profil', COD_NATU_LIEN_FOPF STRING COMMENT 'Code nature lien folio profil', NUM_SEQU_ADPC STRING COMMENT 'Numero sequence adresse profil membre caisse', DAT_DEBU_ROLE STRING COMMENT 'Date debut role'\n" +
            " )\n" +
            " PARTITIONED BY (dat_integ STRING)\n" +
            " ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'\n" +
            " WITH SERDEPROPERTIES (\n" +
            "    \"separatorChar\" = \",\",\n" +
            "    \"quoteChar\"     = \"\\\"\"\n" +
            " )  \n" +
            " STORED AS TEXTFILE \n" +
            " LOCATION '/lac/brute/sic/tfopfrr'\n" +
            " tblproperties \n" +
            "     (    \"skip.header.line.count\"=\"1\",    \"serialization.encoding\"=\"UTF-8\"\n" +
            "  );";

    protected SqlProcessorTest() {
        super(new SqlProcessor());
    }

    @Test
    void process() {
        Optional<Dataset> optionalDataset = datasetProcessor.process(new ByteArrayInputStream(EXAMPLE.getBytes(StandardCharsets.UTF_8)));

        assertTrue(optionalDataset.isPresent());
        assertThat(optionalDataset.get().getFields())
                .hasSize(8)
                .extracting("name")
                .containsExactly("NUM_INSN", "NUM_CAIS", "NUM_PRFL", "NUM_FOLI", "IND_MBRE_CONT_FOPF", "COD_NATU_LIEN_FOPF", "NUM_SEQU_ADPC", "DAT_DEBU_ROLE");
    }
}
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

package ai.datamaker.sink.azure;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.azure.AzureDatalakeOutputSink;
import com.azure.storage.file.datalake.DataLakeFileClient;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AzureDatalakeOutputSinkTest {

    private final AzureDatalakeOutputSink sink = Mockito.spy(AzureDatalakeOutputSink.class);

    @Test
    void accept() {
        Arrays.stream(FormatType.values()).forEach(ft -> assertTrue(sink.accept(ft)));
    }

    @Test
    void getOutputStream() throws Exception {
        DataLakeFileClient client = Mockito.mock(DataLakeFileClient.class);
        doReturn(client).when(sink).getClient(any(JobConfig.class));

        JobConfig config = new JobConfig();
        sink.getConfigProperties().forEach(cp -> {
                                               if (cp.getType() == PropertyConfig.ValueType.STRING) {
                                                   config.put(cp,
                                                              "abc");
                                               }
                                               if (cp.getType() == PropertyConfig.ValueType.LIST) {
                                                   config.put(cp,
                                                              Lists.newArrayList("'abc'"));
                                               }
                                           }
        );
        config.put(AzureDatalakeOutputSink.AZURE_FILENAME, "'datalake'");
        config.put(AzureDatalakeOutputSink.AZURE_STORAGE_ACCOUNT_KEY, "key");
        config.remove(AzureDatalakeOutputSink.AZURE_STORAGE_SAS_TOKEN.getKey());

        try (OutputStream output = sink.getOutputStream(config)) {
            output.write("hello".getBytes(StandardCharsets.UTF_8));
        }

        verify(client, times(1)).append(any(InputStream.class), eq(0L), eq(5L));
        verify(client, times(1)).flush(eq(5L));
    }

    @Test
    void getOutputStream_flushSize() throws Exception {
        DataLakeFileClient client = Mockito.mock(DataLakeFileClient.class);
        doReturn(client).when(sink).getClient(any(JobConfig.class));

        JobConfig config = new JobConfig();
        sink.getConfigProperties().forEach(cp -> {
                                               if (cp.getType() == PropertyConfig.ValueType.STRING) {
                                                   config.put(cp,
                                                              "abc");
                                               }
                                               if (cp.getType() == PropertyConfig.ValueType.LIST) {
                                                   config.put(cp,
                                                              Lists.newArrayList("'abc'"));
                                               }
                                           }
        );
        config.put(AzureDatalakeOutputSink.AZURE_FILENAME, "'datalake'");
        config.put(AzureDatalakeOutputSink.AZURE_STORAGE_BUFFER, 2);
        config.put(AzureDatalakeOutputSink.AZURE_STORAGE_ACCOUNT_KEY, "key");
        config.remove(AzureDatalakeOutputSink.AZURE_STORAGE_SAS_TOKEN.getKey());

        try (OutputStream output = sink.getOutputStream(config)) {
            output.write("he".getBytes(StandardCharsets.UTF_8));
            output.write("llo".getBytes(StandardCharsets.UTF_8));
            output.write("hell0".getBytes(StandardCharsets.UTF_8));
        }

        verify(client, times(1)).append(any(InputStream.class), eq(0L), eq(5L));
        verify(client, times(1)).flush(eq(5L));
        verify(client, times(1)).append(any(InputStream.class), eq(5L), eq(5L));
        verify(client, times(1)).flush(eq(10L));
    }

    @Test
    void getConfigProperties() {
        assertEquals(9, sink.getConfigProperties().size());
    }
}
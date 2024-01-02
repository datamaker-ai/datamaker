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

package ai.datamaker.e2e.sink.base;

import ai.datamaker.model.JobConfig;
import ai.datamaker.model.job.JobExecution;
import ai.datamaker.sink.base.FileOutputSink;
import ai.datamaker.sink.filter.CompressFilter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.OutputStream;

class FileOutputSinkTest {
    private FileOutputSink sink = new FileOutputSink();

    @Test
    void accept() {
    }

    @Test
    void getOutputStream() throws Exception {
        JobConfig config = new JobConfig();
        JobExecution jobExecution = new JobExecution();
        config.setJobExecution(jobExecution);
        config.put(CompressFilter.COMPRESSION_FORMAT.getKey(), "BZIP2");
        File tempFile = File.createTempFile("test", ".bz2");
        tempFile.deleteOnExit();

        config.put(FileOutputSink.FILE_OUTPUT_PATH_PROPERTY.getKey(), "'" + tempFile.getPath() + "'");

        try (OutputStream outputStream = sink.getOutputStream(config)) {
            outputStream.write("hello, world".getBytes());
        }
    }

    @Test
    void getConfigProperties() {
    }
}
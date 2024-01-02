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

package ai.datamaker.sink;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.Configurable;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.field.SimpleFieldValue;
import ai.datamaker.sink.filter.CompressFilter;
import ai.datamaker.sink.filter.EncryptFilter;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.List;

public interface DataOutputSink extends Configurable {

    ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    boolean accept(FormatType type);

    default boolean flushable() {
        return true;
    }

    default OutputStream getOutputStream() throws Exception {
        return getOutputStream(new JobConfig());
    }

    OutputStream getOutputStream(JobConfig config) throws Exception;

    default Object parseExpression(String expression, JobConfig config) {
        EvaluationContext evaluationContext = new StandardEvaluationContext();

        evaluationContext.setVariable("dataset", config.getDataset());
        evaluationContext.setVariable("dataJob", config.getGenerateDataJob());
        evaluationContext.setVariable("jobExecution", config.getJobExecution());

        Expression exp = EXPRESSION_PARSER.parseExpression(expression);
        //return exp.getValue();
        return exp.getValue(evaluationContext);
    }

    default List<List<SimpleFieldValue>> getRecords(InputStream inputStream) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(inputStream)) {
            return (List<List<SimpleFieldValue>>) in.readObject();
        }
    }

    default List<SimpleFieldValue> getRecord(InputStream inputStream) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(inputStream)) {
            return (List<SimpleFieldValue>) in.readObject();
        }
    }

    default OutputStream encryptCompressStream(JobConfig config, OutputStream outputStream) throws Exception {
        String compressionFormat = (String) config.getConfigProperty(CompressFilter.COMPRESSION_FORMAT);
        String encryptionAlgorithm = (String) config.getConfigProperty(EncryptFilter.ENCRYPTION_ALGORITHM);

        if ("NONE".equals(compressionFormat) && "NONE".equals(encryptionAlgorithm)) {
            return outputStream;
        } else if ("NONE".equals(compressionFormat)) {
            return EncryptFilter.encryptStream(config, outputStream);
        } else if ("NONE".equals(encryptionAlgorithm)) {
            return CompressFilter.getCompressedStream(config, outputStream);
        }

//        OutputStream encryptedStream = EncryptFilter.encryptStream(config,
//                                                                   outputStream);
//        return CompressFilter.getCompressedStream(config,
//                                                  encryptedStream);
//        return EncryptFilter.encryptStream(config, CompressFilter.getCompressedStream(config, outputStream));

        OutputStream compressedStream = CompressFilter.getCompressedStream(config, outputStream);

        return new OutputStream() {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final OutputStream stream = EncryptFilter.encryptStream(config, baos);

            @Override
            public void write(int b) throws IOException {
                stream.write(b);
            }

            @Override
            public void write(byte[] b) throws IOException {
                stream.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                stream.write(b, off, len);
            }

            @Override
            public void flush() throws IOException {
                stream.flush();
            }

            @Override
            public void close() throws IOException {
                stream.flush();
                stream.close();
                compressedStream.write(baos.toByteArray());
                compressedStream.close();
            }
        };
    }
}

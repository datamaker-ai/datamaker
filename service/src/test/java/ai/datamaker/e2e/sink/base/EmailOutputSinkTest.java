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
import ai.datamaker.sink.base.EmailOutputSink;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;

@Disabled
class EmailOutputSinkTest {

    private EmailOutputSink sink = new EmailOutputSink();

    @Test
    void accept() {
    }

    @Test
    void getOutputStream() throws Exception {
        //  Outgoing server 	 smtp.videotron.ca 	 587 	 yes
        JobConfig config = new JobConfig();
        config.put(EmailOutputSink.EMAIL_SMTP_HOST, "smtp.videotron.ca");
        config.put(EmailOutputSink.EMAIL_SMTP_PORT, 587);
        config.put(EmailOutputSink.EMAIL_FROM_ADDRESS, "MATHIEU.PELLETIER9940@VIDEOTRON.CA");
        config.put(EmailOutputSink.EMAIL_TO_ADDRESS, "matt.pelletier@videotron.ca");
        config.put(EmailOutputSink.EMAIL_SUBJECT, "Datamaker");
        config.put(EmailOutputSink.EMAIL_AUTH_USERNAME, "VLGOGFAS");
        config.put(EmailOutputSink.EMAIL_AUTH_PASSWORD, "");
        try (OutputStream outputStream = sink.getOutputStream(config)) {
            outputStream.write("Hello world".getBytes());
        }
    }

    @Test
    void getOutputStream_html() throws Exception {
        //  Outgoing server 	 smtp.videotron.ca 	 587 	 yes
        JobConfig config = new JobConfig();
        config.put(EmailOutputSink.EMAIL_SMTP_HOST, "smtp.videotron.ca");
        config.put(EmailOutputSink.EMAIL_SMTP_PORT, 587);
        config.put(EmailOutputSink.EMAIL_FROM_ADDRESS, "MATHIEU.PELLETIER9940@VIDEOTRON.CA");
        config.put(EmailOutputSink.EMAIL_TO_ADDRESS, "matt.pelletier@videotron.ca");
        config.put(EmailOutputSink.EMAIL_SUBJECT, "Datamaker");
        config.put(EmailOutputSink.EMAIL_DATA_OUTPUT, "HTML");
        config.put(EmailOutputSink.EMAIL_AUTH_USERNAME, "VLGOGFAS");
        config.put(EmailOutputSink.EMAIL_AUTH_PASSWORD, "");
        try (OutputStream outputStream = sink.getOutputStream(config)) {
            outputStream.write("Hello world<br/> <h1>This is actual message</h1>".getBytes());
        }
    }

    @Test
    void getOutputStream_attachment() throws Exception {
        //  Outgoing server 	 smtp.videotron.ca 	 587 	 yes
        JobConfig config = new JobConfig();
        config.put(EmailOutputSink.EMAIL_SMTP_HOST, "smtp.videotron.ca");
        config.put(EmailOutputSink.EMAIL_SMTP_PORT, 587);
        config.put(EmailOutputSink.EMAIL_FROM_ADDRESS, "MATHIEU.PELLETIER9940@VIDEOTRON.CA");
        config.put(EmailOutputSink.EMAIL_TO_ADDRESS, "matt.pelletier@videotron.ca");
        config.put(EmailOutputSink.EMAIL_SUBJECT, "Datamaker");
        config.put(EmailOutputSink.EMAIL_DATA_OUTPUT, "ATTACHMENT");
        config.put(EmailOutputSink.EMAIL_FILE_ATTACHMENT_NAME, "\"file.txt\"");
        config.put(EmailOutputSink.EMAIL_AUTH_USERNAME, "VLGOGFAS");
        config.put(EmailOutputSink.EMAIL_AUTH_PASSWORD, "");
        try (OutputStream outputStream = sink.getOutputStream(config)) {
            outputStream.write("Hello world".getBytes());
        }
    }

    @Test
    void getOutputStream_tls() throws Exception {
        //  Outgoing server 	 smtp.videotron.ca 	 587 	 yes
        JobConfig config = new JobConfig();
        config.put(EmailOutputSink.EMAIL_SMTP_HOST, "smtp.videotron.ca");
        config.put(EmailOutputSink.EMAIL_SMTP_PORT, 587);
        config.put(EmailOutputSink.EMAIL_FROM_ADDRESS, "MATHIEU.PELLETIER9940@VIDEOTRON.CA");
        config.put(EmailOutputSink.EMAIL_TO_ADDRESS, "matt.pelletier@videotron.ca");
        config.put(EmailOutputSink.EMAIL_SUBJECT, "Datamaker");
        config.put(EmailOutputSink.EMAIL_SECURED, "TLS");
        config.put(EmailOutputSink.EMAIL_AUTH_USERNAME, "VLGOGFAS");
        config.put(EmailOutputSink.EMAIL_AUTH_PASSWORD, "joq3plwb");
        try (OutputStream outputStream = sink.getOutputStream(config)) {
            outputStream.write("Hello world from TLS test".getBytes());
        }
    }

    @Test
    void getOutputStream_ssl() throws Exception {
        //  Outgoing server 	 smtp.videotron.ca 	 465 	 yes (SSL/TLS)
        JobConfig config = new JobConfig();
        config.put(EmailOutputSink.EMAIL_SMTP_HOST, "smtp.videotron.ca");
        config.put(EmailOutputSink.EMAIL_SMTP_PORT, 465);
        config.put(EmailOutputSink.EMAIL_FROM_ADDRESS, "MATHIEU.PELLETIER9940@VIDEOTRON.CA");
        config.put(EmailOutputSink.EMAIL_TO_ADDRESS, "matt.pelletier@videotron.ca");
        config.put(EmailOutputSink.EMAIL_SUBJECT, "Datamaker");
        config.put(EmailOutputSink.EMAIL_SECURED, "SSL");
        config.put(EmailOutputSink.EMAIL_AUTH_USERNAME, "VLGOGFAS");
        config.put(EmailOutputSink.EMAIL_AUTH_PASSWORD, "joq3plwb");
        try (OutputStream outputStream = sink.getOutputStream(config)) {
            outputStream.write("Hello world from SSL test".getBytes());
        }
    }

    @Test
    void getConfigProperties() {
    }
}
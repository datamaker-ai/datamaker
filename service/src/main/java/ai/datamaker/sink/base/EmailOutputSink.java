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

package ai.datamaker.sink.base;

import ai.datamaker.generator.FormatType;
import ai.datamaker.model.DataOutputSinkType;
import ai.datamaker.model.JobConfig;
import ai.datamaker.model.PropertyConfig;
import ai.datamaker.sink.DataOutputSink;
import ai.datamaker.utils.FormatTypeUtils;
import ai.datamaker.utils.stream.SendDataOutputStream;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Slf4j
@DataOutputSinkType(compressed = true, encrypted = true)
public class EmailOutputSink implements DataOutputSink {

    public static final PropertyConfig EMAIL_DATA_OUTPUT =
            new PropertyConfig("email.sink.data.output",
                               "Data output format",
                               PropertyConfig.ValueType.STRING,
                               "PLAIN_TEXT",
                               Lists.newArrayList("ATTACHMENT", "PLAIN_TEXT", "HTML"));

    public static final PropertyConfig EMAIL_TO_ADDRESS =
            new PropertyConfig("email.sink.to.address",
                               "Recipient address (To)",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig EMAIL_FROM_ADDRESS =
            new PropertyConfig("email.sink.from.address",
                               "Sender address (From)",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig EMAIL_SMTP_HOST =
            new PropertyConfig("email.sink.smtp.host",
                               "SMTP host",
                               PropertyConfig.ValueType.STRING,
                               "localhost",
                               Collections.emptyList());

    public static final PropertyConfig EMAIL_SMTP_PORT =
            new PropertyConfig("email.sink.smtp.port",
                               "SMTP port",
                               PropertyConfig.ValueType.NUMERIC,
                               25,
                               Collections.emptyList());

    public static final PropertyConfig EMAIL_SUBJECT =
            new PropertyConfig("email.sink.subject",
                               "Subject line",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig EMAIL_FILE_ATTACHMENT_NAME =
            new PropertyConfig("email.sink.attachment.name",
                               "File attachment name",
                               PropertyConfig.ValueType.EXPRESSION,
                               "#dataset.name + \"-\" + T(java.lang.System).currentTimeMillis() + \".\" + #dataJob.generator.dataType.name().toLowerCase()",
                               Collections.emptyList());

    public static final PropertyConfig EMAIL_MESSAGE_PART =
            new PropertyConfig("email.sink.message.part",
                               "Message part in case of attachment",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig EMAIL_SECURED =
            new PropertyConfig("email.sink.secured.protocol",
                               "Secured authentication",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Lists.newArrayList("", "SSL", "TLS"));

    public static final PropertyConfig EMAIL_AUTH_USERNAME =
            new PropertyConfig("email.sink.auth.username",
                               "Username",
                               PropertyConfig.ValueType.STRING,
                               "",
                               Collections.emptyList());

    public static final PropertyConfig EMAIL_AUTH_PASSWORD =
            new PropertyConfig("email.sink.auth.password",
                               "Password",
                               PropertyConfig.ValueType.PASSWORD,
                               "",
                               Collections.emptyList());

    @Override
    public boolean accept(FormatType type) {
        return true;
    }

    @Override
    public OutputStream getOutputStream(JobConfig config) throws Exception {
        String to = (String) config.getConfigProperty(EMAIL_TO_ADDRESS);
        String from = (String) config.getConfigProperty(EMAIL_FROM_ADDRESS);
        String host = (String) config.getConfigProperty(EMAIL_SMTP_HOST);
        int port = (int) config.getConfigProperty(EMAIL_SMTP_PORT);

        //Properties properties = System.getProperties();
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", host);
        properties.put("mail.smtp.port", String.valueOf(port));

        Session session = getSession(properties, config);
        final MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject((String) config.getConfigProperty(EMAIL_SUBJECT));

        return new SendDataOutputStream(bytes -> {
            try {

                switch ((String) config.getConfigProperty(EMAIL_DATA_OUTPUT)) {
                    case "ATTACHMENT":

                        String fileName = (String) config.getConfigProperty(EMAIL_FILE_ATTACHMENT_NAME);
                        // Create the message part
                        BodyPart messageBodyPart = new MimeBodyPart();

                        // Fill the message
                        if (config.containsKey(EMAIL_MESSAGE_PART.getKey())) {
                            messageBodyPart.setText((String) config.getConfigProperty(EMAIL_MESSAGE_PART));
                        } else {
                            messageBodyPart.setText("Attachment: " + fileName);
                        }

                        // Create a multipart message
                        Multipart multipart = new MimeMultipart();

                        // Set text message part
                        multipart.addBodyPart(messageBodyPart);

                        // Part two is attachment
                        messageBodyPart = new MimeBodyPart();
                        //DataSource source = new FileDataSource(filename);
                        String type = "text/plain";
                        if (config.getGenerateDataJob() != null) {
                            type = FormatTypeUtils.getContentType(config.getGenerateDataJob().getGenerator().getDataType());
                        }
                        DataSource source = new ByteArrayDataSource(bytes, type);
                        messageBodyPart.setDataHandler(new DataHandler(source));
                        messageBodyPart.setFileName(fileName);
                        multipart.addBodyPart(messageBodyPart);

                        // Send the complete message parts
                        message.setContent(multipart);
                        break;

                    case "PLAIN_TEXT":
                        message.setText(new String(bytes));
                        break;
                    case "HTML":
                        message.setContent(new String(bytes), "text/html");
                        break;
                    default:
                }

                Transport.send(message);
                log.debug("Message sent successfully....");
            } catch (MessagingException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    @VisibleForTesting
    Session getSession(Properties properties, JobConfig config) {
        String username = (String) config.getConfigProperty(EMAIL_AUTH_USERNAME);
        String password = (String) config.getConfigProperty(EMAIL_AUTH_PASSWORD);
        String secured = (String) config.getConfigProperty(EMAIL_SECURED);

        if ("TLS".equals(secured)) {
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", "true");
        } else if ("SSL".equals(secured)) {
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } else {
            if (StringUtils.isNotBlank(username)) {
                properties.setProperty("mail.user", username);
                properties.setProperty("mail.password", password);
            }
            return Session.getDefaultInstance(properties);
        }

        return Session.getInstance(properties, new javax.mail.Authenticator() {

            protected PasswordAuthentication getPasswordAuthentication() {

                return new PasswordAuthentication(username, password);
            }
        });

    }

    @Override
    public List<PropertyConfig> getConfigProperties() {
        return Lists.newArrayList(EMAIL_DATA_OUTPUT,
                                  EMAIL_FROM_ADDRESS,
                                  EMAIL_TO_ADDRESS,
                                  EMAIL_SMTP_HOST,
                                  EMAIL_SMTP_PORT,
                                  EMAIL_SUBJECT,
                                  EMAIL_MESSAGE_PART,
                                  EMAIL_FILE_ATTACHMENT_NAME,
                                  EMAIL_SECURED,
                                  EMAIL_AUTH_USERNAME,
                                  EMAIL_AUTH_PASSWORD);
    }

}

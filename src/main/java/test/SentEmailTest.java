package test;

import configurator.TestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import utils.EmailUtilities;
import utils.PropertiesReader;

import java.io.File;
import java.util.List;

import static utils.PropertiesReader.*;

public class SentEmailTest extends TestConfig {
    private static final Logger logger = LoggerFactory.getLogger(SentEmailTest.class);
   EmailUtilities emailUtilities = new EmailUtilities();
    @Test
    public void sendEmailWithAttachedFiles() {
        PropertiesReader propertiesReader = new PropertiesReader(testData);
        logger.info("Starting email sending test...");

        List<String> toEmails = getEmailsFromProperty("toEmails", getEmail(propertiesReader.getToEmails()));
        List<String> ccEmails = getEmailsFromProperty("ccEmails", getEmail(propertiesReader.getCcEmails()));

        String subject = propertiesReader.getEmailSubject();

        List<File> attachments =emailUtilities.listFilesInFolder(String.valueOf(emailUtilities.getLatestFolder("environmentalFiles")));
//      if (propertiesReader.equalsIgnoreCase("TRUE")) {
//          attachments.add(new File("2DImageLinks.txt"));
//      }
        String msgBody = propertiesReader.getEmailContent();
        if ( msgBody.startsWith("\"") && msgBody.endsWith("\"")) {
            msgBody = msgBody.substring(1, msgBody.length() - 1);
        }
        emailUtilities.sendEmailWithAttachments(toEmails, ccEmails, subject, msgBody, attachments);
        logger.info("Email sent successfully.");
    }


    private List<String> getEmailsFromProperty(String propertyName, List<String> defaultEmails) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isEmpty()) {
            return List.of(propertyValue.split(","));
        }
        return defaultEmails;
    }

    private List<String> getEmail(String emails) {
        if (emails != null && !emails.isEmpty()) {
            if (emails.startsWith("\"") && emails.endsWith("\"")) {
                emails = emails.substring(1, emails.length() - 1);
            }
            return List.of(emails.split(","));
        }
        return List.of();
    }
}

package utils;

import configurator.BaseClass;
import org.apache.commons.mail.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import static utils.PropertiesReader.*;

public class EmailUtilities {

    private static final Logger logger = LoggerFactory.getLogger(EmailUtilities.class);
    private static final Properties prop = new Properties();
    PropertiesReader propertiesReader = new PropertiesReader(BaseClass.testData);
    static {
        try {
            FileInputStream files = new FileInputStream("configurations/userConfigurations.properties");
            prop.load(files);
            logger.info("Loaded Gmail configuration: {}", prop.getProperty("gmail"));
        } catch (IOException e) {
            logger.error("Failed to load configuration file: {}", e.getMessage());
        }
    }

    public  void sendEmailWithAttachments(List<String> toEmails, List<String> ccEmails, String subject, String messageBody, List<File> attachments) {
        try {
            System.out.println(toEmails);
            System.out.println("cc : "+ ccEmails);
            logger.info("Preparing to send email with multiple attachments.");

            MultiPartEmail email = new MultiPartEmail();
            email.setHostName("smtp.gmail.com");
            email.setSmtpPort(465);
            email.setAuthentication(propertiesReader.getToEmails(), propertiesReader.getCcEmails());
            email.setSSLOnConnect(true);
            email.setFrom(propertiesReader.getSenderEmail());

            for (String toEmail : toEmails) {
                email.addTo(toEmail);
            }
            for (String ccEmail : ccEmails) {
                email.addCc(ccEmail);
            }

            email.setSubject(subject);
            email.setMsg(messageBody);

            for (File attachmentPath : attachments) {
                EmailAttachment attachment = new EmailAttachment();
                attachment.setPath(attachmentPath.getAbsolutePath());
                System.out.println("path : "+ attachmentPath.getAbsolutePath());
                attachment.setDisposition(EmailAttachment.ATTACHMENT);
                attachment.setDescription("File attachment");
                attachment.setName(attachmentPath.getName());
                email.attach(attachment);
            }

            logger.info("Sending email with multiple attachments...");
            email.send();
            logger.info("Email sent successfully with multiple attachments!");
        } catch (EmailException e) {
            logger.error("Error sending email with multiple attachments: {}", e.getMessage());
        }
    }

    public  File getLatestFolder(String directoryPath) {
        File[] folders = new File(directoryPath).listFiles(File::isDirectory);
        if (folders == null || folders.length == 0) {
            logger.warn("No folders found in directory: {}", directoryPath);
            return null;
        }

        File latestFolder = folders[0];
        for (File folder : folders) {
            if (folder.lastModified() > latestFolder.lastModified()) {
                latestFolder = folder;
            }
        }

        logger.info("Latest folder found: {}", latestFolder.getName());
        return latestFolder;
    }

    public  List<File> listFilesInFolder(String folderPath) {
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();
        List<File> fileList = new ArrayList<>();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    fileList.add(file);
                }
            }
        } else {
            logger.warn("The folder is empty or does not exist: {}", folderPath);
        }
        return fileList;
    }
}
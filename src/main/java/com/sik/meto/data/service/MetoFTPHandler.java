package com.sik.meto.data.service;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.util.Properties;

public class MetoFTPHandler {
    private static final Logger LOG = LoggerFactory.getLogger(MetoFTPHandler.class);
    private static final String METO_EXTREMES_FILENAME = "MetOfficeExtremes.xlsx";
    private static final String METO_HISTORIC_FILENAME = "MetOfficeHistoricData.xlsx";
    private static final String METO_YRLYAVGS_FILENAME = "MetOfficeYearlyAverages.xlsx";

    FTPClient ftpClient = new FTPClient();

    @Value("${neunelfer.ftp.url}")
    private String ftpHost;
    @Value("${neunelfer.ftp.port}")
    private int ftpPort;
    @Value("${neunelfer.ftp.username}")
    private String ftpUser;
    @Value("${neunelfer.ftp.secret}")
    private String ftpPass;
    @Value("${neunelfer.ftp.localdir}")
    private String ftpLdir;

    public MetoFTPHandler() {
//        Properties properties = new Properties();
//        try {
//            properties.load(new FileInputStream("/Users/sik/met-office/ftp/ftp.properties"));
//        } catch (IOException e) {
//            throw new IllegalStateException(e);
//        }
//        this.ftpHost = properties.getProperty("neunelfer.ftp.url");
//        this.ftpPort = Integer.parseInt(properties.getProperty("neunelfer.ftp.port"));
//        this.ftpUser = properties.getProperty("neunelfer.ftp.username");
//        this.ftpPass = properties.getProperty("neunelfer.ftp.secret");
//        this.ftpLdir = properties.getProperty("neunelfer.ftp.localdir");
    }

    public void uploadFiles() {

        try {
            LOG.info("Connecting to: {}:{} with user: {}",ftpHost, ftpPort, ftpUser);
            ftpClient.connect(ftpHost, ftpPort);
            ftpClient.login(ftpUser, ftpPass);
            ftpClient.enterLocalPassiveMode();

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            this.uploadFile(METO_HISTORIC_FILENAME);
            this.uploadFile(METO_YRLYAVGS_FILENAME);
            this.uploadFile(METO_EXTREMES_FILENAME);

        } catch (IOException ex) {
            LOG.error("Upload Error: " + ex.getMessage());
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                LOG.error("Error: " + ex.getMessage());
            }
        }
    }

    private void uploadFile(String fileName) {
        InputStream isHistoric = null;
        try {
            isHistoric = new FileInputStream(ftpLdir + fileName);
            boolean uploadOk = ftpClient.storeFile(fileName, isHistoric);
            isHistoric.close();
            if (uploadOk) {
                LOG.info("{} uploaded successfully.", fileName);
            }
        } catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}

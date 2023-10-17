package com.sik.meto.data.service;


import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

public class FTPSUploader {

    private static final String FILE_PATH = "/Users/sik/met-office/";
    private static final Logger LOG = LoggerFactory.getLogger(FTPSUploader.class);
    private Properties ftpProps;
    private String ftpUrl;
    private String ftpUser;
    private String ftpSecret;
    private Integer ftpPort;
    private String ftpLocalDir;
    private String ftpRemoteDir;

    private FTPSClient ftpsClient;

    public FTPSUploader() {
        this.ftpProps = this.getFtpProps();
        this.ftpUser = ftpProps.get("neunelfer.ftp.username").toString();
        this.ftpSecret = ftpProps.get("neunelfer.ftp.secret").toString();
        this.ftpUrl = ftpProps.get("neunelfer.ftp.url").toString();
        this.ftpPort = Integer.parseInt(ftpProps.get("neunelfer.ftp.port").toString());
        this.ftpLocalDir = ftpProps.get("neunelfer.ftp.localdir").toString();
        this.ftpRemoteDir = ftpProps.get("neunelfer.ftp.remotedir").toString();
        this.ftpsClient = this.initialiseClient();
    }

    public boolean upload(String fileName)
    {
        try
        {
            int reply;

            ftpsClient.connect(ftpUrl, ftpPort);
            LOG.info("Connected to: {}", ftpUrl);
            LOG.info(ftpsClient.getReplyString());

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftpsClient.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftpsClient.disconnect();
                LOG.error("FTP server refused connection.");
                return false;
            }
            ftpsClient.login(ftpUser, ftpSecret);
            // ... // transfer files
            ftpsClient.setBufferSize(1000);
            ftpsClient.enterLocalPassiveMode();
            // ftp.setControlEncoding("GB2312");
            ftpsClient.changeWorkingDirectory(ftpRemoteDir);
            // ftpsClient.changeWorkingDirectory("/ae"); //path where my files are
            ftpsClient.setFileType(FTP.ASCII_FILE_TYPE);
            LOG.info("Remote system is " + ftpsClient.getSystemName());

            ftpsClient.remoteStore(ftpLocalDir + fileName);
            reply = ftpsClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply))
            {
                LOG.error("FTPS remote store failed with code: {}", reply);
                ftpsClient.logout();
                ftpsClient.disconnect();
                return false;
            }

            String[] tmp = ftpsClient.listNames();  //returns null
            LOG.info(String.valueOf(tmp.length));
        }
        catch (IOException ex)
        {
            LOG.error("Oops! Exception caught: {}", ex.getMessage());
            ex.printStackTrace();
            return false;
        }
        finally
        {
            // logs out and disconnects from server
            try
            {
                if (ftpsClient.isConnected())
                {
                    ftpsClient.logout();
                    ftpsClient.disconnect();
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        return true;
    }

    private FTPSClient initialiseClient() {
        FTPSClient ftpsClient = new FTPSClient("FTP");
        //ftpsClient.setAuthValue("SSL");
        ftpsClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        return ftpsClient;
    }

    private Properties getFtpProps() {
        try (
                InputStream propsFile = new FileInputStream(FILE_PATH + "ftp/ftp.properties")) {
            Properties ftpProps = new Properties();
            ftpProps.load(propsFile);
            return ftpProps;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}

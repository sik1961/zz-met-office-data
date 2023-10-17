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

public class SecureUpload {

    private static final String FILE_PATH = "/Users/sik/met-office/";
    private static final Logger LOG = LoggerFactory.getLogger(SecureUpload.class);
    private Properties ftpProps;
    private String ftpUrl;
    private String ftpUser;
    private String ftpSecret;
    private Integer ftpPort;
    private String ftpLocalDir;
    private String ftpRemoteDir;

    public SecureUpload() {
        this.ftpProps = this.getFtpProps();
        this.ftpUser = ftpProps.get("neunelfer.ftp.username").toString();
        this.ftpSecret = ftpProps.get("neunelfer.ftp.secret").toString();
        this.ftpUrl = ftpProps.get("neunelfer.ftp.url").toString();
        this.ftpPort = Integer.parseInt(ftpProps.get("neunelfer.ftp.port").toString());
        this.ftpLocalDir = ftpProps.get("neunelfer.ftp.localdir").toString();
        this.ftpRemoteDir = ftpProps.get("neunelfer.ftp.remotedir").toString();
    }
    // perform multiple file upload
    public boolean upload(String fileName)
    {
        String server = ftpUrl;
        int port = 2121;
        String user = "joao";
        String pass = "1234";

        boolean error = false;
        FTPSClient ftp = null;
        try
        {
            ftp = new FTPSClient("SSL");
            ftp.setAuthValue("SSL");
            ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));

            int reply;

            ftp.connect(ftpUrl, ftpPort);
            LOG.info("Connected to: {}", ftpUrl);
            LOG.info(ftp.getReplyString());

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftp.disconnect();
                System.err.println("FTP server refused connection.");
                System.exit(1);
            }
            ftp.login(ftpUser, ftpSecret);
            // ... // transfer files
            ftp.setBufferSize(1000);
            ftp.enterLocalPassiveMode();
            // ftp.setControlEncoding("GB2312");
            ftp.changeWorkingDirectory("/");
            ftp.changeWorkingDirectory("/ae"); //path where my files are
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            System.out.println("Remote system is " + ftp.getSystemName());

            String[] tmp = ftp.listNames();  //returns null
            System.out.println(tmp.length);
        }
        catch (IOException ex)
        {
            System.out.println("Oops! Something wrong happened");
            ex.printStackTrace();
        }
        finally
        {
            // logs out and disconnects from server
            try
            {
                if (ftp.isConnected())
                {
                    ftp.logout();
                    ftp.disconnect();
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
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

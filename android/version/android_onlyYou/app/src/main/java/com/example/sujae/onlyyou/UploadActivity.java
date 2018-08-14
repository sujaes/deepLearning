package com.example.sujae.onlyyou;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;

public class UploadActivity {

    private FTPClient mFTP ;

    private FileInputStream fis ;


    private File uploadFile;
//step 1 : 생성자 선언

    public UploadActivity(String ftpURL,int port) throws IOException {

        mFTP = new FTPClient();

        mFTP.connect(ftpURL,port);

    }

//step 2 : FTP server 로그인

    public void login(String LoginID, String Password) throws IOException{

        mFTP.login(LoginID, Password);

    }

//step 3 : FTP server setting

    public boolean settingFTP( int timeout, String ControlEncoding, String changeDir ) throws IOException{

        mFTP.setSoTimeout(timeout);

        mFTP.setControlEncoding(ControlEncoding);

        boolean changeok = mFTP.changeWorkingDirectory(changeDir);

        mFTP.setFileType(FTP.BINARY_FILE_TYPE);

        mFTP.enterLocalPassiveMode();

        return changeok;

    }

//step 4 : File upload to FTP server

    public boolean FileUploadFtp(String uploadFileUri) throws IOException{

        uploadFile = new File(uploadFileUri);

        fis = new FileInputStream(uploadFile);

        boolean isSuccess = mFTP.storeFile(uploadFile.getName(), fis);


        return isSuccess;

    }

//step 5 : disconnect the Socket

    public void closedSocket() throws IOException{

        if(fis != null){

            fis.close();

        }

        mFTP.logout();

        if(mFTP != null && mFTP.isConnected()){

            mFTP.disconnect();

        }

    }

}


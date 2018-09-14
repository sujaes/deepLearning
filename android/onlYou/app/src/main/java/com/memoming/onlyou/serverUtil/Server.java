package com.memoming.onlyou.serverUtil;

import android.os.Handler;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class Server {
    private ChannelSftp sftpChannel;
    private Channel     channel;
    private Session     session;

    public Server() {
        try { connect(); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void connect() throws Exception {
        try {
            ServerInfo sInfo = new ServerInfo();
            String host = sInfo.getHost();
            String user = sInfo.getUser();
            String password = sInfo.getPassword();
            Integer port = sInfo.getPort();

            System.out.println("connecting..." + host);
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, port);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect();
            channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;
            System.out.println("Server connected.");
        }
        catch (Exception e){ e.printStackTrace(); }
    }

    private void disconnect() {
        if (session.isConnected()) {
            System.out.println("disconnecting...");
            sftpChannel.disconnect();
            channel.disconnect();
            session.disconnect();
            System.out.println("Server disconnected.");
        }
    }

    public void upload(String fileName, String remoteDir) throws Exception {
        try {
            FileInputStream fileInputStream = null;

            sftpChannel.cd(remoteDir);
            File file;
            while (true) {
                file = new File(fileName);
                if (file.getTotalSpace() > 0) {
                    fileInputStream = new FileInputStream(file);
                    sftpChannel.put(fileInputStream, file.getName());
                    fileInputStream.close();
                    System.out.println("File uploaded successfully - " + file.getAbsolutePath());
                    break;
                }
            }
        }

        catch (Exception e) { e.printStackTrace(); }

        //결과처리
        if (sftpChannel.getExitStatus() == -1) {
            System.out.println("file uploaded");
            Log.v("upload result", "succeeded");
        }
        else { Log.v("upload faild ", "faild"); }
        disconnect();
    }
    public void download(String fileName, String remoteDir, String localDir) throws Exception {


        //빈버퍼 생성
        byte[] buffer = new byte[1024];
        //버퍼스트림생성
        BufferedInputStream bis = null;
        //서버연결
        connect();
        //경로로 이동
        sftpChannel.cd(remoteDir);

        while (true) {

            try {
                bis             = new BufferedInputStream(sftpChannel.get(fileName));
                File newFile    = new File(localDir +"/"+ fileName);

                // 파일다운로드
                OutputStream os             = new FileOutputStream(newFile);
                BufferedOutputStream bos    = new BufferedOutputStream(os);
                int readCount;
                while ((readCount = bis.read(buffer)) > 0)
                    bos.write(buffer, 0, readCount);
                bis.close();
                bos.close();
                System.out.println("File downloaded successfully - ");
                disconnect();
                break;
            }
            catch (Exception e) { e.printStackTrace(); }
        }
    }

    public ChannelSftp getSftpChannel() { return sftpChannel; }
//    public void download(String fileName, String remoteDir, String localDir) throws Exception {
//        //빈버퍼 생성
//        byte[] buffer = new byte[1024];
//        //버퍼스트림생성
//        BufferedInputStream bis = null;
//        //서버연결
//        connect();
//        sftpChannel.cd(remoteDir);
//
//        while (true) {
//            try {
//                File newFile    = new File(localDir +"/"+ fileName);
//                System.out.println("fileName : "+fileName);
//
//                bis             = new BufferedInputStream(sftpChannel.get(fileName));
//                OutputStream os             = new FileOutputStream(newFile);
//                BufferedOutputStream bos    = new BufferedOutputStream(os);
//                int readCount;
//                while ((readCount = bis.read(buffer)) > 0) {
//                    bos.write(buffer, 0, readCount);
//                }
//
//                bis.close();
//                bos.close();
//
//                System.out.println("File downloaded successfully - ");
//
//                break;
//            }
//            catch (Exception e) { e.printStackTrace(); }
//            finally {
//                disconnect();
//            }
//        }
//
//    }
//
//    public ChannelSftp getSftpChannel() { return sftpChannel; }
}

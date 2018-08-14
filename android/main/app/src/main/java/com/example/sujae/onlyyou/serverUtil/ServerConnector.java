package com.example.sujae.onlyyou.serverUtil;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class ServerConnector {
    private ChannelSftp sftpChannel;
    private Channel     channel;
    private Session     session;

    public ServerConnector() {

    }

    public void connect() throws JSchException, SftpException {
        ServerInfo  sInfo       = new ServerInfo();
        String      host        = sInfo.getHost();
        String      user        = sInfo.getUser();
        String      password    = sInfo.getPassword();
        Integer     port        = sInfo.getPort();

        System.out.println("connecting..." + host);
        JSch jsch       = new JSch();
        session         = jsch.getSession(user, host, port);
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(password);
        session.connect();
        channel         = session.openChannel("sftp");
        channel.connect();
        sftpChannel     = (ChannelSftp) channel;
        System.out.println("Server connected.");
    }

    public void disconnect() {
        if (session.isConnected()) {
            System.out.println("disconnecting...");
            sftpChannel.disconnect();
            channel.disconnect();
            session.disconnect();
            System.out.println("Server disconnected.");
        }
    }

    public ChannelSftp getSftpChannel() { return sftpChannel; }
}

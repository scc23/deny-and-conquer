package com.asap.dnc.network;

import com.asap.dnc.core.GameMessage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ServerUdpUnicast extends Thread {
    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private GameMessage msg;

    public ServerUdpUnicast(InetAddress address, int port, GameMessage msg) throws SocketException {
        this.socket = new DatagramSocket();
        this.address = address;
        this.port = port;
        this.msg = msg;
    }


    private void sendMessage() throws IOException {
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bStream);
        oos.writeObject(msg);
        oos.flush();
        byte[] buf = bStream.toByteArray();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address, port);
        System.out.println("Server Sending unicast message to " + this.address + " on port "+ this.port);
        socket.send(packet);
    }

    @Override
    public void run() {
        try{
            sendMessage();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}

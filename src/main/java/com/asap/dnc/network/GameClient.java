package com.asap.dnc.network;
import com.asap.dnc.core.CoreGameClientImpl;
import com.asap.dnc.core.GameMessage;
import com.asap.dnc.core.PenColor;

import java.net.*;
import java.io.*;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;
import java.util.Random;

class GameClient extends Thread{
    private DatagramSocket socket;
    private InetAddress address;
    private CoreGameClientImpl core;

    private GameClient() throws UnknownHostException, SocketException {
        socket = new DatagramSocket();
        address = InetAddress.getLocalHost();
        core = new CoreGameClientImpl();
    }

    private void SendMessage() throws Exception {
        int row, col, n = 0;
        Random random = new Random();
        MessageType type = MessageType.CELL_ACQUIRE;
        int gridSize = 3;
        PenColor color = PenColor.BLUE;
        while (n <= 20) {
            System.out.println(n);
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            // Randomly select a cell to acquire
            row = 1;
            col = 1;


            System.out.println(timestamp.getTime());
            GameMessage msg = new GameMessage(type, timestamp);
            msg.setRow(row);
            msg.setCol(col);
            msg.setPenColor(color);
//            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
//            ObjectOutputStream oos = new ObjectOutputStream(bStream);
//            oos.writeObject(msg);
//            oos.flush();
//            byte[] buf = bStream.toByteArray();
//            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 5000);
//            socket.send(packet);

            // Convert InetAddress to ip address string for testing, we will need to get the address as a string from ClientInfo later
            core.sendServerRequest(address.getHostAddress(), 5000, msg);
//            core.receiveServerResponse();
            //packet = new DatagramPacket(buf, buf.length);
            //socket.receive(packet);
            // String received = new String(
            //   packet.getData(), 0, packet.getLength());
            // System.out.println(received+"\n");
            n++;
            TimeUnit.SECONDS.sleep(1);

        }
    }

    public void getMessage() throws Exception{
        core.receiveServerResponse();
    }

    public static void main(String[] args) throws Exception{
        GameClient client1 = new GameClient();
        //GameClient client2 = new GameClient();
        Thread sendMsg = new Thread(){
            @Override
            public void run(){
                System.out.println("SendMessage thread started...");
                try{
                    client1.SendMessage();
                } catch (Exception e){

                }
            }
        };
        Thread receiveMsg = new Thread(){
            @Override
            public void run(){
                System.out.println("receiveMessage thread started...");
                try{
                    client1.getMessage();
                    Thread.sleep(100);
                } catch (Exception e){

                }
            }
        };

        sendMsg.start();
        receiveMsg.start();
    }
}

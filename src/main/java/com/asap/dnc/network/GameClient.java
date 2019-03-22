package com.asap.dnc.network;
import com.asap.dnc.core.CoreGameClientImpl;
import com.asap.dnc.core.GameMessage;
import com.asap.dnc.core.PenColor;
import com.asap.dnc.network.gameconfig.client.ClientGrid;

import java.net.*;
import java.sql.Timestamp;
import java.util.concurrent.TimeUnit;
import java.util.Random;

class GameClient extends Thread{
    private String address;
    private CoreGameClientImpl core;

    private GameClient() throws UnknownHostException, SocketException {
        ClientGrid grid = new ClientGrid(10, 3, 3);
        address = "127.0.0.1";
        core = new CoreGameClientImpl(grid);
    }

    private void SendMessage() throws Exception {
        MessageType type;
        int row, col, n = 0;
        Random random = new Random();
        int gridSize = 3;
        PenColor color = PenColor.BLUE;
        while (n <= 20) {
            System.out.println(n);

            type = MessageType.CELL_ACQUIRE;

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            // Randomly select a cell to acquire
            row = random.nextInt(gridSize);
            col = random.nextInt(gridSize);


            System.out.println(timestamp.getTime());
            GameMessage msg = new GameMessage(type, timestamp);
            msg.setPenColor(color);
            msg.setRow(row);
            msg.setCol(col);


//            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
//            ObjectOutputStream oos = new ObjectOutputStream(bStream);
//            oos.writeObject(msg);
//            oos.flush();
//            byte[] buf = bStream.toByteArray();
//            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 5000);
//            socket.send(packet);

            // Convert InetAddress to ip address string for testing, we will need to get the address as a string from ClientInfo later
            core.sendServerRequest(address, 5000, msg);

            // Hold the cell for a few seconds
            Thread.sleep(3000);

            // Send release message to server
            type = MessageType.CELL_RELEASE;
            GameMessage releaseMsg = new GameMessage(type, timestamp);
            releaseMsg.setPenColor(color);
            releaseMsg.setIsOwned();
            core.sendServerRequest(address, 5000, releaseMsg);

            n++;
            TimeUnit.SECONDS.sleep(1);

        }
    }

    public void getMessage() throws Exception{
        core.receiveServerResponse();
    }

    public void getMessageMulticast() throws Exception{
        core.recieveMulticast();
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
                    while (true){
                        client1.getMessage();
                        Thread.sleep(10);
                    }

                } catch (Exception e){

                }
            }
        };

        Thread receiveMsgMulticast = new Thread(){
            @Override
            public void run(){
                System.out.println("Multicast thread started...");
                try{
                    while(true){
                        client1.getMessageMulticast();
                        Thread.sleep(10);
                    }
                } catch (Exception e){

                }
            }
        };

        receiveMsgMulticast.start();
        sendMsg.start();
        receiveMsg.start();
    }
}

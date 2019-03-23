package com.asap.dnc.network;
import com.asap.dnc.core.CoreGameClient;
import com.asap.dnc.core.PenColor;
import com.asap.dnc.network.gameconfig.client.ClientGrid;

import java.util.concurrent.TimeUnit;
import java.util.Random;

class GameClient extends Thread{
    private String address;
    private CoreGameClient core;

    private GameClient() {
        ClientGrid grid = new ClientGrid(5);
        address = "127.0.0.1";
        core = new CoreGameClient(grid);
    }

    private void SendMessage() throws Exception {
        int row, col;
        Random random = new Random();
        int gridSize = 3;
        PenColor color = PenColor.BLUE;
        for (int i = 0; i < 20; i++) {
            System.out.println("i = " + i);

            // Randomly select a cell to acquire
            row = random.nextInt(gridSize);
            col = random.nextInt(gridSize);

            core.sendAcquireMessage(address, 5000, color, row, col);

            // Hold the cell for a few seconds
            Thread.sleep(1000);

            // Send release message to server
            core.sendReleaseMessage(address, 5000, color, row, col, 80);

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

        receiveMsg.start();
        receiveMsgMulticast.start();
        sendMsg.start();
    }
}

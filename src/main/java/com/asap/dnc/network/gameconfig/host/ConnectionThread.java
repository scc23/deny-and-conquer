package com.asap.dnc.network.gameconfig.host;

import com.asap.dnc.network.ClientInfo;
import com.asap.dnc.core.PenColor;
import com.asap.dnc.network.gameconfig.client.ClientConfigMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Semaphore;

class ConnectionThread extends Thread {

    private static long TIME_TO_LIVE = 600000;
    private static ClientInfo[] clientInformation;
    private static PenColor[] penColors;
    private static int nTargetConnections;
    private static int nConnections;
    private static long serverSystemTime;
    private static Semaphore writeLock = new Semaphore(1);

    private Socket clientConnection;

    public static void init(ClientInfo[] _clientInformation, PenColor[] _penColors) throws Exception {
        if (_clientInformation.length > _penColors.length) {
            throw new Exception("Number of expected connections cannot exceed the number of pen colors");
        }

        clientInformation = _clientInformation;
        penColors = _penColors;
        nTargetConnections = _clientInformation.length;
        nConnections = 0;
        serverSystemTime = System.currentTimeMillis();
    }

    public static ClientInfo[] getClientInformation() {
        return clientInformation;
    }

    public ConnectionThread(Socket clientConnection) {
        this.clientConnection = clientConnection;
    }

    public void run() {
        try (ObjectInputStream is = new ObjectInputStream(this.clientConnection.getInputStream());
             ObjectOutputStream os = new ObjectOutputStream(this.clientConnection.getOutputStream())
        ) {
            String clientAddress = this.clientConnection.getInetAddress().getHostAddress();
            int clientPort = this.clientConnection.getPort();

            ClientConfigMessage clientMsg;
            PenColor clientPenColor = null;
            try {
                clientMsg = (ClientConfigMessage) is.readObject();
                try {
                    ClientInfo clientInfo = new ClientInfo(clientAddress, clientPort);
                    clientInfo.isHost(clientMsg.isHost());
                    writeLock.acquire();

                    clientPenColor = clientMsg.getPenColor();
                    if (clientPenColor == null) {
                        clientPenColor = penColors[nConnections];
                    }
                    clientInfo.setPenColor(clientPenColor);
                    clientInformation[nConnections] = clientInfo;

                    writeLock.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (ClassNotFoundException e) {
                System.out.println("Failed to parse client message");
                System.exit(-1);
            }

            try {
                writeLock.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            nConnections++;
            writeLock.release();

            int nConnectionsRemaining = nTargetConnections - nConnections;
            os.writeInt(nConnectionsRemaining);
            os.flush();

            while (nConnections != nTargetConnections) {
                try {
                    sleep(1000);
                    if (nTargetConnections - nConnections != nConnectionsRemaining) {
                        nConnectionsRemaining = nTargetConnections - nConnections;
                        os.writeInt(nConnectionsRemaining);
                        os.flush();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();;
                }
            }

            os.writeObject(clientInformation);
            os.writeObject(clientPenColor);
            os.writeLong(serverSystemTime);
            os.flush();

            try {
                Thread.sleep(TIME_TO_LIVE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            clientConnection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }
}

package com.asap.dnc.network.gameconfig.host;

import com.asap.dnc.gameconfig.GameConfig;
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
    private static GameConfig config;
    private static long serverTime = System.currentTimeMillis();
    private static int nConnections;
    private static Semaphore writeLock = new Semaphore(1);

    private Socket clientConnection;

    public static void init(ClientInfo[] _clientInformation, PenColor[] _penColors, GameConfig _config) throws Exception {
        if (_clientInformation.length > _penColors.length) {
            throw new Exception("Number of expected connections cannot exceed the number of pen colors");
        }

        clientInformation = _clientInformation;
        penColors = _penColors;
        config = _config;
        nConnections = 0;

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

            int nTargetConnections = config.getNumberPlayers();
            try {
                writeLock.acquire();
                nConnections++;
                int nConnectionsRemaining = nTargetConnections - nConnections;
                os.writeInt(nConnectionsRemaining);
                os.flush();
                writeLock.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int _nConnections = nConnections;
            while (nConnections < config.getNumberPlayers()) {
                try {
                    sleep(1000);
                    if (_nConnections != nConnections) {
                        for (int i = _nConnections + 1; i <= nConnections; i++) {
                            os.writeInt(config.getNumberPlayers() - i);
                            os.flush();
                        }
                        _nConnections = nConnections;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();;
                }
            }
            if (_nConnections != nConnections) {
                for (int i = _nConnections + 1; i <= nConnections; i++) {
                    os.writeInt(nTargetConnections - i);
                    os.flush();
                }
            } else if (nTargetConnections != config.getNumberPlayers()) {
                for (int i = _nConnections + 1; i <= nTargetConnections; i++) {
                    os.writeInt(nTargetConnections - i);
                    os.flush();
                }
                ClientInfo[] newClientInformation = new ClientInfo[_nConnections];
                int i = 0;
                for (ClientInfo ci : clientInformation) {
                    if (ci != null) {
                        newClientInformation[i++] = ci;
                    }
                }
                clientInformation = newClientInformation;
            }

            os.writeObject(config);
            os.writeObject(clientInformation);
            os.writeObject(clientPenColor);
            os.flush();

            // wait for client to send ready message before sending time
            is.readInt();
            os.writeLong(serverTime);
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

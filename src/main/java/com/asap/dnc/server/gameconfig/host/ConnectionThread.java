package com.asap.dnc.server.gameconfig.host;

import com.asap.dnc.config.ClientInfo;
import com.asap.dnc.config.PenColor;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Semaphore;

class ConnectionThread extends Thread {

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
        try (InputStream is = this.clientConnection.getInputStream();
             ObjectOutputStream os = new ObjectOutputStream(this.clientConnection.getOutputStream())
        ) {
            String clientAddress = this.clientConnection.getInetAddress().getHostAddress();
            int clientPort = this.clientConnection.getPort();
            boolean isHost = is.read() == 1 ? true : false;

            try {
                ClientInfo clientInfo = new ClientInfo(clientAddress, clientPort);
                writeLock.acquire();

                PenColor penColor = penColors[nConnections];
                clientInfo.setPenColor(penColor);
                clientInformation[nConnections] = clientInfo;

                writeLock.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            nConnections++;
            while (nConnections != nTargetConnections) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();;
                }
            }

            os.writeObject(clientInformation);
            os.writeLong(serverSystemTime);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }
}

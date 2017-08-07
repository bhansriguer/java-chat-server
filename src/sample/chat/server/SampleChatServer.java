/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sample.chat.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author bhans
 */
public class SampleChatServer {

    private static final int PORT = 6969;

    private ServerSocket mServerSocket;
    private ArrayList<Client> clientList;

    private BufferedWriter writer = null;
    private BufferedReader reader = null;

    public SampleChatServer() {
        // TODO code application logic here
        outputLog("Initialized");
    }

    public void start() {
        try {
            mServerSocket = new ServerSocket(PORT);
            clientList = new ArrayList<>();
            System.out.println("Server UP! Listening to port " + PORT);
            Socket client = null;
            while ((client = mServerSocket.accept()) != null) {
                if (client.isConnected()) {
                    reader = new BufferedReader(
                            new InputStreamReader(client.getInputStream()));
                    writer = new BufferedWriter(
                            new OutputStreamWriter(client.getOutputStream()));
                    Client mClient = new Client();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        if (line.toLowerCase().contains("login||")) {
                            System.out.println("Client login: " + line);
                            String login = line.split("\\|\\|")[1];
                            mClient.setLogin(login);
                            mClient.setConnection(client);
                            break;
                        }
                    }
                    if (addClient(mClient)) {
                        outputLog("Client connected: " + mClient.getLogin());
                        outputLog("Connected clients: " + clientList.size());
                        broadcastMessage("Server", "connected: " + mClient.getLogin());
                        // send current user with logged in clients
                        String clients = "";
                        for (Client c : clientList) {
                            clients += c.getLogin() + "|";
                        }
                        clients = clients.substring(0, clients.length() - 1);
                        writer.write("server: userlist: " + clients + "\r\n");
                        writer.flush();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    initializeClient(mClient);
                                } catch (IOException ex) {
                                    outputLog("Error: " + ex.getMessage());
                                }
                            }
                        }).start();
                    }
                }
            }
        } catch (IOException ex) {
            outputLog("Error: " + ex.getMessage());
        }
    }

    private boolean addClient(Client mClient) {
        return clientList.add(mClient);
    }

    private void outputLog(String message) {
        System.out.println(message);
    }

    private void initializeClient(Client mClient) throws IOException {
        BufferedWriter bw;
        BufferedReader br;
        try (Socket client = mClient.getConnection()) {
            bw = new BufferedWriter(
                    new OutputStreamWriter(client.getOutputStream()));
            br = new BufferedReader(
                    new InputStreamReader(client.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                outputLog("From " + mClient.getLogin() + ": " + line);
                if (line.toLowerCase().startsWith("to::")) {
                    String[] msg = line.split("\\:\\:");
                    outputLog("This messages is for: " + msg[1]);
                    outputLog("Message: " + msg[2]);
                    if (!sendPrivateMessage(mClient.getLogin(), msg[1], msg[2])) {
                        bw.write("Server: user " + msg[1] + " is not found.\r\n");
                    }
                } else if (line.toLowerCase().startsWith("pm:")) {
                    bw.write(line);
                } else if (!line.toLowerCase().startsWith("to::")) {
                    broadcastMessage(mClient.getLogin(), line);
                } else {
                    bw.write(mClient.getLogin() + ": " + line + "\r\n");
                    bw.flush();
                }
            }
        }
        clientList.remove(mClient);
        broadcastMessage("Server", "disconnected: " + mClient.getLogin());
        outputLog("Client disconnected: " + mClient.getLogin());
        outputLog("Current connected clients: " + clientList.size());
    }

    private boolean broadcastMessage(String from, String message) {
        BufferedWriter broadcastWriter;
        boolean isBroadcasted = false;
        for (Client client : clientList) {
            if (!client.getLogin().equals(from)) {
                try {
                    broadcastWriter = new BufferedWriter(
                            new OutputStreamWriter(client.getConnection().getOutputStream()));
                    broadcastWriter.write(from + ": " + message + "\r\n");
                    broadcastWriter.flush();
                } catch (IOException ex) {
                    outputLog("Error: " + ex.getMessage());
                    isBroadcasted = false;
                    break;
                }
            }
        }
        return isBroadcasted;
    }

    private boolean sendPrivateMessage(String from, String to, String message) {
        BufferedWriter privateMessageWriter;
        boolean isUserFound = false;
        for (Client client : clientList) {
            if (client.getLogin().equals(to)) {
                try {
                    privateMessageWriter = new BufferedWriter(
                            new OutputStreamWriter(client.getConnection().getOutputStream()));
                    privateMessageWriter.write("PM:" + from + ": " + message + "\r\n");
                    privateMessageWriter.flush();
                } catch (IOException ex) {
                    outputLog("Error: " + ex.getMessage());
                }
                isUserFound = true;
                break;
            }
        }
        return isUserFound;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        new Runnable() {
            @Override
            public void run() {
                SampleChatServer scs = new SampleChatServer();
                scs.start();
            }
        }.run();
    }

}

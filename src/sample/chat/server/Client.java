/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sample.chat.server;

import java.net.Socket;

/**
 *
 * @author bhans
 */
public class Client {

    private String login;
    private Socket connection;

    public Client() {
        login = "";
        connection = new Socket();
    }

    public String getLogin() {
        return login;
    }

    public Socket getConnection() {
        return connection;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setConnection(Socket connection) {
        this.connection = connection;
    }

}

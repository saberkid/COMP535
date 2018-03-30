package socs.network.service;

import socs.network.node.Link;
import socs.network.node.Router;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 *
 */
public class Server implements Runnable{
    private ServerSocket serverSocket;
    private Thread threading;
    private Router router;



    private ClientHandler[] clientHandlers;

    public Server(Router router) {
        this.router = router;
        threading = new Thread(this);
        clientHandlers = new ClientHandler[4];
    }
    public Thread getThreading() {
        return threading;
    }

    public ClientHandler[] getClientHandlers() {
        return clientHandlers;
    }
    public void run() {
        try {
            serverSocket = new ServerSocket(router.getRd().getProcessPortNumber());
        } catch (Exception e) {
            e.printStackTrace();
        }

        while(true) {
            try {
                int freePortNumber = getFreePortNumber();
                if (freePortNumber != -1) {
                    System.out.println("Waiting for client on port " + freePortNumber);
                    Socket server = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(server, this.router);
                    clientHandlers[freePortNumber] = clientHandler;
                    router.getPorts()[freePortNumber] = new Link(); // occupy the port first
                    clientHandler.getThreading().start();

                }
                else{
                    //System.out.println("All ports are occupied");
                }

            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private int getFreePortNumber(){
        int port = -1;
        for (int i = 0; i < router.getPorts().length; ++i) {
            if (router.getPorts()[i] == null) {
                port = i;
                break;
            }
        }
        return port;
    }
}

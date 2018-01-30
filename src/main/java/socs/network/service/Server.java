package socs.network.service;

import socs.network.node.Router;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

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
                    System.out.println("Waiting for client on port " +
                            serverSocket.getLocalPort() + "...");
                    Socket server = serverSocket.accept();
                    Thread clientHandlerThread = new ClientHandler(server, this.router).getThreading();
                    clientHandlerThread.start();


                    /*System.out.println("Just connected to " + server.getRemoteSocketAddress());
                    DataInputStream in = new DataInputStream(server.getInputStream());

                    System.out.println(in.readUTF());
                    DataOutputStream out = new DataOutputStream(server.getOutputStream());
                    out.writeUTF("Thank you for connecting to " + server.getLocalSocketAddress()
                            + "\nGoodbye!");*/
                    //server.close();
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
        for (int i = 0; i < clientHandlers.length; ++i) {
            if (clientHandlers[i] == null) {
                port = i;
                break;
            }
        }
        return port;
    }
}

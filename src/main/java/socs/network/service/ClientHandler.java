package socs.network.service;
import socs.network.message.SOSPFPacket;
import socs.network.node.Router;
import socs.network.node.RouterDescription;
import socs.network.node.RouterStatus;
import socs.network.util.MessageUtils;

import java.io.*;
import java.net.Socket;


public class ClientHandler implements Runnable{

    private Socket clientSocket;
    private Router router;
    private Thread threading;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private RouterDescription remoteRd;

    public Thread getThreading() {
        return threading;
    }

    public ClientHandler(Socket clientSocket, Router router) {
        this.clientSocket = clientSocket;
        this.router = router;
        threading = new Thread(this);
    }

    public void run() {

        try {
            inputStream = new ObjectInputStream(clientSocket.getInputStream());
            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());

            while (true){
                SOSPFPacket receivedPacket = MessageUtils.receivePacket(inputStream);
                switch (receivedPacket.sospfType){
                    case SOSPFPacket.HELLO:

                        // TODO LDU
                        if (remoteRd == null){
                            remoteRd = new RouterDescription(receivedPacket.srcProcessIP, receivedPacket.srcProcessPort, receivedPacket.srcIP);
                            remoteRd.setStatus(RouterStatus.INIT);
                            SOSPFPacket outMessage = MessageUtils.packMessage(SOSPFPacket.HELLO,router.getRd(), remoteRd );
                            MessageUtils.sendMessage(outMessage, outputStream);
                        }
                        else
                        {
                            remoteRd.setStatus(RouterStatus.TWO_WAY);
                        }
                        break;
                    default:
                        System.out.println("UNKNOWN MESSAGE RECEIVED");

                }

            }
        }
        catch (Exception e){

        }

    }
}

package socs.network.service;


import socs.network.message.SOSPFPacket;
import socs.network.node.Link;
import socs.network.node.Router;
import socs.network.node.RouterDescription;
import socs.network.node.RouterStatus;
import socs.network.util.MessageUtils;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client implements Runnable{
    private Router router;
    private RouterDescription rd;
    private RouterDescription remoteRd;
    private String remoteRouterIP;
    private Thread threading;
    private Link link;
    private Socket clientSocket;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;


    public Thread getThreading() {
        return threading;
    }

    public Client(Router router, RouterDescription remoteRd, Link link) {
        this.router = router;
        this.rd = router.getRd();
        this.remoteRd = remoteRd;
        this.link = link;
        this.threading = new Thread(this);
    }

    public void run() {
        try {
            //System.out.println("Connecting to " + remoteRouterIP + "...");
            clientSocket = new Socket(remoteRd.getProcessIPAddress(), remoteRd.getProcessPortNumber());
            //System.out.println("Just connected to " + remoteRouterIP + "...");

            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inputStream = new ObjectInputStream(clientSocket.getInputStream());

            SOSPFPacket messageHello = MessageUtils.packMessage(SOSPFPacket.HELLO, rd, remoteRd);
            MessageUtils.sendMessage(messageHello, outputStream);

            SOSPFPacket receivedPacket = MessageUtils.receivePacket(inputStream);
            //TODO add LD to database
            remoteRd.setStatus(RouterStatus.TWO_WAY);
            MessageUtils.sendMessage(messageHello, outputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}

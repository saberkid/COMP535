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

/**
 *
 */
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

    public RouterDescription getRemoteRd() {
        return remoteRd;
    }
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
            clientSocket = new Socket(remoteRd.getProcessIPAddress(), remoteRd.getProcessPortNumber());

            outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            inputStream = new ObjectInputStream(clientSocket.getInputStream());

            SOSPFPacket messageHello = MessageUtils.packMessage(SOSPFPacket.HELLO, rd, remoteRd, router);
            MessageUtils.sendMessage(messageHello, outputStream);

            SOSPFPacket receivedPacket = MessageUtils.receivePacket(inputStream);
            //update Link state database and send lsa with another HELLO
            router.lsaUpdate(remoteRd, link.getWeight());
            remoteRd.setStatus(RouterStatus.TWO_WAY);
            messageHello = MessageUtils.packMessage(SOSPFPacket.HELLO, rd, remoteRd, router);
            MessageUtils.sendMessage(messageHello, outputStream);

            while (true) {
                receivedPacket = MessageUtils.receivePacket(inputStream);

                switch (receivedPacket.sospfType) {

                    case SOSPFPacket.LSU: {
                        //System.out.printf("received lsu from"+receivedPacket.srcIP);
                        router.synchronizeAndPropagate(receivedPacket.lsaArray, receivedPacket.lsuStarter, receivedPacket.srcIP);
                        break;
                    }
                    default:
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
    public boolean isConnectedWith(String remoteIp) {
        return remoteRd.getSimulatedIPAddress().equals(remoteIp);
    }

    public synchronized void propagate(){
        SOSPFPacket message = MessageUtils.packMessage(SOSPFPacket.LSU, rd, remoteRd, router);
        MessageUtils.sendMessage(message, outputStream);
    }
}

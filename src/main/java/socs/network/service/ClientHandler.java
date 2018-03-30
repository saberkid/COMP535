package socs.network.service;
import socs.network.message.SOSPFPacket;
import socs.network.node.Link;
import socs.network.node.Router;
import socs.network.node.RouterDescription;
import socs.network.node.RouterStatus;
import socs.network.util.MessageUtils;

import java.io.*;
import java.net.Socket;

/**
 *
 */
public class ClientHandler implements Runnable{

    private Socket clientSocket;
    private Router router;
    private Thread threading;
    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    
    HeartBeatServer heartbeat;
    TTLServer ttl;
    public volatile boolean exit = false;

    public HeartBeat getHeartbeat() { return heartbeat; }
    public TTL getTtl() { return ttl; }
    private RouterDescription remoteRd;

    public Thread getThreading() {
        return threading;
    }

    public RouterDescription getRemoteRd() {
        return remoteRd;
    }
    
    public Router getRouter() {
    	return router;
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
                    	if (ttl == null) {
                        if (remoteRd == null){
                            remoteRd = new RouterDescription(receivedPacket.srcProcessIP, receivedPacket.srcProcessPort, receivedPacket.srcIP);
                            remoteRd.setStatus(RouterStatus.INIT);
                            SOSPFPacket outMessage = MessageUtils.packMessage(SOSPFPacket.HELLO,router.getRd(), remoteRd, router );
                            MessageUtils.sendMessage(outMessage, outputStream);
                        }
                        else
                        {
                            remoteRd.setStatus(RouterStatus.TWO_WAY);
                            //update LSA and add link with weight
                            //router.synchronize(receivedPacket.lsaArray);
                            short weight = MessageUtils.getLinkWeight(router, receivedPacket);
                            router.lsaUpdate(remoteRd, weight);
                            router.addLinkForHandler(remoteRd, weight, this);

                            //  send LDU to all neighbours
                            /*SOSPFPacket outMessage = MessageUtils.packMessage(SOSPFPacket.LSU, router.getRd(), remoteRd, router );
                            MessageUtils.sendMessage(outMessage, outputStream);*/
                            router.propagateLspToNbr("");
                            
                            heartbeat = new HeartBeatServer(this);
                            heartbeat.start();
                            ttl = new TTLServer(this);
                            ttl.start();
                        }
                    	}else {
                            if (!exit)
                                ttl.restart();
                    	}
                        break;
                    case SOSPFPacket.LSU: {
                        //synchronize LSD and propagate
                        router.synchronizeAndPropagate(receivedPacket.lsaArray, receivedPacket.srcIP);
                        break;
                    }
                    default:
                        System.out.println("UNKNOWN MESSAGE RECEIVED");

                }

            }
        }
        catch (Exception e){


        }

    }
    public synchronized void propagate(){
        SOSPFPacket message = MessageUtils.packMessage(SOSPFPacket.LSU, router.getRd(), remoteRd, router);
        MessageUtils.sendMessage(message, outputStream);
    }
    
    public void sendMessage(short messageType) {
    	SOSPFPacket message = MessageUtils.packMessage(messageType, router.getRd(), remoteRd, router);
    	MessageUtils.sendMessage(message, outputStream);
    }

    public boolean isConnectedWith(String remoteIp) {
        return remoteRd.getSimulatedIPAddress().equals(remoteIp);
    }


}

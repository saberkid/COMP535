package socs.network.util;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;
import socs.network.message.SOSPFPacket;
import socs.network.node.Router;
import socs.network.node.RouterDescription;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;


/**
 *
 */
public class MessageUtils {
    /*public static String resolveMessage(SOSPFPacket messageReceived){
        String messageResolved;
        String messageType;

        switch ( messageReceived.sospfType){
            case SOSPFPacket.HELLO:
                messageType = "HELLO";
                break;
            case SOSPFPacket.LSU:
                messageType = "LSU";
                break;
            default:
                messageType = "UNKNOWN MESSAGE";

        }
        messageResolved = "received " + messageType + " from " + messageReceived.srcIP+ ";";
        return messageResolved;
    }*/

    public static void sendMessage(SOSPFPacket message, ObjectOutputStream outputStream) {
        String messageType = "NULL";
        switch ( message.sospfType){
            case SOSPFPacket.HELLO:
                messageType = "HELLO";
                break;
            case SOSPFPacket.LSU:
                messageType = "LSU";
                break;
            default:
                messageType = "UNKNOWN MESSAGE";

        }
        System.out.println("sending " + messageType + " to" + message.dstIP + ";");
        try {
            outputStream.writeObject(message);
            outputStream.reset();
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static short getLinkWeight(Router router, SOSPFPacket message) {
        short weight = 0;

        final Optional<LSA> lsaOption = message.lsaArray.stream().filter(l -> l.linkStateID.equals(message.srcIP)).findFirst();
        if (lsaOption.isPresent()) {
            Optional<LinkDescription> linkDescription = lsaOption.get().links.stream().filter(ld -> ld.linkID.equals(router.getRd().getSimulatedIPAddress())).findFirst();
            if (linkDescription.isPresent()) {
                weight = (short) linkDescription.get().tosMetrics;
            }
        }

        return weight;
    }

    public static SOSPFPacket packMessage(short type, RouterDescription local, RouterDescription remote, Router router ){
        SOSPFPacket message = new SOSPFPacket();
        message.srcProcessIP = local.getProcessIPAddress();
        message.srcProcessPort = local.getProcessPortNumber();
        message.srcIP = local.getSimulatedIPAddress();
        message.dstIP = remote.getSimulatedIPAddress();
        message.sospfType = type;
        message.routerID = local.getSimulatedIPAddress();
        router.getLsd().get_store().forEach((routerIp, lsa) -> message.lsaArray.add(lsa));
        message.lsuStarter = type == SOSPFPacket.LSU ? message.srcIP : null;
        return message;
    }

    public static SOSPFPacket receivePacket(ObjectInputStream inputStream) {
        SOSPFPacket receivedPacket = null;
        String receivedMessage = "NULL";
        try {
            receivedPacket = (SOSPFPacket) inputStream.readObject();

            String messageType;
            switch (receivedPacket.sospfType){
                case SOSPFPacket.HELLO:
                    messageType = "HELLO";
                    break;
                case SOSPFPacket.LSU:
                messageType = "LSU";
                break;
                default:
                    messageType = "UNKNOWN MESSAGE";

            }

            receivedMessage = "received " + messageType + " from " + receivedPacket.srcIP + ";";
        }
        catch (Exception e){

        }

        System.out.println(receivedMessage);

        return receivedPacket;
    }
}

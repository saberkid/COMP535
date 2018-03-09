package socs.network.util;

import socs.network.message.SOSPFPacket;
import socs.network.node.Router;
import socs.network.node.RouterDescription;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 *
 */
public class MessageUtils {
    public static String resolveMessage(SOSPFPacket messageReceived){
        String messageResolved;
        String messageType;

        switch ( messageReceived.sospfType){
            case SOSPFPacket.HELLO:
                messageType = "HELLO";
                break;
            default:
                messageType = "UNKNOWN MESSAGE";

        }
        messageResolved = "received " + messageType + " from " + messageReceived.srcIP+ ";";
        return messageResolved;
    }

    public static void sendMessage(SOSPFPacket message, ObjectOutputStream outputStream) {

        try {
            outputStream.writeObject(message);
            outputStream.reset();
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                default:
                    messageType = "UNKNOWN MESSAGE";

            }

            receivedMessage = "received " + messageType + " from " + receivedPacket.srcIP + ";";
        }
        catch (Exception e){

        }
        if (!receivedMessage.equals("NULL"))
            System.out.println(receivedMessage);

        return receivedPacket;
    }
}

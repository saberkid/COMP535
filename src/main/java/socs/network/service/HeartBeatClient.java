package socs.network.service;

import socs.network.message.SOSPFPacket;

import java.util.Timer;
import java.util.TimerTask;

public class HeartBeatClient extends HeartBeat{
	private Client client;
	
	public HeartBeatClient(Client client) {
		this.client = client;
		timer = new Timer();
	}
	
	public void start() {
		timer.scheduleAtFixedRate(new HeartBeatTask(), heartbeatDelay*1000, heartbeatDelay*1000);
	}
	
	class HeartBeatTask extends TimerTask{
		public void run() {
			client.sendMessage(SOSPFPacket.HELLO);
		}
	}
}

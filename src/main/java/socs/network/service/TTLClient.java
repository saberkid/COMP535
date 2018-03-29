package socs.network.service;

import socs.network.message.SOSPFPacket;

import java.util.Timer;
import java.util.TimerTask;

public class TTLClient extends TTL {
	private Client client;
	
	public TTLClient(Client client) {
		this.client = client;
		timer = new Timer();
	}
	
	public void start() {
		timer.schedule(new RemindTask(), delay*1000);
	}
	
	public void restart() {
		timer.cancel();
		timer = new Timer();
		timer.schedule(new RemindTask(), delay*1000);
	}
	
	class RemindTask extends TimerTask{
		@Override
		public void run() {
			timer.cancel();
			String remoteIp = client.getRemoteRd().getSimulatedIPAddress();
			System.out.println("No heartbeat from " + remoteIp + "... Disconnecting...");
			client.getRouter().disconnect(remoteIp);
			this.cancel();
		}
	}
}

package socs.network.service;

import socs.network.message.SOSPFPacket;

import java.util.Timer;
import java.util.TimerTask;

public class TTLServer extends TTL{
	private ClientHandler ch;
	
	public TTLServer(ClientHandler ch) {
		this.ch = ch;
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
			ch.getRouter().disconnect(ch.getRemoteRd().getSimulatedIPAddress(), SOSPFPacket.ANNIHILATE);
			this.cancel();
		}
	}
}

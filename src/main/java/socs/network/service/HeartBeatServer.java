package socs.network.service;

import socs.network.message.SOSPFPacket;

import java.util.Timer;
import java.util.TimerTask;

public class HeartBeatServer extends HeartBeat{
	private ClientHandler ch;
	
	public HeartBeatServer(ClientHandler ch) {
		this.ch = ch;
		timer = new Timer();
		//timer.scheduleAtFixedRate(new HeartBeatTask(), heartbeatDelay*1000, heartbeatDelay*1000);
	}
	
	public void start() {
		timer.scheduleAtFixedRate(new HeartBeatTask(), heartbeatDelay*1000, heartbeatDelay*1000);
	}
	
	class HeartBeatTask extends TimerTask{
		public void run() {
			ch.sendMessage(SOSPFPacket.HELLO);
		}
	}
}

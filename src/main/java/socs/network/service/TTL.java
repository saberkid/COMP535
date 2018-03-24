package socs.network.service;

import java.util.Timer;

public abstract class TTL {
	protected Timer timer;
	protected int delay = 10;
	
	public void kill() {
		timer.cancel();
	}
}

package socs.network.service;

import java.util.Timer;

public class HeartBeat {
	protected Timer timer;
	protected int heartbeatDelay = 7;

    public void kill() {
        timer.cancel();
    }
}

package com.ardublock.ui;

public class SerialPortDetectRunnable implements Runnable{
	
	private OpenblocksFrame parentFrame;
	private boolean doStop;
	
	public SerialPortDetectRunnable(OpenblocksFrame frame) {
		this.parentFrame = frame;
		this.doStop = false;
		
	}
	
	public synchronized void doStop() {
		this.doStop = true;
	}

	public synchronized void doRun() {
		this.doStop = false;
	}

	public synchronized boolean keepRunning() {
		return this.doStop == false;
	}
	
	@Override
	public void run() {
		
		while (true) {
			
			if (keepRunning()) {
				
				//- look for new ports every second
				try {
					parentFrame.updateAvailablePorts();
					Thread.sleep(1);	
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
	}

}

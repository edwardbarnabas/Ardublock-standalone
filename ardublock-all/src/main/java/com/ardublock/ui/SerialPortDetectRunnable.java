package com.ardublock.ui;

public class SerialPortDetectRunnable implements Runnable{
	
	private OpenblocksFrame parentFrame;
	
	public SerialPortDetectRunnable(OpenblocksFrame frame) {
		this.parentFrame = frame;
	}
	
	@Override
	public void run() {
		
		while (true) {
			//- look for new ports every second
			try {
				System.out.println("Auto-detecting ports...");
				parentFrame.updateAvailablePorts();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}

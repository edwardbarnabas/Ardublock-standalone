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
					
					
					String currentItem = (String) parentFrame.portOptionsComboBox.getSelectedItem();
					
					System.out.print("Auto-detecting ports... Current port: " + currentItem + "... ");
					
					parentFrame.updateAvailablePorts();
					
					if(parentFrame.portOptionsModel.getIndexOf(currentItem) == -1) {
						//-do nothing
						System.out.println("Device is gone!");
					}
					else {
						System.out.println("Device is still here!");
						parentFrame.portOptionsModel.setSelectedItem(currentItem);
					}
					
					Thread.sleep(1000);
				
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
	}

}

package com.ardublock.ui;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.ardublock.ui.listener.SerialMonitor;

public class SerialMonitorRunnable implements Runnable{
	
	private boolean doStop;
	private JTextArea tArea;
	private SerialMonitor monitor;
	private JScrollPane scrollPane;
	private OpenblocksFrame parentFrame;
	
	public SerialMonitorRunnable(OpenblocksFrame frame) {
		doStop = false;
		parentFrame = frame;
		tArea = parentFrame.serialMonitortextArea;
		monitor = parentFrame.monitor;
		scrollPane = parentFrame.serialMonitorScrollPane;
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
    	
    	String str;
    	int bytesToRead = 50;
    	
    	while (true) {
    		while(keepRunning()) {
                // keep doing what this thread should do.
                str = monitor.readCharNB(bytesToRead);
	    		tArea.append(str);
	    		
	    		//- update position of the scroll bar to the bottom of jpane
	    		JScrollBar vertical = scrollPane.getVerticalScrollBar();
	    		vertical.setValue( vertical.getMaximum() );
	    		
            }
    	}
        
    }

}


package com.ardublock.ui;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JTextArea;

import com.ardublock.ui.listener.GenerateCodeButtonListener;

public class DriverRunnable implements Runnable {

	private boolean doStop;
	public String[] upload_cmd_array;
	private JTextArea textArea;
	private OpenblocksFrame parentFrame;
	public boolean timed_out = false;
	
	private GenerateCodeButtonListener listener;

	public DriverRunnable (OpenblocksFrame oframe) {
		parentFrame = oframe;
		textArea = parentFrame.uploadTextArea;
		doStop = false;
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
		
		/* loop here forever once the thread begins.  Execute the upload process by toggling the doStop flag */
		while (true) {
			if (keepRunning()) {
				try {
					runDriver();
					
				} catch (InterruptedException | IOException | RuntimeException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Exception caught.  Exiting out of run().");
					return;
				}	
			}
		}
		
	}
	
	
	
	private void runDriver() throws InterruptedException, IOException, RuntimeException {
		
		//- Initialize variables
		String line = null;
		int exitStatus = -1;
		
		File dir = new File(parentFrame.context.getCH341Dir()); 
		
		exitStatus = OpenblocksFrame.runCommandLineSpecificDir(parentFrame.context.buildCH341Command(),dir);
		
		if (exitStatus != 0) {
			if (exitStatus == -2) {
				textArea.append("\nCheck to make sure that the CH341 folder exists!\n");	
			}
			else {
				textArea.append("\nError updating driver!\n");
			}
			textArea.setBackground(Color.red);
		}
		else {
			textArea.append("\nDriver successfully updated!");
			textArea.setBackground(Color.green);
		}
		
		textArea.setCaretPosition(textArea.getDocument().getLength());	
		
		this.doStop = true;
		return;
		
	}

	
		
	
	

}

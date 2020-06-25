package com.ardublock.ui;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JTextArea;

import com.ardublock.ui.listener.GenerateCodeButtonListener;

public class ArduinoCliRunnable implements Runnable {

	private boolean doStop;
	public String[] upload_cmd_array;
	private JTextArea textArea;
	private OpenblocksFrame parentFrame;
	public boolean timed_out = false;
	
	private SerialUploadRunnable uploadRunnable;
	private Thread uploadThread;
	private GenerateCodeButtonListener listener;

	public ArduinoCliRunnable (OpenblocksFrame oframe) {
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
					//-do the compile first
					runArduinoCli();
					
				} catch (InterruptedException | IOException | RuntimeException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Exception caught.  Exiting out of run().");
					return;
				}	
			}
		}
		
	}
	
	
	
	
	
	private void runArduinoCli() throws InterruptedException, IOException, RuntimeException {
		
		//- Initialize variables
		String line = null;
		int exitStatus = -1;
		String[] cmd;
		
		exitStatus = OpenblocksFrame.runCommandLine(parentFrame.context.build_board_index_cli_cmd());
		textArea.append("\n");
		
		if (exitStatus != 0) {
			
			if (exitStatus == -2) {
				textArea.append("Check to make sure that the arduino-cli folder exists!\n");	
			}
			else {
				textArea.append("Error updating board index.  Make sure that your computer is connected to the internet and try again!\n");
			}
			textArea.setBackground(Color.red);
			textArea.setCaretPosition(textArea.getDocument().getLength());	
			this.doStop = true;
			return;
		}
		
		exitStatus = OpenblocksFrame.runCommandLine(parentFrame.context.build_install_servolib_cmd());
		
		if (exitStatus != 0) {
			if (exitStatus == -2) {
				textArea.append("Check to make sure that the arduino-cli folder exists!\n");	
			}
			else {
				textArea.append("Error installing servo library.  Make sure that your computer is connected to the internet and try again!\n");
			}
			textArea.setBackground(Color.red);
			textArea.setCaretPosition(textArea.getDocument().getLength());	
			this.doStop = true;
			return;
		}
		
		textArea.append("\n");

		//- get arduino boards
		cmd = parentFrame.context.build_core_arduino_boards_cli_cmd();
		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec(cmd);
		textArea.append("Sending command: "); 
		for (String str : cmd) {
			textArea.append(str + " ");
		}
		textArea.append("\n");
		
		//- read feedback from process//- read feedback from process
		BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
		
		while ((line = input.readLine()) != null) {
			//- stop loop if the caller is trying to interrupt it
			if (Thread.currentThread().isInterrupted()) {
		        throw new RuntimeException(); 
		    }
			textArea.append(line + "\n");
			textArea.setCaretPosition(textArea.getDocument().getLength());	
			System.out.println(line);
		}
		process.waitFor();
		
		if (exitStatus != 0) {
			textArea.append("Error installing arduino core.  Make sure that your computer is connected to the internet and try again!");
			textArea.setBackground(Color.red);
		}
		else {
			textArea.append("Software successfully updated!");
			textArea.setBackground(Color.green);
		}
		
		textArea.setCaretPosition(textArea.getDocument().getLength());	
		this.doStop = true;
		return;
		
	}

	
		
	
	

}

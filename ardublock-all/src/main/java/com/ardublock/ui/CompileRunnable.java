package com.ardublock.ui;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JTextArea;

import com.ardublock.ui.listener.GenerateCodeButtonListener;

public class CompileRunnable implements Runnable {

	private boolean doStop;
	public String[] upload_cmd_array;
	private JTextArea textArea;
	private OpenblocksFrame parentFrame;
	public boolean timed_out = false;
	
	private SerialUploadRunnable uploadRunnable;
	private Thread uploadThread;
	private GenerateCodeButtonListener listener;

	public CompileRunnable (OpenblocksFrame oframe, GenerateCodeButtonListener gcbl) {
		parentFrame = oframe;
		textArea = parentFrame.uploadTextArea;
		doStop = false;
		uploadRunnable = new SerialUploadRunnable(parentFrame);
		uploadThread = new Thread(uploadRunnable);
		listener = gcbl;
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
					
					//-do the compile first.  If there was an error, skip the uploading
					if(!runCompileProcess_arduino_cli()) continue;
					
					//-when we are finished compiling, start the upload process
					upload_cmd_array = listener.build_arduino_cli_upload_cmd();
					
					//- if there is an existing thread that is alive, then just toggle the run flag by calling doRun() 
					if (uploadThread.isAlive()) {
		        		System.out.println("uploadThread is alive!");
						uploadRunnable.upload_cmd_array = upload_cmd_array;
		        		uploadRunnable.doRun();
		        	}
		        	else {
		        		//- the current uploadThread is not alive, or not running, so create a new runnable and thread and start it
		        		System.out.println("Starting upload thread...");
		        	
		        		uploadRunnable = new SerialUploadRunnable(parentFrame);
						uploadRunnable.upload_cmd_array = upload_cmd_array;
		        	
		        		uploadThread = new Thread(uploadRunnable);
		        		uploadThread.start(); 
		        	}
					
				} catch (InterruptedException | IOException | RuntimeException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Exception caught.  Exiting out of run().");
					return;
				}	
			}
		}
		
	}
	
	
	
	private boolean runCompileProcess_arduino_cli() throws InterruptedException, IOException, RuntimeException {
		
		//- Initialize variables
		String line = null;
		int exitStatus = -1;
		boolean startedLibraries = false;
		boolean ret;
		
		textArea.append("\nCompiling with arduino_cli command: ");
		for (String str : upload_cmd_array) {
			textArea.append(str + " ");
		}

		//- setup process
		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec(upload_cmd_array);

		//- read feedback from process//- read feedback from process
		BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
		
		while ((line = input.readLine()) != null) {
			
			//- stop loop if the caller is trying to interrupt it
			if (Thread.currentThread().isInterrupted()) {
		        throw new RuntimeException(); 
		    }
			
			System.out.println(line);
			//- only print dots if we've already printed the compiling message.
			if (startedLibraries) textArea.append(".");
			
			//- go to last line of the textArea
			textArea.setCaretPosition(textArea.getDocument().getLength());	
    		
			
			if (line.contains("Detecting libraries used")) {
				textArea.append("\n" + line);
				startedLibraries = true;
			}
			
			if (line.contains("Generating function prototypes")) {
				textArea.append("\n" + line);
			}
			
			if (line.contains("Compiling sketch")) {
				textArea.append("\n" + line);
			}
			
			if (line.contains("Compiling libraries")) {
				textArea.append("\n" + line);
			}
			
			if (line.contains("Compiling core")) {
				textArea.append("\n" + line);
			}
			
			if (line.contains("Linking everything together")) {
				textArea.append("\n" + line);
			}
			
			if (line.contains("Sketch uses")) {
				textArea.append("\n" + line);
			}
			
			if (line.contains("Global variables use")) {
				textArea.append("\n" + line);
			}
  
		}
		process.waitFor();
		
		exitStatus = process.exitValue();
		
		System.out.println("Exit Status: " + exitStatus);

		switch(exitStatus) {
		  case 0:
			textArea.append("\nCompiled Successfully!\n");
			ret = true;
		    break;
		  case 1:
			textArea.append("\nCompile Error! Update Software.  Make sure that you're connected to the internet before doing so!");
			textArea.setBackground(Color.red);
			ret = false;
		  default:
			textArea.append("\nCompile Failed! Unknown Error code: " + exitStatus + "\n");
			textArea.setBackground(Color.red);
			ret = false;
			break;
		}
		
		this.doStop = true;

		//- go to last line of the textArea
		textArea.setCaretPosition(textArea.getDocument().getLength());	
		
		return ret;
		
	}

	
		
	
	

}

package com.ardublock.ui;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.JTextArea;

public class SerialUploadRunnable implements Runnable {

	private boolean doStop;
	public String[] upload_cmd_array;
	private JTextArea textArea;
	private OpenblocksFrame parentFrame;
	public boolean timed_out = false;
	
	//- timeout of 15 sec 
	private long timeOut = 15000;

	public SerialUploadRunnable(OpenblocksFrame oframe) {
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
					runUploadProcess_arduino_cli();
				} catch (InterruptedException | IOException | RuntimeException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Exception caught.  Exiting out of run().");
					return;
				}	
			}
		}
		
	}
	
	private void runUploadProcess_arduino_cli() throws InterruptedException, IOException, RuntimeException {
		
		//- Initialize variables
		String line = null;
		int exitStatus = -1;

		//- print out command
		System.out.println("running arduino_cli upload: ");
		
		textArea.append("\nUploading with arduino_cli command: ");
		for (String str : upload_cmd_array) {
			textArea.append(str + " ");
		}
		
		textArea.append("\nStarting to upload. Please wait.  We'll give it up to " + (timeOut/1000) + " seconds...");
		textArea.setCaretPosition(textArea.getDocument().getLength());	

		//- setup process
		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec(upload_cmd_array);

		//- we have timeout functionality here to exit gracefully if it gets stuck here.
		Worker worker = new Worker(process);
		worker.start();
		System.out.println("Testing for timeout...");
		
		try {
			
		    worker.join(timeOut);
		    
		    if (worker.exit != null) {
		    	int workerStatus = worker.exit;
		    	System.out.println("Success: Process completed without time out." + "Status Code: " + workerStatus);
		    }
		    else {
		    	//- process timed out
		    	timed_out = true;
		    	System.out.println("Error: Process timed out!");
		    	textArea.append("\nUpload Failed! Timeout error.");
		    	textArea.append("\nIt shouldn't take this long!");
		    	textArea.append("\nCheck settings and try again!");
				textArea.setBackground(Color.red);
		    	
		    	//- show the last character added in the scrollbar
				textArea.setCaretPosition(textArea.getDocument().getLength());	
		    	
		    	//- if there was a timeout, destroy the descendant processes as well as the main one
		    	//- I initially tried to just destroy the main process, but the port would still be
		    	//- blocked.  
		    	process.descendants().forEach(ph -> {
		    	    ph.destroy();
		    	});
		    	//- destroy main process
		    	process.destroy();
		    	
		    	System.out.println("Destroyed all processes...");
		    	
		    	//- throw runtime exception so that the thread dies once it finishes executing run()
		    	throw new RuntimeException(); 

		    }
		}
	    catch(InterruptedException ex) {
		    worker.interrupt();
		    Thread.currentThread().interrupt();
		    throw ex;
	    } 
		finally {
			
			System.out.println("Executing finally block...");
			
			//- only get exitStatus if the process did NOT time out, else exitStatus == -1.
			if (!timed_out) {
				process.waitFor();
				exitStatus = process.exitValue();
			}
			
			//- lose the worker if there was a timeout
			if (timed_out) {
				this.doStop = true;
			}

		}
		
		System.out.println("Exit Status: " + exitStatus);
		
		switch(exitStatus) {
		  case 0:
			textArea.append("\nSuccess! Done Uploading!");
			textArea.setBackground(Color.green);
		    break;
		  case 1:
			textArea.append("\nUpload Failed! Check connection!");
			textArea.setBackground(Color.red);
		    break;
		  case 2:
			textArea.append("\nUpload Failed! Sketch not found!");
			textArea.setBackground(Color.red);
			break;
		  case 3:
			textArea.append("\nUpload Failed! Invalid (argument for) commandline option");
			textArea.setBackground(Color.red);
			break;
		  case 4:
			textArea.append("\nUpload Failed! Preference passed to --get-pref does not exist");
			textArea.setBackground(Color.red);
			break;
		  default:
			textArea.append("\nUpload Failed! Unknown Error code: " + exitStatus + "");
			textArea.setBackground(Color.red);
			break;
		}
		
		this.doStop = true;

		//- go to last line of the textArea
		textArea.setCaretPosition(textArea.getDocument().getLength());	
		
	}
/*
	private void runUploadProcess_arduino_debug() throws InterruptedException, IOException, RuntimeException {
		
		//- Initialize variables
		String line = null;
		int exitStatus = -1;
		boolean startedLibraries = false;
		
		//- setup process
		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec(upload_cmd_array);

		//- read feedback from process
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
				textArea.append("\n\n" + line);
			}
			
			if (line.contains("Global variables use")) {
				textArea.append("\n\n" + line);
			}
			
			if (line.contains("-Uflash:w:")) {
				
				textArea.append("\n");

			

				//- execution gets to this code once compiling finishes and it begins to upload
				//- we have timeout functionality here to exit gracefully if it gets stuck here.
				Worker worker = new Worker(process);
				worker.start();
				
				try {
					//- 8 second timeout for uploading
				    worker.join(8000);
				    
				    System.out.println("Testing for timeout.");

					textArea.setCaretPosition(textArea.getDocument().getLength());	
				    				    
				    if (worker.exit != null) {
				    	int workerStatus = worker.exit;
				    	System.out.println("Success: Process completed without time out." + "Status Code: " + workerStatus);
				    }
				    else {
				    	//- process timed out
				    	timed_out = true;
				    	System.out.println("Error: Process timed out!");
				    	
				    	textArea.append("\n");
						
				    	textArea.append("\nUpload Failed! Timeout error.");
				    	textArea.append("\nIt shouldn't take this long!");
				    	textArea.append("\nCheck settings and try again!\n\n");
						textArea.setBackground(Color.red);
				    	
				    	//- show the last character added in the scrollbar
						textArea.setCaretPosition(textArea.getDocument().getLength());	
				    	
				    	//- if there was a timeout, destroy the descendant processes as well as the main one
				    	//- I initially tried to just destroy the main process, but the port would still be
				    	//- blocked.  
				    	process.descendants().forEach(ph -> {
				    	    ph.destroy();
				    	});
				    	//- destroy main process
				    	process.destroy();
				    	
				    	System.out.println("Destroyed all processes...");
				    	
				    	//- throw runtime exception so that the thread dies once it finishes executing run()
				    	throw new RuntimeException(); 

				    }
				}
			    catch(InterruptedException ex) {
				    worker.interrupt();
				    Thread.currentThread().interrupt();
				    throw ex;
			    } 
				finally {
					
					System.out.println("Executing finally block...");
					
					//- only get exitStatus if the process did NOT time out, else exitStatus == -1.
					if (!timed_out) {
						exitStatus = process.exitValue();
					}
					
					//- lose the worker if there was a timeout
					if (timed_out) {
						this.doStop = true;
					}

				}
				
			}
	
			
			
		}
		
		
		textArea.append("\n");
		
		//- go to last line of the textArea
		textArea.setCaretPosition(textArea.getDocument().getLength());		
		
		switch(exitStatus) {
		  case 0:
			textArea.append("\nSuccess! Done Uploading!\n\n");
			textArea.setBackground(Color.green);
		    break;
		  case 1:
			textArea.append("\nUpload Failed! Check connection!\n\n");
			textArea.setBackground(Color.red);
		    break;
		  case 2:
			textArea.append("\nUpload Failed! Sketch not found!\n\n");
			textArea.setBackground(Color.red);
			break;
		  case 3:
			textArea.append("\nUpload Failed! Invalid (argument for) commandline option\n\n");
			textArea.setBackground(Color.red);
			break;
		  case 4:
			textArea.append("\nUpload Failed! Preference passed to --get-pref does not exist\n\n");
			textArea.setBackground(Color.red);
			break;
		  default:
			textArea.append("\nUpload Failed! Unknown Error code: " + exitStatus + "\n\n");
			textArea.setBackground(Color.red);
			break;
		}
		
		this.doStop = true;

		//- go to last line of the textArea
		textArea.setCaretPosition(textArea.getDocument().getLength());	
		
	}
*/	

}

package com.ardublock.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SerialUploadRunnable implements Runnable {

	private boolean doStop;
	public String upload_cmd;
	private JScrollPane scrollPane;
	private JTextArea textArea;
	private JFrame frame;
	public boolean timed_out = false;

	public SerialUploadRunnable(JFrame jframe, JTextArea jTextArea, JScrollPane jscrollPane) {
		upload_cmd = null;
		textArea = jTextArea;
		scrollPane = jscrollPane;
		doStop = false;
		frame = jframe;
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
					runUploadProcess();
				} catch (InterruptedException | IOException | RuntimeException e ) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("Exception caught.  Exiting out of run().");
					return;
				}	
			}
		}
		
	}

	private void runUploadProcess() throws InterruptedException, IOException, RuntimeException {
		
		//- Initialize variables
		String line = null;
		int exitStatus = -1;
		boolean startedLibraries = false;
		
		
		System.out.println(upload_cmd);
		
		//- setup process
		Runtime rt = Runtime.getRuntime();
		Process process = rt.exec(upload_cmd);
		
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
				textArea.append("\n**************************************************************");
				textArea.append("\n************** Uploading Code To Device ****************");
				textArea.append("\n**************************************************************");
				
				textArea.append("\nStarting to upload. Please wait...");
				
				//- go to last line of the textArea
				textArea.setCaretPosition(textArea.getDocument().getLength());	

				//- execution gets to this code once compiling finishes and it begins to upload
				//- we have timeout functionality here to exit gracefully if it gets stuck here.
				Worker worker = new Worker(process);
				worker.start();
				System.out.println("Testing for timeout...");
				
				try {
					//- 8 second timeout for uploading
				    worker.join(8000);
				    
				    if (worker.exit != null) {
				    	int workerStatus = worker.exit;
				    	System.out.println("Success: Process completed without time out." + "Status Code: " + workerStatus);
				    }
				    else {
				    	//- process timed out
				    	timed_out = true;
				    	System.out.println("Error: Process timed out!");
				    	
				    	textArea.append("\n");
						textArea.append("\n**************************************************************");
						textArea.append("\n*************************** Result **************************");
						textArea.append("\n**************************************************************");
						
				    	textArea.append("\nUpload Failed! Timeout error.");
				    	textArea.append("\nIt shouldn't take this long!");
				    	textArea.append("\nCheck settings and try again!\n\n");
				    	
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
		textArea.append("\n**************************************************************");
		textArea.append("\n*************************** Result **************************");
		textArea.append("\n**************************************************************");
		
		//- go to last line of the textArea
		textArea.setCaretPosition(textArea.getDocument().getLength());		
		
		switch(exitStatus) {
		  case 0:
			textArea.append("\nSuccess! Done Uploading!\n\n");
		    break;
		  case 1:
			textArea.append("\nUpload Failed! Check connection!\n\n");
		    break;
		  case 2:
			textArea.append("\nUpload Failed! Sketch not found!\n\n");
			break;
		  case 3:
			textArea.append("\nUpload Failed! Invalid (argument for) commandline option\n\n");
			break;
		  case 4:
			textArea.append("\nUpload Failed! Preference passed to --get-pref does not exist\n\n");
			break;
		  default:
			textArea.append("\nUpload Failed! Unknown Error.\n\n");
			break;
		}
		
		this.doStop = true;

		//- go to last line of the textArea
		textArea.setCaretPosition(textArea.getDocument().getLength());	

		
		//- automatically close the window if it was successful.  If there is an error,
		//- leave it open so that the user can see.
	/*	if (exitStatus == 0) {
			textArea.append("\nThis window will close automatically in 3 seconds...");
			Thread.sleep(3000);
			frame.dispose();
		}
		else {
			//-just leave it
		}
	*/	
		
	}
	

}

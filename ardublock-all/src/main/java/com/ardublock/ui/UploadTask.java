package com.ardublock.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class UploadTask implements Callable<String> {
	
	public JScrollPane scrollPane;
	public String upload_cmd;
	public JTextArea textArea;
	
	
	public UploadTask(JScrollPane sp, JTextArea ta, String cmd) {
		this.scrollPane = sp;
		this.textArea = ta;
		this.upload_cmd = cmd;
	}
	
    @Override
    public String call() throws Exception {
    	
    	upload();
    	
        return "Finished the call!";
    }
    
    private void upload() {
    	
    	String line = null;
    	JScrollBar vertical = scrollPane.getVerticalScrollBar();
    	int exitStatus = -1;
    	
    	System.out.println(upload_cmd);
    	
    	//- setup process
    	Runtime rt = Runtime.getRuntime();
    	try {
    		
    		Process process = rt.exec(upload_cmd);
    		
    		//- read feedback from process
    		BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
    		while ((line = input.readLine()) != null) {
    			
    			//- stop loop if the caller if trying to interrupt it
    			if (Thread.currentThread().isInterrupted()) {
    		        throw new RuntimeException(); 
    		    }
    			
    			//System.out.println(line);
    			System.out.print(".");
    			//- update status frame with process feedback
    			textArea.append(".");
    			//- position scroll to the bottom of the frame
    			vertical.setValue(vertical.getMaximum());
    		}
    		
    		//- get result of process and display it 
    		exitStatus = process.exitValue();
    		switch(exitStatus) {
    		  case 0:
    			textArea.append("\n\nDone Uploading!\n");
    		    break;
    		  case 1:
    			textArea.append("\n\nUpload Failed! Check connection!\n");
    		    break;
    		  case 2:
    			textArea.append("\n\nUpload Failed!  Sketch not found!\n");
    			break;
    		  case 3:
    			textArea.append("\n\nUpload Failed!  Invalid (argument for) commandline option\n");
    			break;
    		  case 4:
    			textArea.append("\n\nUpload Failed!  Preference passed to --get-pref does not exist\n");
    			break;
    		  default:
    			textArea.append("\n\nUpload Failed!  Unknown Error.");
    			break;
    		}
    		
    		vertical.setValue(vertical.getMaximum());
		
    	} catch (IOException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}

    }

}


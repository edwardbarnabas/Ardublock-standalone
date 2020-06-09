package com.ardublock.ui.listener;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JOptionPane;

import com.fazecast.jSerialComm.SerialPort;

/* jSerial reference
 * https://fazecast.github.io/jSerialComm/
 */

public class SerialMonitor {
	
	public String selectedPort;
	public int selectedBaud;
	public int selectedBitSize;
	public int selectedStopBit;
	public int selectedParity;
	
	SerialPort sp;
	
	public SerialMonitor() {
		selectedPort = null;
		selectedBaud = 9600;
		selectedBitSize = 8;
		selectedStopBit = 1;
		selectedParity = 0;
		sp = null;
	}
	
	static public String[] getPorts() {
		SerialPort[] ports;
		String[] ret = null;
		
		ports = SerialPort.getCommPorts();
		
		if (ports.length > 0) {
			String[] portStrings = new String[ports.length];
			for(int i=0;i<ports.length;i++) {
				portStrings[i]=ports[i].getSystemPortName();
			}
			return portStrings;
		}
		else return ret;
	}
	
	public boolean open() {
		
		sp = SerialPort.getCommPort(selectedPort);
		sp.setComPortParameters(selectedBaud, selectedBitSize, selectedStopBit, selectedParity);
		
		if (sp.openPort()) {
	      System.out.println("Port is open :)");
	      return true;
	    } 
		else {
	      System.out.println("Failed to open port :(");
	      return false;
	    }   
	}
	
	public boolean close() {
		if (sp.closePort()) {
	      System.out.println("Port is closed :)");
	      return true;
	    } 
		else {
	      System.out.println("Failed to close port :(");
	      return false;
	    }
		
	}
	
	public void writeChar(Integer i) {
		sp.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0); // block until bytes can be written
		try {
			sp.getOutputStream().write(i.byteValue());
			sp.getOutputStream().flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    System.out.println("Sent number: " + i);
	}
	
	/*
	 * reads # of bytes (set by charCount) and returns it as a String.
	 * Non-blocking: means that it does not wait there if there is no data.
	*/
	public String readCharNB(int charCount) {
		char c = 0;
		String str = null;
		byte[] readBuffer = new byte[1];
		int numRead = 0;
		
		sp.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
	    try {
	    	
	    	for (int j = 0; j < charCount; ++j) {
	    		
	    		// wait for serial
	    		while (sp.bytesAvailable() == 0) {
	    			Thread.sleep(1);
	    		}
	    		
		        numRead += sp.readBytes(readBuffer, 1);
		        
		        // initialize string if this is the first read
		        if (j==0) {
		        	str = new String(readBuffer);
		        } else {
		        	str += new String(readBuffer);
		        }
		        
	    	}
	    } 
	    catch (Exception e) { 
	    	e.printStackTrace(); 
	    }
	    System.out.println("\nBytes Read: " + numRead);
	    System.out.println("\nResulting String:\n"+ str);
	    return str;
	}

	public String readChar(int num) {
		String str = null;
		sp.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
	    InputStream in = sp.getInputStream();
	    
	    try {
	       for (int j = 0; j < num; ++j) {
	    	   char c = (char)in.read();
	    	   str += c;
	    	   System.out.print(c);
	       }
	       in.close();
	    } 
	    catch (Exception e) { 
	    	e.printStackTrace(); 
	    }
	    return str;
	}
	
	public String portSelector() {
		String[] portList = SerialMonitor.getPorts();
	    selectedPort = (String) JOptionPane.showInputDialog(null, "Select Port", "Available Ports:", JOptionPane.QUESTION_MESSAGE, null, portList, portList[0]); // Initial choice
		return selectedPort;
	}
}
	    


package com.ardublock.ui.listener;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.*;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;


import com.ardublock.core.Context;
import com.ardublock.translator.AutoFormat;
import com.ardublock.translator.Translator;
import com.ardublock.translator.block.exception.BlockException;
import com.ardublock.translator.block.exception.SocketNullException;
import com.ardublock.translator.block.exception.SubroutineNameDuplicatedException;
import com.ardublock.translator.block.exception.SubroutineNotDeclaredException;
import com.ardublock.ui.OpenblocksFrame;
import com.ardublock.ui.SerialUploadRunnable;


import edu.mit.blocks.codeblocks.Block;
import edu.mit.blocks.renderable.RenderableBlock;
import edu.mit.blocks.workspace.Workspace;

public class GenerateCodeButtonListener implements ActionListener
{
	//private JFrame parentFrame;
	private OpenblocksFrame parentFrame;
	
	
	private Context context;
	private Workspace workspace; 
	private ResourceBundle uiMessageBundle;
	
	private Thread uploadThread;
	private SerialUploadRunnable uploadRunnable;
	private String upload_cmd;

	private ArrayList<String> upload_cmd_list;
	private String[] upload_cmd_array;

	//private ArrayList<String> upload_cli_list;
	//private String[] upload_cli_array;

	private JTextArea textArea;
	
	//- upload file parameters
	private String sketchfileDir;
	private String sketchfilePath;
	private String arduinoCliDir;
	private String arduinoCliPath;

	public GenerateCodeButtonListener(OpenblocksFrame oframe, Context context)
	{
		this.parentFrame = oframe;
		this.context = context;
		
		this.textArea = oframe.uploadTextArea;
		
		workspace = context.getWorkspaceController().getWorkspace();
		uiMessageBundle = ResourceBundle.getBundle("com/ardublock/block/ardublock");
		
		upload_cmd = null;
		upload_cmd_list = new ArrayList<String>();
		//upload_cli_list = new ArrayList<String>();

		uploadRunnable = new SerialUploadRunnable(parentFrame);
		uploadThread = new Thread(uploadRunnable);
		
		//-create temp_sketch directory in the executing folder of this .jar
		sketchfileDir = context.getSketchDir();
		
		File directory = new File(sketchfileDir);
		if(!directory.exists()) {
			System.out.println("Creating directory: " + sketchfileDir);
			directory.mkdir();
		}
		
		//- create file path to the .ino that will be created later.
		//- add quotes around file path in case there are whitespaces in the path
		sketchfilePath = sketchfileDir + context.getSketchName();

	}

	public boolean generateC() {
		boolean success;
		success = true;
		Translator translator = new Translator(workspace);
		translator.reset();
		
		Iterable<RenderableBlock> renderableBlocks = workspace.getRenderableBlocks();
		
		Set<RenderableBlock> loopBlockSet = new HashSet<RenderableBlock>();
		Set<RenderableBlock> subroutineBlockSet = new HashSet<RenderableBlock>();
		Set<RenderableBlock> scoopBlockSet = new HashSet<RenderableBlock>();
		
		StringBuilder code = new StringBuilder();
		
		
		for (RenderableBlock renderableBlock:renderableBlocks)
		{
			Block block = renderableBlock.getBlock();
			
			if (!block.hasPlug() && (Block.NULL.equals(block.getBeforeBlockID())))
			{
				
				if(block.getGenusName().equals("loop"))
				{
					loopBlockSet.add(renderableBlock);
				}
				if(block.getGenusName().equals("loop1"))
				{
					loopBlockSet.add(renderableBlock);
				}
				if(block.getGenusName().equals("loop2"))
				{
					loopBlockSet.add(renderableBlock);
				}
				if(block.getGenusName().equals("loop3"))
				{
					loopBlockSet.add(renderableBlock);
				}
				if(block.getGenusName().equals("program"))
				{
					loopBlockSet.add(renderableBlock);
				}
				if(block.getGenusName().equals("setup"))
				{
					loopBlockSet.add(renderableBlock);
				}
				if (block.getGenusName().equals("subroutine"))
				{
					String functionName = block.getBlockLabel().trim();
					try
					{
						translator.addFunctionName(block.getBlockID(), functionName);
					}
					catch (SubroutineNameDuplicatedException e1)
					{
						context.highlightBlock(renderableBlock);
						//find the second subroutine whose name is defined, and make it highlight. though it cannot happen due to constraint of OpenBlocks -_-
						JOptionPane.showMessageDialog(parentFrame, uiMessageBundle.getString("ardublock.translator.exception.subroutineNameDuplicated"), "Error", JOptionPane.ERROR_MESSAGE);
						return false;
					}
					subroutineBlockSet.add(renderableBlock);
				}
				if (block.getGenusName().equals("scoop_task"))
				{
					translator.setScoopProgram(true);
					scoopBlockSet.add(renderableBlock);
				}
				if (block.getGenusName().equals("scoop_loop"))
				{
					translator.setScoopProgram(true);
					scoopBlockSet.add(renderableBlock);
				}
				if (block.getGenusName().equals("scoop_pin_event"))
				{
					translator.setScoopProgram(true);
					scoopBlockSet.add(renderableBlock);
				}
				
			}
		}
		if (loopBlockSet.size() == 0) {
			JOptionPane.showMessageDialog(parentFrame, uiMessageBundle.getString("ardublock.translator.exception.noLoopFound"), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (loopBlockSet.size() > 1) {
			for (RenderableBlock rb : loopBlockSet)
			{
				context.highlightBlock(rb);
			}
			JOptionPane.showMessageDialog(parentFrame, uiMessageBundle.getString("ardublock.translator.exception.multipleLoopFound"), "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try
		{
			
			for (RenderableBlock renderableBlock : loopBlockSet)
			{
				translator.setRootBlockName("loop");
				Block loopBlock = renderableBlock.getBlock();
				code.append(translator.translate(loopBlock.getBlockID()));
			}
			
			for (RenderableBlock renderableBlock : scoopBlockSet)
			{
				translator.setRootBlockName("scoop");
				Block scoopBlock = renderableBlock.getBlock();
				code.append(translator.translate(scoopBlock.getBlockID()));
			}
			
			for (RenderableBlock renderableBlock : subroutineBlockSet)
			{
				translator.setRootBlockName("subroutine");
				Block subroutineBlock = renderableBlock.getBlock();
				code.append(translator.translate(subroutineBlock.getBlockID()));
			}
			
			translator.beforeGenerateHeader();
			code.insert(0, translator.genreateHeaderCommand());
		}
		catch (SocketNullException e1)
		{
			e1.printStackTrace();
			success = false;
			Long blockId = e1.getBlockId();
			Iterable<RenderableBlock> blocks = workspace.getRenderableBlocks();
			for (RenderableBlock renderableBlock2 : blocks)
			{
				Block block2 = renderableBlock2.getBlock();
				if (block2.getBlockID().equals(blockId))
				{
					context.highlightBlock(renderableBlock2);
					break;
				}
			}
			JOptionPane.showMessageDialog(parentFrame, uiMessageBundle.getString("ardublock.translator.exception.socketNull"), "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (BlockException e2)
		{
			e2.printStackTrace();
			success = false;
			Long blockId = e2.getBlockId();
			Iterable<RenderableBlock> blocks = workspace.getRenderableBlocks();
			for (RenderableBlock renderableBlock2 : blocks)
			{
				Block block2 = renderableBlock2.getBlock();
				if (block2.getBlockID().equals(blockId))
				{
					context.highlightBlock(renderableBlock2);
					break;
				}
			}
			JOptionPane.showMessageDialog(parentFrame, e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (SubroutineNotDeclaredException e3)
		{
			e3.printStackTrace();
			success = false;
			Long blockId = e3.getBlockId();
			Iterable<RenderableBlock> blocks = workspace.getRenderableBlocks();
			for (RenderableBlock renderableBlock3 : blocks)
			{
				Block block2 = renderableBlock3.getBlock();
				if (block2.getBlockID().equals(blockId))
				{
					context.highlightBlock(renderableBlock3);
					break;
				}
			}
			JOptionPane.showMessageDialog(parentFrame, uiMessageBundle.getString("ardublock.translator.exception.subroutineNotDeclared"), "Error", JOptionPane.ERROR_MESSAGE);
			
		}
		
		if (success)
		{
			AutoFormat formatter = new AutoFormat();
			String codeOut = code.toString();
			
			if (context.isNeedAutoFormat)
			{
				codeOut = formatter.format(codeOut);
			}
			
			if (!context.isInArduino())
			{
				System.out.println(codeOut);
			}		
			context.didGenerate(codeOut);
			
			doSaveIno(codeOut);
						
		}
		
		return success;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		textArea.setBackground(Color.white);
		textArea.setText("");
		textArea.append("\nGenerating Blocks...\n");
		
		if(generateC()) {
			try {
				textArea.append("\nBlocks successfully converted to C Code!\n");
				compileAndUpload();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else {
			textArea.append("\nBlock generation error!\n");
			textArea.setBackground(Color.red);	
		}
	}

	private boolean runArduinoCli() {

		String baud = null;
		String avr_part = null;
		String board = null;
		String selectedBoard = (String) parentFrame.boardOptionsComboBox.getSelectedItem();
		String port = (String) parentFrame.portOptionsComboBox.getSelectedItem();

		upload_cmd_list.clear();
		upload_cmd_list.add(context.getArduinoCliDir());

		//- if Arduino cli is not there, error out.
		arduinoCliDir = context.getArduinoCliDir();
		File directory = new File(arduinoCliDir);
		directory = new File(arduinoCliDir);
		if(!directory.exists()) {
			System.out.println("Cannot find Arduino-cli!");
			textArea.append("\n Arduino-cli not installed!");
			textArea.append("\n Cannot find file: " + arduinoCliDir);
			textArea.setBackground(Color.red);
			JOptionPane.showMessageDialog(parentFrame, "Arduino cli not installed!", "Error", JOptionPane.ERROR_MESSAGE);
			System.out.println("Arduino cli not installed!");
			return false;
		}

		//- Compile first
		upload_cmd_list.add("compile");
		upload_cmd_list.add("--fqbn");

		if (selectedBoard=="Barnabas Noggin") {
			baud = "57600";
			board = "nano";
			avr_part = "atmega328old";
			upload_cmd_list.add("arduino:avr:" + board + ":cpu=" + avr_part);
		}
		else if (selectedBoard == "Arduino Uno") {
			baud = "115200";
			board = "uno";
			upload_cmd_list.add("arduino:avr:" + board);
		}

		upload_cmd_list.add(context.getSketchPath(sketchfilePath));

		//- run compile command first

		//- display compile status
		textArea.append("\nPort: " + port);
		textArea.append("\nBaudRate: " + baud);
		textArea.append("\nBoard: " + board);
		textArea.append("\nAVR Part: " + avr_part);
		textArea.append("\nSketch Name: " + sketchfilePath);
		textArea.append("\nArduino Compile Command: " + upload_cmd_list.toString() +"\n");

		upload_cmd_array = new String[upload_cmd_list.size()];
		upload_cmd_array = upload_cmd_list.toArray(upload_cmd_array);
		Runtime rt = Runtime.getRuntime();
		Process process;
		try {
			process = rt.exec(upload_cmd_array);

			BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			process.waitFor();
			textArea.append("\nFinished compiling!\n");
		} catch (Exception e) {
			e.printStackTrace();
		}

		//- now prepare for upload
		upload_cmd_list.clear();
		upload_cmd_list.add(context.getArduinoCliDir());
		upload_cmd_list.add("upload");
		upload_cmd_list.add("-p");
		upload_cmd_list.add(context.getPortString(port));
		upload_cmd_list.add("--fqbn");

		if (selectedBoard=="Barnabas Noggin") {
			baud = "57600";
			board = "nano";
			avr_part = "atmega328old";
			upload_cmd_list.add("arduino:avr:" + board + ":cpu=" + avr_part);
		}
		else if (selectedBoard == "Arduino Uno") {
			baud = "115200";
			board = "uno";
			upload_cmd_list.add("arduino:avr:" + board);
		}

		upload_cmd_list.add(context.getSketchDir());
		upload_cmd_list.add("--verbose");

		textArea.append("\nArduino Upload Command: " + upload_cmd_list.toString() +"\n");

		return true;
		

	}

	private boolean runArduinoDebug() {

		String baud = null;
		String avr_part = null;
		String board = null;
		String selectedBoard = (String) parentFrame.boardOptionsComboBox.getSelectedItem();
		String port = (String) parentFrame.portOptionsComboBox.getSelectedItem();

		upload_cmd = context.getArduinoCmdLine();
		upload_cmd_list.clear();
		upload_cmd_list.add(context.getArduinoCmdLine());

		//- check to see if arduino command line exists on system.  If not, throw an error
		//- if Arduino file doesn't exit, abort and notify the user to install Arduino first.
		File arduinoFile = new File(upload_cmd);
		if (!arduinoFile.exists()) {
			textArea.append("\n Arduino not installed!");
			textArea.append("\n Cannot find file: " + upload_cmd);
			textArea.setBackground(Color.red);
			JOptionPane.showMessageDialog(parentFrame, "Arduino not installed!  To install, go to: https://www.arduino.cc/en/Main/Software", "Error", JOptionPane.ERROR_MESSAGE);
			System.out.println("Arduino not installed!");
			return false;
		}

		if (selectedBoard=="Barnabas Noggin") {
			baud = "57600";
			board = "nano";
			avr_part = "atmega328old";

			upload_cmd += " --board arduino:avr:" + board + ":cpu=" + avr_part;

			upload_cmd_list.add("--board");
			upload_cmd_list.add("arduino:avr:" + board + ":cpu=" + avr_part);
		}
		else if (selectedBoard == "Arduino Uno") {
			baud = "115200";
			board = "uno";

			upload_cmd += " --board arduino:avr:" + board;

			upload_cmd_list.add("--board");
			upload_cmd_list.add("arduino:avr:" + board);
		}

		//- add "/dev" to port path if on linux or mac
		port = context.getPortString(port);
		
		upload_cmd += " --port " + port;
		upload_cmd_list.add("--port");
		upload_cmd_list.add(port);
		
		//- add quotes to sketch file path to take care of white spaces (only for windows)
		upload_cmd += " --upload " + context.getSketchPath(sketchfilePath);
		upload_cmd_list.add("--upload");
		upload_cmd_list.add(context.getSketchPath(sketchfilePath));
		
		upload_cmd += " --verbose";
		upload_cmd_list.add("--verbose");

		/*******************************/
		/**** Display Upload Status ****/
		/*******************************/
		
		textArea.append("\nPort: " + port);
		textArea.append("\nBaudRate: " + baud);
		textArea.append("\nBoard: " + board);
		textArea.append("\nAVR Part: " + avr_part);
		textArea.append("\nSketch Name: " + sketchfilePath);
		textArea.append("\nArduino Command List: " + upload_cmd_list.toString() +"\n");

		return true;
		
	}
	
	public void compileAndUpload() throws InterruptedException, ExecutionException
	{

		/***************************************************************/
		/**** Settings To Upload To Arduino Nano (Older Bootloader) ****/
		/***************************************************************/
		
		/**Reference for arduino_debug.exe: 
		 * https://github.com/arduino/Arduino/blob/ide-1.5.x/build/shared/manpage.adoc 
		 * */

		//- get port from the drop down list
		String port = (String) parentFrame.portOptionsComboBox.getSelectedItem();
		
		if (port == uiMessageBundle.getString("ardublock.conn_msg.no_conn")) {
			//JOptionPane.showMessageDialog(parentFrame, uiMessageBundle.getString("ardublock.conn_msg.no_port"), "Error", JOptionPane.ERROR_MESSAGE);
			System.out.println(uiMessageBundle.getString("ardublock.conn_msg.no_port"));
			textArea.append("\n" + uiMessageBundle.getString("ardublock.conn_msg.no_port"));
			textArea.setBackground(Color.red);
			return;
		}

        /* start thread here.  It will stop when the user clicks on the X 
         * of the serial monitor window.
         * 
         * If the thread has been started and stopped before, just call doRun(), 
         * otherwise, start the thread since it has never been run before.
         */
        
        //- if there is an existing thread that is alive, then just toggle the run flag by calling doRun()        
		
		//- convert String list to String array

		if (runArduinoDebug()) {
		//if (runArduinoCli()) {
			upload_cmd_array = new String[upload_cmd_list.size()];
			upload_cmd_array = upload_cmd_list.toArray(upload_cmd_array);

			if (uploadThread.isAlive()) {
        		System.out.println("uploadThread is alive!");
				uploadRunnable.upload_cmd = upload_cmd;
				uploadRunnable.upload_cmd_array = upload_cmd_array;
        		uploadRunnable.doRun();
        	}
        	else {
        		//- the current uploadThread is not alive, or not running, so create a new runnable and thread and start it
        		System.out.println("Starting upload thread...");
        	
        		uploadRunnable = new SerialUploadRunnable(parentFrame);
				uploadRunnable.upload_cmd = upload_cmd;
				uploadRunnable.upload_cmd_array = upload_cmd_array;
        	
        		uploadThread = new Thread(uploadRunnable);
        		uploadThread.start(); 
        	}

		}
	}
	
	//- creates a .ino file with corresponding directory inside the executing folder of this .jar 
	public void doSaveIno(String content)
	{	
		try {
			
			//- delete the file first if it exists
			 File newTextFile = new File(sketchfilePath);
			 if (newTextFile.exists()){
				 newTextFile.delete();
				 System.out.println("deleting sketchfile at: " + sketchfilePath);
			 }  
			
			//- create text file
			System.out.println("creating sketchfile at: " + sketchfilePath);
            newTextFile = new File(sketchfilePath);
            FileWriter fw = new FileWriter(newTextFile);
            fw.write(content);
            fw.close();

        } catch (IOException iox) {
            //do stuff with exception
            iox.printStackTrace();
            System.out.println("Error creating sketch file! Error: " + iox.toString());
        }
	}
}

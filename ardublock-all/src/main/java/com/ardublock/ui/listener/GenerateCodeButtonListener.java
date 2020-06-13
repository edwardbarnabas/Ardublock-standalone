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
	private JTextArea textArea;
	
	//- upload file parameters
	private String sketchfileDir;
	private String sketchfilePath;

	public GenerateCodeButtonListener(OpenblocksFrame oframe, Context context)
	{
		this.parentFrame = oframe;
		this.context = context;
		
		this.textArea = oframe.uploadTextArea;
		
		workspace = context.getWorkspaceController().getWorkspace();
		uiMessageBundle = ResourceBundle.getBundle("com/ardublock/block/ardublock");
		
		upload_cmd = null;
		
		uploadRunnable = new SerialUploadRunnable(textArea);
		uploadThread = new Thread(uploadRunnable);
		
		//-create temp_sketch directory in the executing folder of this .jar
		sketchfileDir = System.getProperty("user.dir") + "\\temp_sketch";
		File directory = new File(sketchfileDir);
		if(!directory.exists()) {
			System.out.println("Creating directory");
			directory.mkdir();
		}
		
		//- create file path to the .ino that will be created later.
		sketchfilePath = sketchfileDir + "\\temp_sketch.ino";
		
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
	
	public void compileAndUpload() throws InterruptedException, ExecutionException
	{
		
/*		Cmds for avrdude
		
		String avr_loc="C:\\users\\edwardbarnabas\\dev\\arduino-upload";
		String avr_exe = '"' + avr_loc + "\\tools\\avr\\bin\\avrdude.exe" + '"';
		String conf_file = '"' + avr_loc + "\\tools\\avr\\etc\\avrdude.conf" + '"';
		String hex_file = '"' + avr_loc + "\\blank.hex" + '"';
		String port = "COM5";
		String baud = "57600";
		String avr_part = "atmega328p";
				
		String cmd = avr_exe;
		cmd += " -C" + conf_file;
		cmd += " -v";
		cmd += " -p" + avr_part;
		cmd += " -carduino";
		cmd += " -P" + port;
		cmd += " -b" + baud;
		cmd += " -D";
		cmd += " -Uflash:w:" + hex_file + ":i";
		
*/
		/***************************************************************/
		/**** Settings To Upload To Arduino Nano (Older Bootloader) ****/
		/***************************************************************/
		
		/**Reference for arduino_debug.exe: 
		 * https://github.com/arduino/Arduino/blob/ide-1.5.x/build/shared/manpage.adoc 
		 * */

		//- get port from the drop down list
		String port = (String) parentFrame.portOptions.getSelectedItem();
		
		if (port == uiMessageBundle.getString("ardublock.conn_msg.no_conn")) {
			JOptionPane.showMessageDialog(parentFrame, uiMessageBundle.getString("ardublock.conn_msg.no_port"), "Error", JOptionPane.ERROR_MESSAGE);
			System.out.println(uiMessageBundle.getString("ardublock.conn_msg.no_port"));
			return;
		}
		
		String baud = null;
		String avr_part = null;
		String board = null;
		String selectedBoard = (String) parentFrame.boardOptions.getSelectedItem();
		
		//- call to context object gets the arduino command line path for Mac, Windows or Linux
		upload_cmd = context.getArduinoCmdLine();

		if (selectedBoard=="Barnabas Noggin") {
			baud = "57600";
			board = "nano";
			avr_part = "atmega328old";
			upload_cmd += " --board arduino:avr:" + board + ":cpu=" + avr_part;
		}
		else if (selectedBoard == "Arduino Uno") {
			baud = "115200";
			board = "uno";
			upload_cmd += " --board arduino:avr:" + board;
		}
		
		upload_cmd += " --port " + port;
		upload_cmd += " --upload " + sketchfilePath;
		upload_cmd += " --verbose";
		
		
		/*******************************/
		/**** Display Upload Status ****/
		/*******************************/
		
		textArea.append("\nPort: " + port);
		textArea.append("\nBaudRate: " + baud);
		textArea.append("\nBoard: " + board);
		textArea.append("\nAVR Part: " + avr_part);
		textArea.append("\nSketch Name: " + sketchfilePath);
				
   /*     frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
        frame.setSize(new Dimension(349, 500)); // set the frame size (you'll usually want to call frame.pack())
        frame.setResizable(false);
        
        frame.setLocationRelativeTo(null); // center the frame
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
        	//- run this code when the users closes the window.
        	@Override
            public void windowClosing(WindowEvent e) {
        		
        		//- interrupt thread and stop runnable if it was running
        		if (uploadRunnable.keepRunning()) {
        			uploadRunnable.doStop();
        			uploadThread.interrupt(); //-interrupt thread so that the current process will stop
        		}
        		
        		frame.dispose();
            }
        });
        
        */
        
        /* start thread here.  It will stop when the user clicks on the X 
         * of the serial monitor window.
         * 
         * If the thread has been started and stopped before, just call doRun(), 
         * otherwise, start the thread since it has never been run before.
         */
        
        //- if there is an existing thread that is alive, then just toggle the run flag by calling doRun()        
        if (uploadThread.isAlive()) {
        	System.out.println("uploadThread is alive!");
        	uploadRunnable.upload_cmd = upload_cmd;
        	uploadRunnable.doRun();
        }
        else {
        	//- the current uploadThread is not alive, or not running, so create a new runnable and thread and start it
        	System.out.println("Starting upload thread...");
        	
        	uploadRunnable = new SerialUploadRunnable(textArea);
        	uploadRunnable.upload_cmd = upload_cmd;
        	
        	uploadThread = new Thread(uploadRunnable);
        	uploadThread.start(); 
        }
        
        

	}
	
	//- creates a .ino file with corresponding directory inside the executing folder of this .jar 
	public void doSaveIno(String content)
	{	
		try {
            File newTextFile = new File(sketchfilePath);
            FileWriter fw = new FileWriter(newTextFile);
            fw.write(content);
            fw.close();

        } catch (IOException iox) {
            //do stuff with exception
            iox.printStackTrace();
        }
	}
}

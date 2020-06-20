package com.ardublock.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.ardublock.core.Context;
import com.ardublock.ui.listener.ArdublockWorkspaceListener;
import com.ardublock.ui.listener.GenerateCodeButtonListener;
import com.ardublock.ui.listener.NewButtonListener;
import com.ardublock.ui.listener.OpenButtonListener;
import com.ardublock.ui.listener.OpenblocksFrameListener;
import com.ardublock.ui.listener.SaveAsButtonListener;
import com.ardublock.ui.listener.SaveButtonListener;
import com.ardublock.ui.listener.SerialMonitor;

import edu.mit.blocks.controller.WorkspaceController;
import edu.mit.blocks.workspace.Page;
import edu.mit.blocks.workspace.Workspace;

public class OpenblocksFrame extends JFrame
{

	private static final long serialVersionUID = 2841155965906223806L;
	
	private Context context;
	
	private JFileChooser fileChooser;
	private FileFilter ffilter;
	
	
	//- Serial Monitor 
	public  JFrame serialMonitorframe;
	public JScrollPane serialMonitorScrollPane;
	public SerialMonitor monitor;
	public JTextArea serialMonitortextArea; 
	private SerialMonitorRunnable sm_Runnable;
	private Thread serialMonitorThread;
	
	//- Serial Port Detection
	private SerialPortDetectRunnable spd_Runnable;
	private Thread serialPortDetectThread;
	public JComboBox portOptionsComboBox;
	public DefaultComboBoxModel portOptionsModel;
	public volatile String currentPort;

	//- Serial Port Upload
	public JScrollPane uploadScrollPane;
	public JTextArea uploadTextArea;
	
	//- Porgram Options
	public JComboBox programComboBox;
	
	
	private ResourceBundle uiMessageBundle;
	
	
	public JComboBox<String> boardOptionsComboBox;
	
	
	public void addListener(OpenblocksFrameListener ofl)
	{
		context.registerOpenblocksFrameListener(ofl);
	}
	
	public String makeFrameTitle()
	{
		String title = Context.APP_NAME + " " + context.getSaveFileName();
		if (context.isWorkspaceChanged())
		{
			title = title + " *";
		}
		return title;
		
	}
	
	public OpenblocksFrame()
	{		
		/* Construct main Ardublock window */
		context = Context.getContext();
		this.setTitle(makeFrameTitle());
		//this.setSize(new Dimension(1024, 760));
		this.setSize(new Dimension(1200, 760));
		this.setLayout(new BorderLayout());
		this.setLocationRelativeTo(null);

		uiMessageBundle = ResourceBundle.getBundle("com/ardublock/block/ardublock");
		
		/* File Handling Objects */
		fileChooser = new JFileChooser();
		ffilter = new FileNameExtensionFilter(uiMessageBundle.getString("ardublock.file.suffix"), "abp");
		fileChooser.setFileFilter(ffilter);
		fileChooser.addChoosableFileFilter(ffilter);
		
		/* Serial Monitor Objects */
		serialMonitortextArea = new JTextArea();
		monitor = new SerialMonitor();
		serialMonitorframe = new JFrame("Serial Monitor");
		
		serialMonitorScrollPane = new JScrollPane(serialMonitortextArea);
		sm_Runnable = new SerialMonitorRunnable(this);
	    serialMonitorThread = new Thread(sm_Runnable);
	    
	    /* Serial Upload Objects */
  		uploadTextArea = new JTextArea();
  		uploadTextArea.setEditable(false);
  		uploadTextArea.setLineWrap(true);
  		uploadTextArea.setWrapStyleWord(true);
  		uploadTextArea.setMargin(new Insets(5, 5, 5, 5));
		
  		uploadScrollPane = new JScrollPane(uploadTextArea);
  		uploadScrollPane.setPreferredSize(new Dimension(600,75));
	  
		initOpenBlocks();
	  
	}
	
	public void updateAvailablePorts() {
		
		//- store the currently selected port
		currentPort = (String) portOptionsComboBox.getSelectedItem();
		
		//System.out.println("Detecting ports... Current port: " + currentPort + "...");
		
		//- get current list of devices
		String[] portArray = SerialMonitor.getPorts();
		
		//- if there are no ports, create single element array that has no_conn error message
		if (portArray == null) {
			portArray = new String[] {uiMessageBundle.getString("ardublock.conn_msg.no_conn")};
		}

		List<String> list = Arrays.asList(portArray);
		
		//- remove ports that are no longer there
		for (int i=0;i<portOptionsModel.getSize();i++) {				
			if (!list.contains(portOptionsModel.getElementAt(i))) {
				portOptionsModel.removeElementAt(i);
				System.out.println("removing an element!");
			}
		}
			
		//- add any ports that weren't there before
		
		for (String x: list) {
			
			if (portOptionsModel.getIndexOf(x)==-1) {
				
				if (currentPort == null) {
					portOptionsModel.addElement(x);	
					//System.out.println("Current Port: " + currentPort + ". adding element: " + x + " at the end of the list.");
				}
				else {
					
					//- add port elements in ascending alphabetical order
					for (int i=0; i<portOptionsModel.getSize();i++) {
						if (x.compareTo((String) portOptionsModel.getElementAt(i)) < 0) {
							
							int index;
		
							if (i == 0) index = 0;
							else index = i-1;
							
							portOptionsModel.insertElementAt(x, index);
							
							System.out.println("Current Port: " + currentPort + ". inserting element: " + x + "at index: " + index);
							
							break; //- break out of for loop iteration
						}
						//- if at the end of the list, just add it
						if (i==portOptionsModel.getSize()-1) {
							portOptionsModel.addElement(x);	
							break;
						}
				
					}
					
				}
				
			}
		}
		
		//- make sure that same device is selected if it's still there from last time we checked for devices
		if(portOptionsModel.getIndexOf(currentPort) == -1) {
			//- set selected item to the first element
			portOptionsModel.setSelectedItem(portOptionsModel.getElementAt(0));
			System.out.println("Device is gone! Selecting a new device.");
		}
		else {
			//- do nothing.  note: reselecting the device will prevent you from being able to select a device from GUI at times.
			//System.out.println("Device is still here!");
		}
	
	}
	
	private void initOpenBlocks()
	{
		
		final Context context = Context.getContext();
		
		//- get block workspace object from context.  
		final Workspace workspace = context.getWorkspace();
		
		workspace.addWorkspaceListener(new ArdublockWorkspaceListener(this));
		
		/*********************************************/
		/**** Generate Buttons On Top Of Window ******/
		/*********************************************/
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout());
		
		JButton newButton = new JButton(uiMessageBundle.getString("ardublock.ui.new"));
		newButton.addActionListener(new NewButtonListener(this));
		JButton saveButton = new JButton(uiMessageBundle.getString("ardublock.ui.save"));
		saveButton.addActionListener(new SaveButtonListener(this));
		JButton saveAsButton = new JButton(uiMessageBundle.getString("ardublock.ui.saveAs"));
		saveAsButton.addActionListener(new SaveAsButtonListener(this));
		JButton openButton = new JButton(uiMessageBundle.getString("ardublock.ui.load"));
		openButton.addActionListener(new OpenButtonListener(this));
		
		JButton generateButton = new JButton(uiMessageBundle.getString("ardublock.ui.upload"));
		generateButton.addActionListener(new GenerateCodeButtonListener(this, context));
		
		
		JButton serialMonitorButton = new JButton(uiMessageBundle.getString("ardublock.ui.serialMonitor"));
		serialMonitorButton.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				openSerialMonitor();
			}
		});
		
		JButton saveImageButton = new JButton(uiMessageBundle.getString("ardublock.ui.saveImage"));
		saveImageButton.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				Dimension size = workspace.getCanvasSize();
				System.out.println("size: " + size);
				BufferedImage bi = new BufferedImage(2560, 2560, BufferedImage.TYPE_INT_RGB);
				Graphics2D g = (Graphics2D)bi.createGraphics();
				double theScaleFactor = (300d/72d);  
				g.scale(theScaleFactor,theScaleFactor);
				
				workspace.getBlockCanvas().getPageAt(0).getJComponent().paint(g);
				try{
					final JFileChooser fc = new JFileChooser();
					fc.setSelectedFile(new File("ardublock.png"));
					int returnVal = fc.showSaveDialog(workspace.getBlockCanvas().getJComponent());
			        if (returnVal == JFileChooser.APPROVE_OPTION) {
			            File file = fc.getSelectedFile();
						ImageIO.write(bi,"png",file);
			        }
				} catch (Exception e1) {
					
				} finally {
					g.dispose();
				}
			}
		});

		/* get port */
		
		portOptionsModel = new DefaultComboBoxModel();
		portOptionsComboBox= new JComboBox(portOptionsModel);
		
		currentPort = null;
		updateAvailablePorts();
		
		/* continuously look for new hardware connections */
		spd_Runnable = new SerialPortDetectRunnable(this);
	    serialPortDetectThread = new Thread(spd_Runnable);
	    serialPortDetectThread.start(); 

		/* add boards */
		String[] boardList = {"Barnabas Noggin", "Arduino Uno"};
		boardOptionsComboBox = new JComboBox<String>(boardList);
		
		/* Zoom level of Ardublock */
		
		JLabel zoomLabel = new JLabel("Zoom: ");
		
		JButton zoomIn = new JButton("+");
		zoomIn.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				double zoom = Page.getZoomLevel();
				zoom -= 0.1;
				Page.setZoomLevel(zoom);
		        System.out.println("Zoom level: " + Page.getZoomLevel());
			}
		});
		
		JButton zoomOut = new JButton("-");
		zoomOut.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				double zoom = Page.getZoomLevel();
				zoom += 0.1;
				Page.setZoomLevel(zoom);
		        System.out.println("Zoom level: " + Page.getZoomLevel());
			}
		});
		
		/* add robot projects */
		String[] programList = {"Barnabas Bot", "Barnabas Racer"};
		programComboBox = new JComboBox<String>(programList);
		programComboBox.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				System.out.println("item state changed to: " + programComboBox.getSelectedItem() + ".  Current Ardublock Version = " + context.ArdublockVersion);
				
				//- ignore if selected program is same as the current one. 
				if (context.ArdublockVersion == programComboBox.getSelectedItem()) {
					System.out.println("skipped");
					return;
				}
				else {
					changeProgramSpace();
				}
			}
			
		});
		
		JButton websiteButton = new JButton(uiMessageBundle.getString("ardublock.ui.website"));
		websiteButton.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
			    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
			    URL url;
			    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			        try {
						url = new URL("https://www.barnabasrobotics.com");
			            desktop.browse(url.toURI());
			        } catch (Exception e1) {
			            e1.printStackTrace();
			        }
			    }
			}
		});
		
		JPanel jp1 = new JPanel();
		JPanel jp2 = new JPanel();
		JPanel jp3 = new JPanel();
		JPanel jp4 = new JPanel();
		JPanel jp5 = new JPanel();
		
		jp1.add(newButton);
		jp1.add(saveButton);
		jp1.add(saveImageButton);
		
		jp1.add(openButton);
		jp1.add(saveAsButton);
		jp1.add(websiteButton);
		
		jp1.setLayout(new GridLayout(2,3));
		
		jp2.add(new JLabel("Select Port"));
		jp2.add(portOptionsComboBox);
		jp2.setLayout(new GridLayout(2,1));
		
		jp3.add(new JLabel("Select Hardware"));
		jp3.add(boardOptionsComboBox);
		jp3.setLayout(new GridLayout(2,1));
		
		jp4.add(new JLabel("Select Project"));
		jp4.add(programComboBox);
		jp4.setLayout(new GridLayout(2,1));
		
		jp5.add(generateButton);
		jp5.add(serialMonitorButton);
		jp5.setLayout(new GridLayout(2,1));
		
		
		topPanel.add(jp1);
		topPanel.add(jp2);
		topPanel.add(jp3);
		topPanel.add(jp4);
		topPanel.add(jp5);
		
		/*********************************************/
		/**** Generate Bottom Panel Of The Window ****/
		/*********************************************/
		
		JPanel bottomPanel = new JPanel();
		JLabel versionLabel = new JLabel("Version: " + uiMessageBundle.getString("ardublock.ui.version"));
		
		bottomPanel.add(new JLabel("Upload Status: "));
		bottomPanel.add(uploadScrollPane);
		bottomPanel.add(versionLabel);
		 

		/***** Position Items On Window ******/
		this.add(topPanel, BorderLayout.NORTH);
		this.add(bottomPanel, BorderLayout.SOUTH);
		
		/***** Add block work area to window ***/
		this.add(workspace, BorderLayout.CENTER);
		
	}
	
	public void doOpenArduBlockFile()
	{
		if (context.isWorkspaceChanged())
		{
			int optionValue = JOptionPane.showOptionDialog(this, uiMessageBundle.getString("message.content.open_unsaved"), uiMessageBundle.getString("message.title.question"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.YES_OPTION);
			if (optionValue == JOptionPane.YES_OPTION)
			{
				doSaveArduBlockFile();
				this.loadFile();
			}
			else
			{
				if (optionValue == JOptionPane.NO_OPTION)
				{
					this.loadFile();
				}
			}
		}
		else
		{
			
			this.loadFile();
		}
		this.setTitle(makeFrameTitle());
	}
	
	private void loadFile()
	{
		int result = fileChooser.showOpenDialog(this);
		
		System.out.println("Opening Ardublock File...");
		
		if (result == JFileChooser.APPROVE_OPTION)
		{
			File savedFile = fileChooser.getSelectedFile();
			if (!savedFile.exists())
			{
				JOptionPane.showOptionDialog(this, uiMessageBundle.getString("message.file_not_found"), uiMessageBundle.getString("message.title.error"), JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, null, JOptionPane.OK_OPTION);
				return ;
			}
			
			try
			{
				this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				context.loadArduBlockFile(savedFile,this);
				context.setWorkspaceChanged(false);
				
			}
			catch (IOException e)
			{
				JOptionPane.showOptionDialog(this, uiMessageBundle.getString("message.file_not_found"), uiMessageBundle.getString("message.title.error"), JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE, null, null, JOptionPane.OK_OPTION);
				e.printStackTrace();
			}
			finally
			{
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				
			}
		}
	}
	
	public void doSaveArduBlockFile()
	{
		if (!context.isWorkspaceChanged())
		{
			return ;
		}
		
		String saveString = getArduBlockString();
		
		if (context.getSaveFilePath() == null)
		{
			chooseFileAndSave(saveString);
		}
		else
		{
			File saveFile = new File(context.getSaveFilePath());
			writeFileAndUpdateFrame(saveString, saveFile);
		}
	}
	
	
	public void doSaveAsArduBlockFile()
	{
		if (context.isWorkspaceEmpty())
		{
			return ;
		}
		
		String saveString = getArduBlockString();
		
		chooseFileAndSave(saveString);
		
	}
	
	private void chooseFileAndSave(String ardublockString)
	{
		File saveFile = letUserChooseSaveFile();
		saveFile = checkFileSuffix(saveFile);
		if (saveFile == null)
		{
			return ;
		}
		
		if (saveFile.exists() && !askUserOverwriteExistedFile())
		{
			return ;
		}
		
		writeFileAndUpdateFrame(ardublockString, saveFile);
	}
	
	private String getArduBlockString()
	{
		WorkspaceController workspaceController = context.getWorkspaceController();
		return workspaceController.getSaveString();
	}
	
	private void writeFileAndUpdateFrame(String ardublockString, File saveFile) 
	{
		try
		{
			saveArduBlockToFile(ardublockString, saveFile);
			context.setWorkspaceChanged(false);
			this.setTitle(this.makeFrameTitle());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
	}
	
	private File letUserChooseSaveFile()
	{
		int chooseResult;
		chooseResult = fileChooser.showSaveDialog(this);
		if (chooseResult == JFileChooser.APPROVE_OPTION)
		{
			return fileChooser.getSelectedFile();
		}
		return null;
	}
	
	private boolean askUserOverwriteExistedFile()
	{
		int optionValue = JOptionPane.showOptionDialog(this, uiMessageBundle.getString("message.content.overwrite"), uiMessageBundle.getString("message.title.question"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.YES_OPTION);
		return (optionValue == JOptionPane.YES_OPTION);
	}
	
	private void saveArduBlockToFile(String ardublockString, File saveFile) throws IOException
	{
		context.saveArduBlockFile(saveFile, ardublockString);
		context.setSaveFileName(saveFile.getName());
		context.setSaveFilePath(saveFile.getAbsolutePath());
	}
	
	public void doNewArduBlockFile()
	{
		if (context.isWorkspaceChanged())
		{
			int optionValue = JOptionPane.showOptionDialog(this, uiMessageBundle.getString("message.question.newfile_on_workspace_changed"), uiMessageBundle.getString("message.title.question"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.YES_OPTION);
			if (optionValue != JOptionPane.YES_OPTION)
			{
				return ;
			}
		}
		this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		context.resetWorkspace();
		context.setWorkspaceChanged(false);
		this.setTitle(this.makeFrameTitle());
		this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}
	
	public void changeProgramSpace()
	{
	
		int optionValue = JOptionPane.showOptionDialog(this, uiMessageBundle.getString("message.question.changeworkspace_on_workspace_changed"), uiMessageBundle.getString("message.title.question"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.YES_OPTION);
		if (optionValue == JOptionPane.NO_OPTION)
		{
			//- change combobox selection back to the current version.
			programComboBox.setSelectedItem((String)context.ArdublockVersion);
			System.out.println("Setting combo box back to original: " + context.ArdublockVersion);
			return;
		}
		else {
			context.ArdublockVersion = (String) programComboBox.getSelectedItem();
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			context.resetWorkspace();
			context.setWorkspaceChanged(false);
			this.setTitle(this.makeFrameTitle());
			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

		}
	}

	private File checkFileSuffix(File saveFile)
	{
		String filePath = saveFile.getAbsolutePath();
		if (filePath.endsWith(".abp"))
		{
			return saveFile;
		}
		else
		{
			return new File(filePath + ".abp");
		}
	}
	
	private void openSerialMonitor() {
		
        String port = (String) portOptionsComboBox.getSelectedItem();
        monitor.selectedPort = port;
        monitor.open();
        
        String strInit = null;
        
        strInit = "Initializing Serial Monitor...\n";
		strInit += "Selected Port: " + monitor.selectedPort + "\n";
		strInit += "Selected BaudRate: " + monitor.selectedBaud + "\n\n";
		
		strInit += "****************************************************************\n";

		serialMonitortextArea.setText(strInit);
		
		serialMonitorframe.getContentPane().add(serialMonitorScrollPane, BorderLayout.CENTER);
		serialMonitorframe.setSize(new Dimension(500, 240)); // set the frame size (you'll usually want to call frame.pack())
		serialMonitorframe.setLocationRelativeTo(null); // center the frame
		serialMonitorframe.setVisible(true);
		serialMonitorframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		serialMonitorframe.addWindowListener(new WindowAdapter() {
        	//- run this code when the users closes the window.
        	@Override
            public void windowClosing(WindowEvent e) {
        		monitor.close();
        		sm_Runnable.doStop();
        		serialMonitorframe.dispose();
            }
        });
        
        /* start thread here.  It will stop when the user clicks on the X 
         * of the serial monitor window.
         * 
         * If the thread has been started and stopped before, just call doRun(), 
         * otherwise, start the thread since it has never been run before.
         */
        if (sm_Runnable.keepRunning() == false) {
        	sm_Runnable.doRun();
        }
        else {
        	serialMonitorThread.start();        
        }
	}
	
	  

}



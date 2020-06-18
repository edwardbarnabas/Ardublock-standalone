package com.ardublock.core;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.Popup;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import processing.app.Editor;

import com.ardublock.ui.listener.OpenblocksFrameListener;

import edu.mit.blocks.codeblocks.Block;
import edu.mit.blocks.controller.WorkspaceController;
import edu.mit.blocks.renderable.BlockUtilities;
import edu.mit.blocks.renderable.FactoryRenderableBlock;
import edu.mit.blocks.renderable.RenderableBlock;
import edu.mit.blocks.workspace.FactoryManager;
import edu.mit.blocks.workspace.Page;
import edu.mit.blocks.workspace.Workspace;

public class Context
{
	/* select the version of ardublock that you would like to compile 
	 * 
	 * ArdublockVersion = all - includes all libraries
	 * ArdublockVersion = bot - Barnabas-Bot only
	 * 
	 * 
	 * 
	 */
	final public String ArdublockVersion = "all";
	//final public String ArdublockVersion = "bot";
	
	public final static String LANG_DTD_PATH = "/com/ardublock/block/lang_def.dtd";
	
	
	public static String ARDUBLOCK_LANG_PATH;
	
	
	public final static String DEFAULT_ARDUBLOCK_PROGRAM_PATH = "/com/ardublock/default.abp";
	public final static String ARDUINO_VERSION_UNKNOWN = "unknown";
	public final boolean isNeedAutoFormat = true;
	
	private static Context singletonContext;
	
	private boolean workspaceChanged;
	private boolean workspaceEmpty;
	
	private Set<RenderableBlock> highlightBlockSet;
	private Set<OpenblocksFrameListener> ofls;
	private boolean isInArduino = false;
	private String arduinoVersionString = ARDUINO_VERSION_UNKNOWN;
	private OsType osType; 

	public static String APP_NAME;

	private Editor editor;
	
	public enum OsType
	{
		LINUX,
		MAC,
		WINDOWS,
		UNKNOWN,
	};
	
	private String saveFilePath;
	private String saveFileName;
	
	//- return port string based on os
	public String getPortString(String detectedPort) {
		if (osType.equals(OsType.WINDOWS)) {
			//- no need to append anything
		}
		else if (osType.equals(OsType.MAC)) {
			detectedPort = "/dev/" + detectedPort;
		}
		else if (osType.equals(OsType.LINUX)) {
			detectedPort = "/dev/" + detectedPort;
		}
		else {
			detectedPort = "/dev/" + detectedPort;
		}

		return detectedPort;

	}

	//- return sketch name based on ino
	public String getSketchName() {

		String path = null;

		if (osType.equals(OsType.WINDOWS)) {
			path = "\\temp_sketch.ino";
		}
		else if (osType.equals(OsType.MAC)) {
			path = "/temp_sketch.ino";
		}
		else if (osType.equals(OsType.LINUX)) {
			path = "/temp_sketch.ino";
		}
		else {
			path = "/temp_sketch.ino";
		}
	
		return path;

	}

	public String getSketchDir() {

		String path = System.getProperty("user.dir");

		if (osType.equals(OsType.WINDOWS)) {
			path += "\\temp_sketch";
		}
		else if (osType.equals(OsType.MAC)) {
			path += "/temp_sketch";
		}
		else if (osType.equals(OsType.LINUX)) {
			path += "/temp_sketch";
		}
		else {
			path += "/temp_sketch";
		}
	
		return path;

	}
	
	//- returns the arduino command line for uploading and compiling depending
	//- on the OS
	public String getArduinoCmdLine() {
		
		String path = null;
		
		if (osType.equals(OsType.WINDOWS)) {
			path = "C:\\Program Files (x86)\\Arduino\\arduino_debug.exe";
		}
		else if (osType.equals(OsType.MAC)) {
			path = "/Applications/Arduino.app/Contents/MacOS/Arduino";
		}
		else if (osType.equals(OsType.LINUX)) {
			path = "$HOME/arduino-1.8.12/";
		}
		else {
			path = "Unknown OS.  I don't know the path!";
		}
	
		return path;
				
	}
	
	public static Context getContext()
	{
		if (singletonContext == null)
		{
			synchronized (Context.class)
			{
				if (singletonContext == null)
				{
					singletonContext = new Context();
				}
			}
		}
		return singletonContext;
	}
	
	private WorkspaceController workspaceController;
	private Workspace workspace;
	
	private Context()
	{
		
		switch(ArdublockVersion) {
		  case "all":
			  APP_NAME = "ArduBlock";
			  ARDUBLOCK_LANG_PATH = "/com/ardublock/block/ardublock.xml";
		    break;
		  case "bot":
			  APP_NAME = "Ardublock: Barnabas-Bot";
			  ARDUBLOCK_LANG_PATH = "/com/ardublock/block/ardublock_bot.xml";
		    break;
		  default:
			  APP_NAME = "Ardublock";
			  ARDUBLOCK_LANG_PATH = "/com/ardublock/block/ardublock.xml";
		}
		
		workspaceController = new WorkspaceController();
		
		resetWorksapce();
		
		workspace = workspaceController.getWorkspace();
		workspaceChanged = false;
		highlightBlockSet = new HashSet<RenderableBlock>();
		ofls = new HashSet<OpenblocksFrameListener>();
		this.workspace = workspaceController.getWorkspace();
		
		isInArduino = false;
		
		osType = determineOsType();
		
		
	}
	
	public void resetWorksapce()
	{

		// Style list
		List<String[]> list = new ArrayList<String[]>();
		String[][] styles = {};

		for (String[] style : styles) {
			list.add(style);
		}
		workspaceController.resetWorkspace();
		workspaceController.resetLanguage();
		workspaceController.setLangResourceBundle(ResourceBundle.getBundle("com/ardublock/block/ardublock"));
		workspaceController.setStyleList(list);
		workspaceController.setLangDefDtd(this.getClass().getResourceAsStream(LANG_DTD_PATH));
		
		workspaceController.setLangDefStream(this.getClass().getResourceAsStream(ARDUBLOCK_LANG_PATH));
		
		
		workspaceController.loadFreshWorkspace();
		
		loadDefaultArdublockProgram();
		
		saveFilePath = null;
		saveFileName = "untitled";
		workspaceEmpty = true;
	}
	
	private void loadDefaultArdublockProgram()
	{
		
		Workspace workspace = workspaceController.getWorkspace();
		Page page = workspace.getPageNamed("Ardublock Workspace");
		
		FactoryManager manager = workspace.getFactoryManager();
		Block newBlock;
		
		//- add empty loop block
        newBlock = new Block(workspace, "loop", false);
        FactoryRenderableBlock factoryRenderableBlock = new FactoryRenderableBlock(workspace, manager, newBlock.getBlockID());
        RenderableBlock renderableBlock = factoryRenderableBlock.createNewInstance();
        renderableBlock.setLocation(100, 100);
        
        //- set how zoomed in the workspace is.
        //- < 1 = zoomed in, > 1 = zoomed out
        
        Page.setZoomLevel(2.5);
        System.out.println("Zoom level: " + Page.getZoomLevel());
        
        page.addBlock(renderableBlock);
        
        
        
	}
	
	//determine OS
	private OsType determineOsType()
	{
		String osName = System.getProperty("os.name");
		osName = osName.toLowerCase();

		if (osName.contains("win"))
		{
			return Context.OsType.WINDOWS;
		}
		if (osName.contains("linux"))
		{
			return Context.OsType.LINUX;
		}
		if(osName.contains("mac"))
		{
			return Context.OsType.MAC;
		}
		return Context.OsType.UNKNOWN;
	}
	
	
	public File getArduinoFile(String name)
	{
		String path = System.getProperty("user.dir");
		if (osType.equals(OsType.MAC))
		{
			String javaroot = System.getProperty("javaroot");
			if (javaroot != null)
			{
				path = javaroot;
			}
		}
		File workingDir = new File(path);
		return new File(workingDir, name);
	}

	public WorkspaceController getWorkspaceController() {
		return workspaceController;
	}
	
	public Workspace getWorkspace()
	{
		return workspace;
	}

	public boolean isWorkspaceChanged()
	{
		return workspaceChanged;
	}

	public void setWorkspaceChanged(boolean workspaceChanged) {
		this.workspaceChanged = workspaceChanged;
	}
	
	public void highlightBlock(RenderableBlock block)
	{
		block.updateInSearchResults(true);
		highlightBlockSet.add(block);
	}
	
	public void cancelHighlightBlock(RenderableBlock block)
	{
		block.updateInSearchResults(false);
		highlightBlockSet.remove(block);
	}
	
	public void resetHightlightBlock()
	{
		for (RenderableBlock rb : highlightBlockSet)
		{
			rb.updateInSearchResults(false);
		}
		highlightBlockSet.clear();
	}
	
	public void saveArduBlockFile(File saveFile, String saveString) throws IOException
	{
		if (!saveFile.exists())
		{
			saveFile.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(saveFile, false);
		fos.write(saveString.getBytes("UTF8"));
		fos.flush();
		fos.close();
		didSave();
	}
	
	public void loadArduBlockFile(File savedFile) throws IOException
	{
		if (savedFile != null)
		{
			saveFilePath = savedFile.getAbsolutePath();
			saveFileName = savedFile.getName();
			workspaceController.resetWorkspace();
			workspaceController.loadProjectFromPath(saveFilePath);
			didLoad();
		}
	}
	
	public void setEditor(Editor e) {
		editor = e;
	}
	
	public Editor getEditor() {
		return editor;
	}
	
	
	public boolean isInArduino() {
		return isInArduino;
	}

	public void setInArduino(boolean isInArduino) {
		this.isInArduino = isInArduino;
	}

	public String getArduinoVersionString() {
		return arduinoVersionString;
	}

	public void setArduinoVersionString(String arduinoVersionString) {
		this.arduinoVersionString = arduinoVersionString;
	}

	public OsType getOsType() {
		return osType;
	}

	public void registerOpenblocksFrameListener(OpenblocksFrameListener ofl)
	{
		ofls.add(ofl);
	}
	
	public void didSave()
	{
		for (OpenblocksFrameListener ofl : ofls)
		{
			ofl.didSave();
		}
	}
	
	public void didLoad()
	{
		for (OpenblocksFrameListener ofl : ofls)
		{
			ofl.didLoad();
		}
	}
	
	public void didGenerate(String sourcecode)
	{
		for (OpenblocksFrameListener ofl : ofls)
		{
			ofl.didGenerate(sourcecode);
		}
	}

	public String getSaveFileName() {
		return saveFileName;
	}

	public void setSaveFileName(String saveFileName) {
		this.saveFileName = saveFileName;
	}

	public String getSaveFilePath() {
		return saveFilePath;
	}

	public void setSaveFilePath(String saveFilePath) {
		this.saveFilePath = saveFilePath;
	}

	public boolean isWorkspaceEmpty() {
		return workspaceEmpty;
	}

	public void setWorkspaceEmpty(boolean workspaceEmpty) {
		this.workspaceEmpty = workspaceEmpty;
	}
}

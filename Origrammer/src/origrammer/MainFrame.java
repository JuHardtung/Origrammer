package origrammer;
import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;

import origrammer.Constants.NewStepOptions;

public class MainFrame extends JFrame implements ActionListener, ComponentListener, WindowListener {
	public MainScreen mainScreen;
	public UITopPanel uiTopPanel;
	public UISidePanel uiSidePanel;
	public UIStepOverviewPanel uiStepOverviewPanel;
	public UIBottomPanel uiBottomPanel;
	
	private JMenu menuFile = new JMenu("File");
	private JMenuItem menuItemNew = new JMenuItem("New");
	private JMenuItem menuItemOpen = new JMenuItem("Open File");
	private JMenuItem menuItemSave = new JMenuItem("Save File");
	private JMenuItem menuItemExport = new JMenuItem("Export");
	
	private JMenu menuEdit = new JMenu("Edit");
	private JMenuItem menuItemUndo = new JMenuItem("Undo");
	private JMenuItem menuItemRedo = new JMenuItem("Redo");
	private JMenuItem menuItemCut = new JMenuItem("Cut");
	private JMenuItem menuItemCopy = new JMenuItem("Copy");
	private JMenuItem menuItemPaste = new JMenuItem("Paste");
	private JMenuItem menuItemDeleteSelected = new JMenuItem("Delete selection");
	private JMenuItem menutItemModelPreferences = new JMenuItem("Model Preferences");
	private JMenuItem menuItemOrigrammerPreferences = new JMenuItem("Origrammer Preferences");
	
	private JMenu menuObject = new JMenu("Object");
	private JMenu menuType = new JMenu("Type");
	
	private JMenu menuSelect = new JMenu("Select");
	private JMenuItem menuItemSelectAll = new JMenuItem("Select All");
	private JMenuItem menuItemUnselectAll = new JMenuItem("Unselect All");
	
	//private JMenu menuHelp = new JMenu("Help");
	private JMenu menuAbout = new JMenu("About");
	
	private String lastPath = "";
	public ArrayList<String> mruFiles = new ArrayList<>(); //MostRecentlyUsedFiles
	private JMenuItem[] mruFilesMenuItem = new JMenuItem[Config.MRUFILE_NUM];
	

	MainFrame(){
		mainScreen = new MainScreen();
		addWindowListener(this);

		uiTopPanel = new UITopPanel(mainScreen);
		uiSidePanel = new UISidePanel(mainScreen, uiTopPanel);
		uiStepOverviewPanel = new UIStepOverviewPanel();
		uiBottomPanel = new UIBottomPanel(mainScreen);

		try {
			BufferedImage img = ImageIO.read(new File("./images/origrammer.gif"));
			this.setIconImage(img);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(uiTopPanel, BorderLayout.PAGE_START);
		getContentPane().add(uiSidePanel, BorderLayout.LINE_START);
		getContentPane().add(mainScreen, BorderLayout.CENTER);
		getContentPane().add(uiStepOverviewPanel, BorderLayout.LINE_END);
		getContentPane().add(uiBottomPanel, BorderLayout.PAGE_END);

		pack();
		setVisible(true);
		setTitle(Origrammer.res.getString("Title_long") + Origrammer.VERSION);

		//setSize(1010, 800);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		initMainScreen();

		menuItemNew.addActionListener(this);
		menuItemOpen.addActionListener(this);  
		menuItemSave.addActionListener(this);
		menuItemExport.addActionListener(this);

		menuItemUndo.addActionListener(this);
		menuItemUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK));

		menuItemRedo.addActionListener(this);
		menuItemCut.addActionListener(this);
		menuItemCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_MASK));
	
		menuItemCopy.addActionListener(this);
		menuItemCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK));
		
		menuItemPaste.addActionListener(this);
		menuItemPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_MASK));

		menuItemDeleteSelected.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Origrammer.diagram.steps.get(Globals.currentStep).deleteAllSelectedObjects();
				mainScreen.repaint();
			}
		});
		menuItemDeleteSelected.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		menutItemModelPreferences.addActionListener(this);
		menuItemOrigrammerPreferences.addActionListener(this);

		menuItemSelectAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Origrammer.diagram.steps.get(Globals.currentStep).selectAll();
				Globals.toolbarMode = Constants.ToolbarMode.SELECTION_TOOL;
				uiSidePanel.selectionToolRB.setSelected(true);
				uiSidePanel.modeChanged();
				mainScreen.repaint();
			}
		});
		menuItemSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));

		menuItemUnselectAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Origrammer.diagram.steps.get(Globals.currentStep).unselectAll();
				Globals.toolbarMode = Constants.ToolbarMode.SELECTION_TOOL;
				uiSidePanel.selectionToolRB.setSelected(true);
				uiSidePanel.modeChanged();
				mainScreen.repaint();
			}
		});
		menuItemUnselectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));

		menuAbout.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				openAboutDialog();
			}

			@Override
			public void menuCanceled(MenuEvent e) {				
			}
			@Override
			public void menuDeselected(MenuEvent e) {				
			}
		});
		
		for (int i = 0; i < Config.MRUFILE_NUM; i++) {
			mruFilesMenuItem[i] = new JMenuItem();
			mruFilesMenuItem[i].addActionListener(this);
        }

		//##### MENU BAR #####
		JMenuBar menuBar = new JMenuBar();
		buildMenuFile();
		buildMenuEdit();
		buildMenuSelect();

		menuBar.add(menuFile);
		menuBar.add(menuEdit);
		//        menuBar.add(menuObject);
		//        menuBar.add(menuType); //TODO: object and type menuItem
		menuBar.add(menuSelect);
		menuBar.add(menuAbout);
		setJMenuBar(menuBar);
		
	}

	private void buildMenuFile() {
		menuFile.removeAll();

		menuFile.add(menuItemNew);
		menuFile.add(menuItemOpen);
		menuFile.addSeparator();
		menuFile.add(menuItemSave);
		menuFile.add(menuItemExport);

		        for (int i=0; i<Config.MRUFILE_NUM; i++) {
		        	int index = mruFiles.size() - 1 - i;
		        	if (index >= 0) {
		        		String path = mruFiles.get(index);
		        		mruFilesMenuItem[i].setText(path);
		        		menuFile.add(mruFilesMenuItem[i]);
		        	} else {
		        		mruFilesMenuItem[i].setText("");
		        	}
		        }
	}
	
	private void buildMenuEdit() {
		menuEdit.removeAll();
		
		menuEdit.add(menuItemUndo);
		menuEdit.add(menuItemRedo);
		menuEdit.addSeparator();
		menuEdit.add(menuItemCut);
		menuEdit.add(menuItemCopy);
		menuEdit.add(menuItemPaste);
		menuEdit.add(menuItemDeleteSelected);
		menuEdit.addSeparator();
		menuEdit.add(menutItemModelPreferences);
		menuEdit.add(menuItemOrigrammerPreferences);
	}
	
	private void buildMenuSelect() {
		menuSelect.removeAll();
		
		menuSelect.add(menuItemSelectAll);
		menuSelect.add(menuItemUnselectAll);
	}

	private void initMainScreen() {
		add(mainScreen);
	}
	
	public void updateUI() {
		uiTopPanel.modeChanged();
		uiSidePanel.modeChanged();
		uiBottomPanel.stepChanged();
	}
	
	
	public void updateMenu(String filePath) {
		if (mruFiles.contains(filePath)) {
			return;
		}
		
		mruFiles.add(filePath);
		buildMenuFile();
	}
	
	private void openFile() {
		NewStepOptions lastNewStepOptions = Globals.newStepOptions;
		Globals.newStepOptions = Constants.NewStepOptions.EMPTY_STEP;
		Globals.currentStep = 0;

		FileDialog fd = new FileDialog(this, "Choose a file", FileDialog.LOAD);
		fd.setDirectory("C:\\Desktop");
		fd.setFile("*.xml");
		fd.setVisible(true);
		
		String filename = fd.getFile();
		if (filename == null)  {
			System.out.println("You cancelled the choice");
		} else {
			System.out.println("You chose " + filename);
			String filePath = fd.getDirectory() + filename;
			openFile(filePath);
			updateMenu(filePath);
			
			for (int i=0; i<Origrammer.diagram.steps.size(); i++) {
				Globals.currentStep = i;
				uiStepOverviewPanel.createStepPreview();
			}
			uiStepOverviewPanel.updateStepOverViewPanel();
			mainScreen.repaint();
			uiBottomPanel.stepChanged();
			lastPath = filePath;
		}
		Globals.newStepOptions = lastNewStepOptions;
	}

	private void openFile(String filePath) {
		Origrammer.mainFrame.uiSidePanel.dispGridCheckBox.setSelected(false);
		Origrammer.mainFrame.mainScreen.setDispGrid(false);

		XMLEncoderDecoder xmlEncoderDecoder = new XMLEncoderDecoder();
		DiagramDataSet diaDataSet = xmlEncoderDecoder.deserializeFromXML(filePath);
		if (diaDataSet == null) {
			return;
		}

		Diagram diagram = new Diagram();
		diaDataSet.recover(diagram);
		Origrammer.diagram = diagram;
		Origrammer.diagram.dataFilePath = filePath;
	}
	
	private void saveFile() {
		FileDialog fd = new FileDialog(this, "Save", FileDialog.SAVE);
		fd.setDirectory("C:\\Desktop");
		fd.setFile(".xml");
		fd.setVisible(true);
		
		String filePath = fd.getDirectory();
		if (filePath == null)  {
			System.out.println("You cancelled the choice");
		} else {
			String fileName = fd.getFile();
			if (!fileName.endsWith(".xml")) {
				fileName += ".xml";
			}
			File file = new File(filePath+fileName);
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(
						null, Origrammer.res.getString("Warning_SameNameFileExists"),
						Origrammer.res.getString("DialogTitle_FileSave"),
						JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
					return;
				}
			}
			saveFile(filePath + fileName);
			updateMenu(filePath + fileName);
			lastPath = filePath;
		}
	}
	
	private void saveFile(String filePath) {
		XMLEncoderDecoder exporter = new XMLEncoderDecoder();
		DiagramDataSet diagramData = new DiagramDataSet(Origrammer.diagram);
		exporter.serializeToXML(diagramData, filePath);
		Origrammer.diagram.dataFilePath = filePath;
	}
	
	
	public void updateTitleText() {
		String fileName;
		if ((Origrammer.diagram.dataFilePath).equals("")) {
			fileName = Origrammer.res.getString("DefaultFileName");
		} else {
			File file = new File(Origrammer.diagram.dataFilePath);
			fileName = file.getName();
		}
		setTitle(fileName + " - " + Origrammer.res.getString("Title_short") + Origrammer.VERSION);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		//check the last opened files
		for (int i = 0; i < Config.MRUFILE_NUM; i++) {
			if (e.getSource() == mruFilesMenuItem[i]) {
				try {
					String filePath = mruFilesMenuItem[i].getText();
					openFile(filePath);
					updateMenu(filePath);
					updateTitleText();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(
							this, e.toString(), Origrammer.res.getString("Error_FileLoadFailed"),
									JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
				mainScreen.repaint();
				return;
			}
		}
		if (e.getSource() == menuItemNew) {
			NewFileDialog pd = new NewFileDialog(this, mainScreen);
			Rectangle rec = getBounds();
            pd.setLocation((int) (rec.getCenterX() - pd.getWidth() / 2),
                    (int) (rec.getCenterY() - pd.getHeight() / 2));
			pd.setModal(true);
			pd.setVisible(true);
		} else if (e.getSource() == menuItemSave) {
			saveFile();	
		} else if (e.getSource() == menuItemOpen) {
			openFile();
			mainScreen.repaint();
			updateTitleText();
		} else if (e.getSource() == menuItemUndo) {
			Origrammer.diagram.steps.get(Globals.currentStep).popUndoInfo();
			mainScreen.repaint();
		} else if (e.getSource() == menuItemCut) {
			if (Origrammer.diagram.steps.get(Globals.currentStep).getSelectedObjectsCount() == 0) {
				System.out.println("No Objects selected");
			} else {
				Origrammer.diagram.steps.get(Globals.currentStep).cutObjects();
			}
		}
		else if (e.getSource() == menuItemCopy) {
			if (Origrammer.diagram.steps.get(Globals.currentStep).getSelectedObjectsCount() == 0) {
				System.out.println("No Objects selected");
			} else {
				Origrammer.diagram.steps.get(Globals.currentStep).copyObjects();
			}
		} else if (e.getSource() == menuItemPaste) {
			if (Origrammer.diagram.steps.get(Globals.currentStep).copiedObjects.getCopiedObjectsCount() == 0) {
				System.out.println("No Objects copied");
			} else {
				Origrammer.diagram.steps.get(Globals.currentStep).pasteCopiedObjects();
			}
			repaint();
		} else if (e.getSource() == menutItemModelPreferences) {
			openModelPref();
			
		} else if (e.getSource() == menuItemOrigrammerPreferences) {
			openOrigrammerPref();
			
		}
	}
	
	/**
	 * Opens the Diagram Options Dialog
	 */
	private void openModelPref() {
		ModelPreferenceDialog mpd = new ModelPreferenceDialog(this, mainScreen);
		Rectangle rec = getBounds();
		mpd.setLocation((int) (rec.getCenterX() - mpd.getWidth() / 2),
                (int) (rec.getCenterY() - mpd.getHeight() / 2));
		//mpd.setModal(true);
		mpd.setVisible(true);
	}
	
	/**
	 * Opens the Origrammer Preferences Dialog
	 */
	private void openOrigrammerPref() {
		PreferenceDialog pd = new PreferenceDialog(this, mainScreen);
		Rectangle rec = getBounds();
        pd.setLocation((int) (rec.getCenterX() - pd.getWidth() / 2),
                (int) (rec.getCenterY() - pd.getHeight() / 2));
		pd.setVisible(true);
	}
	
	/**
	 * Opens the About Dialog
	 */
	private void openAboutDialog() {
		AboutDialog aboutD = new AboutDialog(this, mainScreen);
		Rectangle rec = getBounds();
		aboutD.setLocation((int) (rec.getCenterX() - aboutD.getWidth() / 2),
                (int) (rec.getCenterY() - aboutD.getHeight() / 2));
		aboutD.setVisible(true);
	}
	
	class FileFilterEx extends FileFilter {
		private String extensions[];
		private String msg;
		
		public FileFilterEx(String[] extensions, String msg) {
			this.extensions = extensions;
			this.msg = msg;
		}

		@Override
		public boolean accept(File f) {
			for (int i = 0; i < extensions.length; i++) {
				if (f.isDirectory()) {
					return true;
				}
				if (f.getName().endsWith(extensions[i])) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String getDescription() {
			return msg;
		}
	}
	
	
	@Override
	public void windowActivated(WindowEvent arg0) {		
	}

	@Override
	public void windowClosed(WindowEvent e) {		
	}

	@Override
	public void windowClosing(WindowEvent e) {		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {		
	}

	@Override
	public void windowIconified(WindowEvent e) {		
	}

	@Override
	public void windowOpened(WindowEvent e) {		
	}

	@Override
	public void componentHidden(ComponentEvent e) {		
	}

	@Override
	public void componentMoved(ComponentEvent e) {		
	}

	@Override
	public void componentResized(ComponentEvent e) {		
	}

	@Override
	public void componentShown(ComponentEvent e) {		
	}
}

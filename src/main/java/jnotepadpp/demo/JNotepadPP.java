package jnotepadpp.demo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Collator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import javax.swing.Timer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;

import jnotepadpp.DefaultMultipleDocumentModel;
import jnotepadpp.MultipleDocumentListener;
import jnotepadpp.SingleDocumentModel;

public class JNotepadPP extends JFrame {

	private static final long serialVersionUID = 1L;

	public static final String TITLE = "JNotepad++";

	private DefaultMultipleDocumentModel tabModel;

	private JLabel length = new JLabel();

	private JLabel time = new JLabel();

	private JLabel infoBar = new JLabel();

	private Timer clock;

	public JNotepadPP() {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLocation(0, 0);
		setSize(800, 800);

		this.setTitle(TITLE);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				exitAction();
			}
		});

		this.tabModel = new DefaultMultipleDocumentModel();
		this.startClock();
		this.initModelLiseners();

		initGUI();
	}

	private void initModelLiseners() {

		MultipleDocumentListener listener = new MultipleDocumentListener() {
			@Override
			public void currentDocumentChanged(SingleDocumentModel previousModel, SingleDocumentModel currentModel) {

				if (currentModel == null) {
					return;
				}

				JNotepadPP.this.textAreaStatus();
			}

			@Override
			public void documentAdded(SingleDocumentModel model) {
			}

			@Override
			public void documentRemoved(SingleDocumentModel model) {
			}
		};

		this.tabModel.addMultipleDocumentListener(listener);
	}

	private void initGUI() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(tabModel, BorderLayout.CENTER);

		this.createActions();
		this.createMenuBar();
		this.createToolbar();
		this.createStatusBar();

		tabModel.createNewDocument();
		this.textAreaStatus();
	}

	private void createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		menuBar.add(fileMenuBar());
		menuBar.add(editMenuBar());
		menuBar.add(toolsMenuBar());
		menuBar.add(languageMenuBar());

		this.setJMenuBar(menuBar);
	}

	private JMenu fileMenuBar() {
		JMenu file = new JMenu("File");

		file.add(new JMenuItem(newDocAction));
		file.add(new JMenuItem(openAction));
		file.add(new JMenuItem(saveAction));
		file.add(new JMenuItem(saveAsAction));

		file.addSeparator();

		file.add(new JMenuItem(closeAction));
		file.add(new JMenuItem(exitAction));
		return file;
	}

	private JMenu editMenuBar() {
		JMenu edit = new JMenu("Edit");

		edit.add(new JMenuItem(cutAction));
		edit.add(new JMenuItem(copyAction));
		edit.add(new JMenuItem(pasteAction));

		return edit;
	}

	private JMenu languageMenuBar() {
		JMenu languages = new JMenu("Languages");

		languages.add(new JMenuItem(hrAction));
		languages.add(new JMenuItem(enAction));
		languages.add(new JMenuItem(deAction));

		return languages;
	}

	private JMenu toolsMenuBar() {

		JMenu tools = new JMenu("Tools");

		JMenu changeCase = new JMenu("Change case");
		changeCase.add(new JMenuItem(upperAction));
		changeCase.add(new JMenuItem(lowerAction));
		changeCase.add(new JMenuItem(invertAction));
		
		tools.add(changeCase);
		tools.addSeparator();

		JMenu sort = new JMenu("Sort");
		sort.add(ascAction);
		sort.add(descAction);

		tools.add(sort);

		return tools;
	}

	private void createToolbar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(true);

		toolBar.add(new JButton(newDocAction));
		toolBar.add(new JButton(openAction));
		toolBar.add(new JButton(saveAction));
		toolBar.add(new JButton(saveAsAction));

		toolBar.addSeparator();

		toolBar.add(new JButton(cutAction));
		toolBar.add(new JButton(copyAction));
		toolBar.add(new JButton(pasteAction));

		toolBar.addSeparator();

		toolBar.add(new JButton(closeAction));
		toolBar.add(new JButton(exitAction));

		this.getContentPane().add(toolBar, BorderLayout.PAGE_START);
	}

	private void createStatusBar() {
		JPanel statusPanel = new JPanel(new GridLayout(0, 5));

		statusPanel.add(length);
		statusPanel.add(new JSeparator(JSeparator.VERTICAL));

		statusPanel.add(infoBar);
		statusPanel.add(new JSeparator(JSeparator.VERTICAL));

		time.setHorizontalAlignment(JLabel.RIGHT);
		statusPanel.add(time);

		this.getContentPane().add(statusPanel, BorderLayout.PAGE_END);
	}

	private void startClock() {
		DateTimeFormatter dateAndTimeFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
		
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
            	JNotepadPP.this.time.setText(dateAndTimeFormat.format(LocalDateTime.now()));
            }
        };

		this.clock = new Timer(1000, taskPerformer);
		this.clock.start();
	}

	protected void textAreaStatus() {
		JTextArea currentEditor = this.tabModel.getCurrentDocument().getTextComponent();
		
		int len = currentEditor.getText().length();
		
		this.length.setText("length: " + len);
		try {
			
			int position = currentEditor.getCaretPosition(); //pozicija
			int line = currentEditor.getLineOfOffset(position); //linija
			int column = position - currentEditor.getLineStartOffset(line); //stupac
			
			int dot = currentEditor.getCaret().getDot();
			int mark =  currentEditor.getCaret().getMark();
			
			int selected = Math.abs(dot - mark); //abs length
			
			this.infoBar.setText(String.format("%s:%d %s:%d %s:%d", "Ln", line, "Col", column, "Sel", selected));
			
		} catch (BadLocationException ignorable) {
			System.err.println("Error has acoured during updaing status of text");
		}
	}
	
	
	protected void addCaretListener(JTextArea editor) {
		editor.addCaretListener(l -> {
			int dot = editor.getCaret().getDot();
			int mark = editor.getCaret().getMark();
			boolean change = dot != mark;

			upperAction.setEnabled(change);
			lowerAction.setEnabled(change);
			invertAction.setEnabled(change);
			cutAction.setEnabled(change);
			copyAction.setEnabled(change);
			ascAction.setEnabled(change);
			descAction.setEnabled(change);

			JNotepadPP.this.textAreaStatus();
		});
	}

	private final Action newDocAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			SingleDocumentModel newDoc =  tabModel.createNewDocument();
			addCaretListener(newDoc.getTextComponent());
		}
	};

	private final Action openAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			openAction();
			addCaretListener(tabModel.getCurrentDocument().getTextComponent());
		}
	};

	private final Action saveAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			saveAction();
			addCaretListener(tabModel.getCurrentDocument().getTextComponent());
		}
	};

	private final Action saveAsAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			saveAsAction();
			addCaretListener(tabModel.getCurrentDocument().getTextComponent());
		}
	};

	private final Action closeAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			closeAction();
		}
	};

	private final Action exitAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			exitAction();
		}
	};

	private final Action cutAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			new DefaultEditorKit.CutAction().actionPerformed(e);
		}
	};

	private final Action copyAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			new DefaultEditorKit.CopyAction().actionPerformed(e);
		}
	};

	private final Action pasteAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			new DefaultEditorKit.PasteAction().actionPerformed(e);
		}
	};

	private final Action upperAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			caseAction(s -> s.toUpperCase());
		}
	};

	private final Action lowerAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			caseAction(s -> s.toLowerCase());
		}
	};

	private final Action invertAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			caseAction(s -> toggleCase(s));
		}
	};

	private final Action ascAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			sortAction(lines -> {
				Collator collator = Collator.getInstance(new Locale("en"));
				Collections.sort(lines, collator);

	            return String.join("\n", lines);
	        });
		}
	};

	private final Action descAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			sortAction(lines -> {
				Collator collator = Collator.getInstance(new Locale("en"));
				Collections.sort(lines, collator.reversed());

	            return String.join("\n", lines);
	        });
		}
	};

	private final Action hrAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			// ToDo
		}
	};

	private final Action enAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			// ToDo
		}
	};

	private final Action deAction = new AbstractAction() {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			// ToDo
		}
	};

	private void createActions() {

		// Open
		this.openAction.putValue(Action.NAME, "Open");
		this.openAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
		this.openAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
		this.openAction.putValue(Action.SHORT_DESCRIPTION, "Used to open existing file from disk.");

		// Save
		this.saveAction.putValue(Action.NAME, "Save");
		this.saveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"));
		this.saveAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
		this.saveAction.putValue(Action.SHORT_DESCRIPTION, "Used to save current file to disk.");

		// SaveAs
		this.saveAsAction.putValue(Action.NAME, "Save As");
		this.saveAsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control J"));
		this.saveAsAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_J);
		this.saveAsAction.putValue(Action.SHORT_DESCRIPTION, "Used to save as current file to disk.");

		// Close action
		this.closeAction.putValue(Action.NAME, "Close");
		this.closeAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control W"));
		this.closeAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_W);
		this.closeAction.putValue(Action.SHORT_DESCRIPTION, "Close document.");

		// Exit action
		this.exitAction.putValue(Action.NAME, "Exit");
		this.exitAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control E"));
		this.exitAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
		this.exitAction.putValue(Action.SHORT_DESCRIPTION, "Exit program.");

		// New Document
		this.newDocAction.putValue(Action.NAME, "New Document");
		this.newDocAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control P"));
		this.newDocAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
		this.newDocAction.putValue(Action.SHORT_DESCRIPTION, "New document.");

		// Cut
		this.cutAction.putValue(Action.NAME, "Cut");
		this.cutAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"));
		this.cutAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
		this.cutAction.putValue(Action.SHORT_DESCRIPTION, "Cut text.");

		// Copy
		this.copyAction.putValue(Action.NAME, "Copy");
		this.copyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
		this.copyAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		this.copyAction.putValue(Action.SHORT_DESCRIPTION, "Copy text.");

		// Paste
		this.pasteAction.putValue(Action.NAME, "Paste");
		this.pasteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
		this.pasteAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_V);
		this.pasteAction.putValue(Action.SHORT_DESCRIPTION, "Paste text.");

		// ToUpper
		this.upperAction.putValue(Action.NAME, "To Uppercase");
		this.upperAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control U"));
		this.upperAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_U);
		this.upperAction.putValue(Action.SHORT_DESCRIPTION, "To upper text.");

		// ToLower
		this.lowerAction.putValue(Action.NAME, "To Lowercase");
		this.lowerAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control L"));
		this.lowerAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_L);
		this.lowerAction.putValue(Action.SHORT_DESCRIPTION, "To lower text.");

		// Invert
		this.invertAction.putValue(Action.NAME, "Invert Case");
		this.invertAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control I"));
		this.invertAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_I);
		this.invertAction.putValue(Action.SHORT_DESCRIPTION, "Invert text.");

		// Asc
		this.ascAction.putValue(Action.NAME, "Ascending");
		this.ascAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control M"));
		this.ascAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_M);
		this.ascAction.putValue(Action.SHORT_DESCRIPTION, "Ascending text.");

		// Desc
		this.descAction.putValue(Action.NAME, "Descending");
		this.descAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"));
		this.descAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
		this.descAction.putValue(Action.SHORT_DESCRIPTION, "Descending text.");

		// Hr -lang
		this.hrAction.putValue(Action.NAME, "Croatian");
		this.hrAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control 9"));
		this.hrAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
		this.hrAction.putValue(Action.SHORT_DESCRIPTION, "Craotian language.");

		// En -lang
		this.enAction.putValue(Action.NAME, "English");
		this.enAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control 8"));
		this.enAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_E);
		this.enAction.putValue(Action.SHORT_DESCRIPTION, "English language.");

		// De -lang
		this.deAction.putValue(Action.NAME, "German");
		this.deAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control 7"));
		this.deAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_G);
		this.deAction.putValue(Action.SHORT_DESCRIPTION, "German language.");
				
		
	}

	protected void caseAction(Function<String, String> function) {
		
		JTextArea currentTextArea = tabModel.getCurrentDocument().getTextComponent();

		int offset = Math.min(currentTextArea.getCaret().getDot(), currentTextArea.getCaret().getMark());
		int len = Math.abs(currentTextArea.getCaret().getDot() - currentTextArea.getCaret().getMark());

		if (len < 1) {
			return;
		}
		Document doc = currentTextArea.getDocument();
		
		try {
			String text = doc.getText(offset, len);
			text = function.apply(text); //Akcija, funkcije
			doc.remove(offset, len);
			doc.insertString(offset, text, null);
		} catch (BadLocationException ignored) {
			System.err.println("Error accured during text operation");
		}
	}

	protected void sortAction(Function<List<String>, String> function) {
		
		JTextArea editor = tabModel.getCurrentDocument().getTextComponent();
		Document doc = editor.getDocument();
		Element root = doc.getDefaultRootElement();
		
		
        int offset = editor.getSelectionStart();
        int len = editor.getSelectionEnd();
        
        offset = root.getElement(root.getElementIndex(offset)).getStartOffset();
        
        len = Math.min(root.getElement(root.getElementIndex(len)).getEndOffset(), doc.getLength());

		try {			
			String text = doc.getText(offset, len - offset);
			List<String> lines = new ArrayList<>(Arrays.asList(text.split("\n")));
			
			String sortedText = function.apply(lines);

			doc.remove(offset, len - offset);
			doc.insertString(offset, sortedText, null);
			this.tabModel.getCurrentDocument().getTextComponent().setDocument(doc);
		} catch (BadLocationException ignorable) {
			ignorable.printStackTrace();
		}
	}
	
	protected void exitAction() {

		while (this.tabModel.getCurrentDocument() != null) {
			 this.tabModel.setSelectedIndex(0); //Prvi 
			 SingleDocumentModel model = this.tabModel.getDocument(0);
			
			if (model.isModified()) {
				Path path = model.getFilePath();
				String fileName = "unnamed";
				
				if(path != null) {
					fileName = path.getFileName().toString();
					
				}
				userInputEnum wantToSave = userInputEnum.CANCEL;
				
				wantToSave = JOptionPaneConfirmation("Do you want to save " + fileName, "Save file");


				if (wantToSave == userInputEnum.YES) {
					this.saveAction();
				}
				
				if(wantToSave == userInputEnum.CANCEL) {
					return;
				}
				
				if(wantToSave == userInputEnum.NO) {
					tabModel.closeDocument(model);
				}
				
			}else {
				tabModel.closeDocument(model);
			}
		}


		this.tabModel.disposeModel();
		this.dispose();
		System.exit(ABORT);
	}

	private userInputEnum JOptionPaneConfirmation(String optionQuestion, String optionTitle) {
		
		int outputOption = JOptionPane.showConfirmDialog(this, optionQuestion, optionTitle, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

		if(outputOption == JOptionPane.CANCEL_OPTION || outputOption == JOptionPane.CLOSED_OPTION) {
			return userInputEnum.CANCEL;
		}
		
		if (outputOption == JOptionPane.NO_OPTION) {
			return userInputEnum.NO;
		}

		if (outputOption == JOptionPane.YES_OPTION) {
			return userInputEnum.YES;
		}

		return userInputEnum.CANCEL;
	}

	protected void saveAsAction() {
		SingleDocumentModel model = tabModel.getCurrentDocument();

		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("Save document as");
		
		if (jfc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		Path destination = jfc.getSelectedFile().toPath();

		if (Files.exists(destination)) {
			
			userInputEnum wantToSave = userInputEnum.CANCEL;

			wantToSave = JOptionPaneConfirmation("File already exist at path: " + destination, "File already exists");
			
			if (wantToSave == userInputEnum.CANCEL || wantToSave == userInputEnum.NO) {
				return;
			}
		}

		tabModel.saveDocument(model, destination);
	}

	protected void saveAction() {
		SingleDocumentModel model = tabModel.getCurrentDocument();

		if (model.getFilePath() == null) {
			saveAsAction();
		} else {
			tabModel.saveDocument(model, null);
		}
	}

	protected void openAction() {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("open_file");
		
		if (jfc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}

		Path filePath = jfc.getSelectedFile().toPath();
		if (Files.isReadable(filePath) == false) {
			JOptionPane.showMessageDialog(this, "Can not open file at: " + filePath , "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		tabModel.loadDocument(filePath);
	}

	protected void closeAction() {
		SingleDocumentModel model = tabModel.getCurrentDocument();
		
		if (model.isModified()) {
			Path path = model.getFilePath();
			String fileName = "unnamed";
			
			if(path != null) {
				fileName = path.getFileName().toString();
				
			}
			userInputEnum wantToSave = userInputEnum.CANCEL;
			
			wantToSave = JOptionPaneConfirmation("Do you want to save " + fileName, "Save file");

			if (wantToSave == userInputEnum.CANCEL) { //odustajem
				return;
			}else if (wantToSave == userInputEnum.YES) { //hocu spremit
				this.saveAction();
			}
		}

		tabModel.closeDocument(model); //Rekao sam no, necu spremit
		
		if (tabModel.getNumberOfDocuments() == 0) {
			tabModel.createNewDocument();
		}
	}

	protected String toggleCase(String s) {
		char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (Character.isUpperCase(chars[i])) {
				chars[i] = Character.toLowerCase(chars[i]);
			} else if (Character.isLowerCase(chars[i])) {
				chars[i] = Character.toUpperCase(chars[i]);
			}
		}
		return new String(chars);
	}
	
	private static enum  userInputEnum{
		YES,
		CANCEL,
		NO
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new JNotepadPP().setVisible(true);
		});
	}

}

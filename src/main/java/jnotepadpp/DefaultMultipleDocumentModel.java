package jnotepadpp;

import java.awt.Image;
import java.io.IOException;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

public class DefaultMultipleDocumentModel extends JTabbedPane implements MultipleDocumentModel{
	
	private static final long serialVersionUID = 1L;

	private List<SingleDocumentModel> documents;
    
	private List<MultipleDocumentListener> listeners;
	
    private SingleDocumentModel currentDocument;
    
    private final ImageIcon modifiedIcon = loadImageIcon("modified.png");

    private final ImageIcon unModifiedIcon = loadImageIcon("unmodifed.png");

        
	public DefaultMultipleDocumentModel() {
		this.documents = new LinkedList<>();
		this.currentDocument  = null;
		this.listeners = new LinkedList<>();
		
		this.addChangeListener(e -> {
            SingleDocumentModel previousModel = this.currentDocument;

            int newIndex = this.getSelectedIndex();
            
            if(newIndex != -1) {
            	this.currentDocument = this.documents.get(newIndex);
            }else {
            	this.currentDocument = null;
            }
            
            //Promjena u subjektu
            this.notifyRegisteredListeners(listener -> listener.currentDocumentChanged(previousModel, this.currentDocument));
        });
	}
	

	@Override
	public Iterator<SingleDocumentModel> iterator() {
		return documents.iterator();
	}

	@Override
	public JComponent getVisualComponent() {
		return this;
	}

	@Override
	public SingleDocumentModel createNewDocument() {
        SingleDocumentModel previousModel = this.currentDocument;

		DefaultSingleDocumentModel newDocument = new DefaultSingleDocumentModel(null, "");
		this.documents.add(newDocument);
        this.addTab("", new JPanel().add(new JScrollPane(newDocument.getTextComponent())));
        int tabIndex = this.documents.indexOf(newDocument);
        this.setTitleAt(tabIndex, "unnamed"); //novi file
        this.setSelectedIndex(tabIndex);
        this.setIconAt(tabIndex, unModifiedIcon);

        newDocument.addSingleDocumentListener(addNewDocumentListener());
        
        this.notifyRegisteredListeners(listener -> listener.documentAdded(newDocument));
        
        this.notifyRegisteredListeners(listener -> listener.currentDocumentChanged(previousModel, newDocument));
        
        this.currentDocument = newDocument;

		return newDocument;
	}


	@Override
	public SingleDocumentModel getCurrentDocument() {
		return this.currentDocument;
	}

	@Override
	public SingleDocumentModel loadDocument(Path path) {

		if(path == null) {
			JOptionPane.showMessageDialog(this,	"Path je null",	"Pogreska", JOptionPane.ERROR_MESSAGE);
		}
		
        SingleDocumentModel previousModel = this.currentDocument;
        
        
        //Ako je vec otvoren taj path
        boolean found = false;
        for(SingleDocumentModel document: this.documents) {
        	
        	if(document.getFilePath() == null) {
        		continue;
        	}
        	
        	if(document.getFilePath().equals(path)) {
        		this.currentDocument = document;
        		found = true;
        	}
        }
        
        if(found == false) {
        	Path filePath = path.toAbsolutePath().normalize();
        	if (Files.isReadable(filePath) == false) {
        		JOptionPane.showMessageDialog(this,
        				"Datoteka " + filePath.toAbsolutePath() + " ne postoji!", "PogreĹˇka",
        				JOptionPane.ERROR_MESSAGE);
        		return null;
        	}
        	
        	byte[] okteti;
        	try {
        		okteti = Files.readAllBytes(filePath);
        	} catch (Exception ex) {
        		JOptionPane.showMessageDialog(this,
        				"PogreĹˇka prilikom ÄŤitanja datoteke " + filePath.toAbsolutePath() + ".", "PogreĹˇka",
        				JOptionPane.ERROR_MESSAGE);
        		return null;
        	}
        	
        	String tekst = new String(okteti, StandardCharsets.UTF_8);
        	
        	DefaultSingleDocumentModel newDocument = new DefaultSingleDocumentModel(filePath, tekst);
        	
        	this.documents.add(newDocument);
        	
            this.addTab("", new JPanel().add(new JScrollPane(newDocument.getTextComponent())));
            int tabIndex = this.documents.indexOf(newDocument);
            this.setTitleAt(tabIndex, filePath.getFileName().toString()); //novi file
            this.setSelectedIndex(tabIndex);
            this.setIconAt(tabIndex, unModifiedIcon);
            
            newDocument.addSingleDocumentListener(addNewDocumentListener());

            this.notifyRegisteredListeners(listener -> listener.documentAdded(this.currentDocument));
            
            this.currentDocument = newDocument;

        }
        
        	this.notifyRegisteredListeners(listener -> listener.currentDocumentChanged(previousModel, this.currentDocument));

        
        return currentDocument;	
	}

	@Override
	public void saveDocument(SingleDocumentModel model, Path newPath) {
		if(newPath == null) {
			newPath = model.getFilePath();
		}
		
		newPath = newPath.toAbsolutePath().normalize();
		
		byte[] podatci = model.getTextComponent().getText().getBytes(StandardCharsets.UTF_8);

		try {
			Files.write(newPath, podatci);
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(this,
					"PogreĹˇka prilikom zapisivanja datoteke " + newPath.toFile().getAbsolutePath()
							+ ".\nPaĹľnja: nije jasno u kojem je stanju datoteka na disku!",
					"PogreĹˇka", JOptionPane.ERROR_MESSAGE);
			return;
		}
        this.notifyRegisteredListeners(listener -> listener.currentDocumentChanged(model, this.currentDocument));
        
        model.setFilePath(newPath);
        model.setModified(false);
        
		setTitleAt(getSelectedIndex(), currentDocument.getFilePath().getFileName().toString());
        setIconAt(getSelectedIndex(), unModifiedIcon);       
	}

	@Override
	public void closeDocument(SingleDocumentModel model) {
		
        int oldIndex = this.documents.indexOf(model);
        
        int newIndex = -1;
        
        if(this.documents.size() > 1) {
        	newIndex = this.documents.size()-2;
        }
        
        this.documents.remove(model);
        this.notifyRegisteredListeners(listener -> listener.documentRemoved(model));
        this.removeTabAt(oldIndex);
        
        if(newIndex != -1) {
        	this.currentDocument = this.documents.get(newIndex);
        }else {
        	this.currentDocument = null;
        }

        this.notifyRegisteredListeners(listener -> listener.currentDocumentChanged(model, this.currentDocument));
    }
	
	

	@Override
	public void addMultipleDocumentListener(MultipleDocumentListener l) {
		this.listeners.add(l);
	}

	@Override
	public void removeMultipleDocumentListener(MultipleDocumentListener l) {
		this.listeners.remove(l);
	}

	@Override
	public int getNumberOfDocuments() {
		return this.documents.size();
	}

	@Override
	public SingleDocumentModel getDocument(int index) {
		return this.documents.get(index);
	}

	@Override
	public SingleDocumentModel findForPath(Path path) {
		if(path == null) {
			return null;
		}
		
		for(SingleDocumentModel document: this.documents) {
			if(document.getFilePath().equals(path)) {
				return document;
			}
		}
		
		return null;

	}
	
	@Override
	public int getIndexOfDocument(SingleDocumentModel doc) {
		return this.documents.indexOf(doc);
	}

	
	private SingleDocumentListener addNewDocumentListener() {
		return new SingleDocumentListener() {
			
			@Override
			public void documentModifyStatusUpdated(SingleDocumentModel model) {
                if (model.isModified()) {
                	setIconAt(getSelectedIndex(), modifiedIcon);                	
                }else {
                	setIconAt(getSelectedIndex(), unModifiedIcon);
                }
            }
			@Override
			public void documentFilePathUpdated(SingleDocumentModel model) {
				
			}
		};
	}
	
	//Obavjestavanje subscribera
    private void notifyRegisteredListeners(Consumer<MultipleDocumentListener> action) {
    	
    	for(MultipleDocumentListener listener: this.listeners) {
    		action.accept(listener);
    	}
    }
    
    protected static ImageIcon loadImageIcon(String path) {
    	
        URL imgURL = DefaultMultipleDocumentModel.class.getClassLoader().getResource(path);
                
        if(imgURL == null) {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
        
        Image loadImage = null;
		try {
			loadImage = new ImageIcon(Files.readAllBytes(Paths.get(imgURL.toURI()))).getImage();
		} catch (Exception e) {
            System.err.println("Couldn't create image from file: " + path);
            return null;

		}
        return new ImageIcon(loadImage.getScaledInstance(20, 20, Image.SCALE_SMOOTH));

    }
    
    public void disposeModel() {
    	this.listeners = new LinkedList<>();
    	this.documents = new LinkedList<>();
    }
    
    public List<SingleDocumentModel> getAllDocuments() {
    	return this.documents;
    }

}

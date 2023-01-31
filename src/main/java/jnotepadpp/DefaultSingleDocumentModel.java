package jnotepadpp;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class DefaultSingleDocumentModel implements SingleDocumentModel{
	
    private Path filePath;
    
    private JTextArea textComponent;
    
    private boolean modified;

    private String originalText;

    private List<SingleDocumentListener> listeners;
    
    public DefaultSingleDocumentModel(Path filePath, String textContent) {
    	
    	if(filePath == null) {
    		this.filePath = null;
    	}else {
    		this.filePath = filePath.toAbsolutePath().normalize();
    	}
    	
    	if(textContent == null) {
    		this.textComponent = new JTextArea("");
    	}else {
    		this.textComponent = new JTextArea(textContent);
    	}
    	
        this.listeners = new ArrayList<>();
        
        this.originalText = textContent;

        this.textComponent.getDocument().addDocumentListener(new DocumentListener() {
        	
            @Override
            public void insertUpdate(DocumentEvent e) {
                setModified(this.isModified());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setModified(this.isModified());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setModified(this.isModified());
            }
            
            public boolean isModified() {
            	if(originalText.equals(textComponent.getText()) == false) {
            		return true;
            	}
            	return false;
            }
            
        });
    }


	@Override
	public JTextArea getTextComponent() {
		return this.textComponent;
	}

	@Override
	public Path getFilePath() {
		return this.filePath;
	}

	@Override
	public void setFilePath(Path path) {
	      this.filePath = path.toAbsolutePath().normalize();
	      this.notifyRegisteredListeners(listener -> listener.documentFilePathUpdated(this));
		
	}

	@Override
	public boolean isModified() {
		return this.modified;
	}

	@Override
	public void setModified(boolean modified) {
        this.modified = modified;
        
        //Ako je modification stavljen na save
        if (modified == false) {
        	this.originalText = this.textComponent.getText();
        }

        this.notifyRegisteredListeners(listener -> listener.documentModifyStatusUpdated(this));
		
	}

	@Override
	public void addSingleDocumentListener(SingleDocumentListener l) {
        this.listeners.add(l);
		
	}

	@Override
	public void removeSingleDocumentListener(SingleDocumentListener l) {
		this.listeners.remove(l);
		
	}
	
    private void notifyRegisteredListeners(Consumer<SingleDocumentListener> action) {
    	
    	for(SingleDocumentListener listener: listeners) {
    		action.accept(listener);
    	}
    	
    }

    
    
    
    

}

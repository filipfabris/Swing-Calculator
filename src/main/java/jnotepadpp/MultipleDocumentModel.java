package jnotepadpp;

import java.nio.file.Path;

import javax.swing.JComponent;

public interface MultipleDocumentModel extends Iterable<SingleDocumentModel> {
	JComponent getVisualComponent();

	SingleDocumentModel createNewDocument();

	SingleDocumentModel getCurrentDocument();

	SingleDocumentModel loadDocument(Path path);

	void saveDocument(SingleDocumentModel model, Path newPath);

	void closeDocument(SingleDocumentModel model);

	void addMultipleDocumentListener(MultipleDocumentListener l);

	void removeMultipleDocumentListener(MultipleDocumentListener l);

	int getNumberOfDocuments();

	SingleDocumentModel getDocument(int index);

	SingleDocumentModel findForPath(Path path); // null, if no such model exists
	
	int getIndexOfDocument(SingleDocumentModel doc);  //-1 if not present
}

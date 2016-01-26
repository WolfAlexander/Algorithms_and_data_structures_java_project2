import se.kth.id1020.util.Document;

import java.util.ArrayList;

/**
 * This is property of every word in dictionary - holds list of documents
 * is which documents word exists
 */
public class WordProperties {
    private ArrayList<WordDocument> documentExistsIn = new ArrayList<WordDocument>();

    public WordProperties(Document document){
        documentExistsIn.add(new WordDocument(document));
    }

    /**
     * Adds new document to the list of documents where word exists or if
     * document exists in the list then increments number of occurrences in the document
     * @param document the document where word is found
     */
    public void foundIn(Document document){
        BinarySearch<WordDocument, Document> binarySearch = new BinarySearch<WordDocument, Document>();

        int index = binarySearch.search(documentExistsIn, document);

        if(index < 0)
            documentExistsIn.add(new WordDocument(document));
        else
            documentExistsIn.get(index).incrementNumberOfOccurrences();
    }

    public int countDocumentsExistsIn(){
        return documentExistsIn.size();
    }

    public ArrayList<WordDocument> getDocumentWhereWordExistsIn(){
        return this.documentExistsIn;
    }
}
import se.kth.id1020.util.Document;

import java.util.HashMap;

/**
 * This static class contains all documents and count of all words in each document
 */
public class Documents {
    private static HashMap<String, Integer> documents = new HashMap<String, Integer>();

    /**
     * This method adds new document to the list
     * If documents already exists then its count increases
     * @param document is the reference to a document that has to be added
     */
    public static void add(Document document){
        if(documents.containsKey(document.name))
            documents.put(document.name, (documents.get(document.name)+1));
        else
            documents.put(document.name, 1);
    }

    /**
     * @return total count of documents
     */
    public static int getDocumentsCount(){
        return documents.size();
    }

    /**
     * This method returns word count for a certain document
     * @param name is that name of the document
     * @return int number of words in document or -1 is document is not found
     */
    public static int getWordCountInDocument(String name){
        try{
            return documents.get(name);
        }catch(NullPointerException ex){
            System.err.println("Documents error! Document " + name + " does not exist!");
            return -1;
        }
    }
}
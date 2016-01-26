import se.kth.id1020.util.Document;

/**
 * Document that a word exists in
 * This object contains document reference, number of occurrences of the word in this document
 * and a relevance variable that will change dependently on entered query
 */
public class WordDocument implements Comparable<Document>{
    private Document document;
    private int numberOfOccurrences;
    private double tempRelevance = 0.0;

    /**
     * Constructor of WordDocument
     * @param document that will be stored
     */
    public WordDocument(Document document){
        this.document = document;
        this.numberOfOccurrences = 1;
    }

    /**
     * This method is used to compare this object with another
     * @param obj is the another object
     * @return boolean if true ..
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof WordDocument){
            WordDocument temp = (WordDocument)obj;
            return this.document.name.equals(temp.document.name);
        }else
            return false;

    }

    public int compareByRelevance(WordDocument wd){
        return Double.compare(this.tempRelevance, wd.tempRelevance);
    }

    /**
     * Compares this document to another by popularity
     * @param wd is the another document
     * @return 1 if this document is more popular, 0 if documents are equally popular
     * and -1 if this document is less popular
     */
    public int compareByPopularity(WordDocument wd){
        if(this.getDocument().popularity > wd.getDocument().popularity)
            return 1;
        else if(this.getDocument().popularity < wd.getDocument().popularity)
            return -1;
        else
            return 0;
    }

    public void changeRelevance(double relevance){
        this.tempRelevance = relevance;
    }

    public void incrementNumberOfOccurrences(){
        numberOfOccurrences++;
    }

    public Document getDocument(){
        return document;
    }

    public int getNumberOfOccurrences(){
        return numberOfOccurrences;
    }

    /**
     * Compares this document with another by name which is how
     * document can be compares
     * @param doc is the another document
     * @return 1 if this document is greater, 0 if this document is equal or -1 if this document is less than doc
     */
    public int compareTo(Document doc) {
        return this.document.name.compareTo(doc.name);
    }
}
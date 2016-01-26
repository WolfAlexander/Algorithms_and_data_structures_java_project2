import se.kth.id1020.TinySearchEngineBase;
import se.kth.id1020.util.Attributes;
import se.kth.id1020.util.Document;
import se.kth.id1020.util.Sentence;
import se.kth.id1020.util.Word;

import java.util.HashMap;
import java.util.List;

public class TinySearchEngine implements TinySearchEngineBase {
    private HashMap<String, WordProperties> dictionary = new HashMap<String, WordProperties>();
    private Query query;
    protected Cache cache = new Cache();

    /**
     * This method is called when new sentence is given by Driver from main class
     * @param sentence of type Sentence
     * @param attributes attributes of the sentence
     */
    public void insert(Sentence sentence, Attributes attributes) {
        for(Word word : sentence.getWords()){
            if(dictionary.containsKey(word.word))
                dictionary.get(word.word).foundIn(attributes.document);
            else
                dictionary.put(word.word, new WordProperties(attributes.document));

            Documents.add(attributes.document);
        }
    }

    /**
     * This method is called when searching is performed
     * @param s is the query
     * @return List of Documents as a result of query
     */
    public List<Document> search(String s) {
        try{
            this.query = new Query(s, dictionary, cache);
            return query.execute();
        }catch(Exception ex){
            ex.printStackTrace();
            System.err.println("Invalid query!");
            return null;
        }
    }

    /**
     * Returns infix notation of a query
     * @param s is a query
     * @return string infix notation
     */
    public String infix(String s){
        return "Query(" + query.getQuery() + ")";
    }

    public void preInserts() {
        //no strong need for this in my implementation
    }

    public void postInserts() {
        //no strong need for this in my implementation
    }
}
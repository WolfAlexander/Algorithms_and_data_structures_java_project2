import se.kth.id1020.util.Document;
import se.kth.id1020.util.Word;

import java.util.*;

public class Query {
    private HashMap<String, WordProperties> dictionary;
    private String query;
    private ArrayList<String> subQueries = new ArrayList<String>();
    private boolean advancedQuery = false;
    private Cache cache;
    private ArrayList<String> properties = new ArrayList<String>(Arrays.asList("relevance", "popularity"));
    private ArrayList<String> orderDirections = new ArrayList<String>(Arrays.asList("asc", "desc"));

    /**
     * Constructor of query splits string query in parts
     * @param enteredQuery is the incoming query
     * @param dictionary is the HashMap that query will be run on
     */
    public Query(String enteredQuery, HashMap<String, WordProperties> dictionary, Cache cache){
        this.dictionary = dictionary;
        this.cache = cache;

        if(isAdvancedQuery(enteredQuery)) {
            this.advancedQuery = true;
            this.query = getInfixNotation(enteredQuery);
        }else
            this.query = enteredQuery;
    }

    /**
     * This method starts executing query
     * @return list of documents which is a result of the query
     */
    public List<Document> execute(){
        if(!advancedQuery)
            return convertToDocumentsList(executeRegularQuery());
        else
            return convertToDocumentsList(executeAdvancedQuery());
    }

    /**
     * This method returns query that execute method executes
     * @return String query
     */
    public String getQuery(){
        return this.query;
    }

    //Executes query of type T or T orderby Property Direction where T is one word
    private ArrayList<WordDocument> executeRegularQuery(){
        String[] splitedQuery = this.query.split(" ");
        ArrayList<WordDocument> listOfWordDocuments = new ArrayList<WordDocument>();
        String word = "";

        for(int i = 0; i < splitedQuery.length; i++){
            if(!isOrderByCommand(splitedQuery[i]) && i == 0) {
                word = splitedQuery[i];

                if(cache.isCached(word))
                    addWithoutDuplicate(listOfWordDocuments, cache.getCachedResult(word));
                else
                    addWithoutDuplicate(listOfWordDocuments, getDocumentsWhereWordExists(word));

            }else if(i >= 1 && isOrderByCommand(splitedQuery[i])) {
                subQueries.add(word);
                listOfWordDocuments = orderBy(listOfWordDocuments, splitedQuery[++i], splitedQuery[++i]);
                break;
            }
            else
                throw new IllegalArgumentException();
        }

        return listOfWordDocuments;
    }

    //Executes query if type T or T orderby Property Direction where T is
    //T+T or T|T or T-T where T is a word
    private ArrayList<WordDocument> executeAdvancedQuery(){
        ArrayList<WordDocument> documents = customDijkstrasTwoStack(this.query.split(" "));
        ArrayList<WordDocument> result = new ArrayList<WordDocument>();

        try{
            String[] orderByPart = query.substring(query.indexOf("ORDERBY")).trim().toLowerCase().split(" ");
            if(isOrderByCommand(orderByPart[0]))
                result = orderBy(documents, orderByPart[1], orderByPart[2]);
        }catch (StringIndexOutOfBoundsException ex){
            result = documents;
        }

        return result;
    }

    //Dijkstras two stack algorithm is used to evaluate an infix expression
    //For task 5.3 this method is now completely modified
    private ArrayList<WordDocument> customDijkstrasTwoStack(String[] input){
        Stack<String> signs = new Stack<String>();
        Stack<ArrayList<WordDocument>> values = new Stack<ArrayList<WordDocument>>();
        String tempWord0 = null;
        String tempWord1 = null;

        for(int i = 0; i < input.length; i++) {
            String t = input[i];

            if (t.equals("(") || t.equals("")) ;
            else if (isSignOperator(t)) {
                signs.push(t);
            }else if (t.equals(")")) {
                ArrayList<WordDocument> value = new ArrayList<WordDocument>();
                String sign = signs.pop();
                String subQuery = tempWord0 + " " + sign + " " + tempWord1;

                if(tempWord0 != null && tempWord1 != null && cache.isCached(subQuery)) {
                    value = cache.getCachedResult(subQuery);

                    tempWord0 = null;
                    tempWord1 = null;
                }else{
                    ArrayList<WordDocument> documentsOfWord0;
                    ArrayList<WordDocument> documentsOfWord1;

                    if(tempWord0 != null && tempWord1 != null){
                        documentsOfWord0 = getDocumentsWhereWordExists(tempWord0);
                        documentsOfWord1 = getDocumentsWhereWordExists(tempWord1);
                    }else{
                        documentsOfWord1 = values.pop();
                        documentsOfWord0 = values.pop();
                    }

                    if (sign.equals("+")) {
                        value = intersection(documentsOfWord0, documentsOfWord1);
                    } else if (sign.equals("-"))
                        value = difference(documentsOfWord0, documentsOfWord1);
                    else if (sign.equals("|"))
                        value = union(documentsOfWord0, documentsOfWord1);


                    if(tempWord0 != null && tempWord1 != null)
                        cache.addToCache(subQuery, value);

                    tempWord0 = null;
                    tempWord1 = null;
                }

                values.push(value);
            }else if(t.toLowerCase().equals("orderby")){
                break;
            }else{
                if(tempWord0 == null)
                    tempWord0 = t;
                else
                    tempWord1 = t;
            }
        }

        return values.pop();
    }

    //Converts prefix notation to infix notation
    private String getInfixNotation(String query){
        Stack<Character> signs = new Stack<Character>();
        ArrayList<String> queryParts = new ArrayList<String>();
        String[] splitedQuery = query.split(" ");
        String infixQuery;

        for(int i = 0; i < splitedQuery.length; i++){
            if(isSignOperator(splitedQuery[i]))
                signs.push(splitedQuery[i].charAt(0));
            else if(isOrderByCommand(splitedQuery[i]))
                queryParts.add(splitedQuery[i] + " " + splitedQuery[++i] + " " + splitedQuery[++i]);
            else {
                String word0 = splitedQuery[i];
                Character sign = signs.pop();
                String word1 = splitedQuery[++i];

                queryParts.add(" ( " + word0 + " " + sign + " " + word1 + " ) ");
                this.subQueries.add(word0 + " " + sign + " " + word1);
            }
        }

        infixQuery = queryParts.get(0);

        for(int j = 1; j < queryParts.size(); j++){
            if(!signs.empty()) {
                infixQuery = " ( " + infixQuery + signs.pop() + queryParts.get(j) + " ) ";
            }else
                infixQuery += " " + queryParts.get(j).toUpperCase();
        }

        return infixQuery;
    }

    //Retrieves list of documents from data structure for a specific word
    private ArrayList<WordDocument> getDocumentsWhereWordExists(String word){
        try{
            return dictionary.get(word).getDocumentWhereWordExistsIn();
        }catch (NullPointerException ex){
            return new ArrayList<WordDocument>();
        }
    }

    //Concats two ArrayLists into one without duplicates
    private void addWithoutDuplicate(ArrayList<WordDocument> addTo, ArrayList<WordDocument> addFrom) {
        for (WordDocument x : addFrom)
            if (!addTo.contains(x))
                addTo.add(x);
    }

    //This method is responsible for ordering result of a query
    private ArrayList<WordDocument> orderBy(ArrayList<WordDocument> listToOrder, String orderProperty, String orderDirection){
        ArrayList<WordDocument> orderedList;

        int propertyIndex = this.properties.indexOf(orderProperty);
        int orderDirectionIndex = this.orderDirections.indexOf(orderDirection);

        if(propertyIndex != -1 && orderDirectionIndex != -1){
            if(propertyIndex == 0){
                setRelevanceForDocuments(listToOrder);
                orderedList = BubbleSort.sort(listToOrder, propertyIndex, orderDirectionIndex);
            }else
                orderedList = BubbleSort.sort(listToOrder, propertyIndex, orderDirectionIndex);
        }
        else
            throw new IllegalArgumentException();

        return orderedList;
    }

    //Converts an ArrayList of WordDocumets to an ArrayList of Documents
    private ArrayList<Document> convertToDocumentsList(ArrayList<WordDocument> list){
        ArrayList<Document> convertedList = new ArrayList<Document>();

        for(WordDocument x : list)
            convertedList.add(x.getDocument());

        return convertedList;
    }

    //This method checks is entered query is an advanced query of not
    //Advanced query is query where sign operators(+ - |) are used
    private boolean isAdvancedQuery(String enteredQuery){
        try{
            Character firstChar = enteredQuery.charAt(0);
            Character secondChar = enteredQuery.charAt(1);

            return isSignOperator(firstChar.toString()) && secondChar.equals(' ');
        }catch (StringIndexOutOfBoundsException ex){
           return false;
        }
    }


    private ArrayList<WordDocument> intersection(ArrayList<WordDocument> docsOfFirstTerm, ArrayList<WordDocument> docsOfSecondTerm){
        ArrayList<WordDocument> temp = copyArrayList(docsOfFirstTerm);
        temp.retainAll(docsOfSecondTerm);

        return temp;
    }

    private ArrayList<WordDocument> union(ArrayList<WordDocument> docsOfFirstTerm, ArrayList<WordDocument> docsOfSecondTerm){
        ArrayList<WordDocument> temp = copyArrayList(docsOfFirstTerm);
        for (WordDocument x : docsOfSecondTerm)
            if (!temp.contains(x))
                temp.add(x);

        return temp;
    }

    private ArrayList<WordDocument> difference(ArrayList<WordDocument> docsOfFirstTerm, ArrayList<WordDocument> docsOfSecondTerm){
        ArrayList<WordDocument> temp = copyArrayList(docsOfFirstTerm);

        if(!temp.isEmpty())
            temp.removeAll(docsOfSecondTerm);
        else temp = docsOfSecondTerm;

        return temp;
    }

    private boolean isSignOperator(String s){
        return s.equals("+") || s.equals("|") || s.equals("-");
    }

    private ArrayList<WordDocument> copyArrayList(ArrayList<WordDocument> arrayToCopy){
        ArrayList<WordDocument> temp = new ArrayList<WordDocument>();

        for(WordDocument wd : arrayToCopy)
            temp.add(wd);

        return temp;
    }

    private boolean isOrderByCommand(String s){
        return s.toLowerCase().equals("orderby");
    }

    private void setRelevanceForDocuments(ArrayList<WordDocument> result){
        for(WordDocument document : result){
            double relevance = 0.0;
            for(String subQuery : subQueries)
                relevance += calculateRelevance(document, subQuery);
            document.changeRelevance(relevance);
        }
    }

    private double calculateRelevance(WordDocument document, String subQuery){
        String[] splitedQuery = subQuery.split(" ");
        String searchTerm0 = splitedQuery[0];
        String searchTerm1 = "";
        if(advancedQuery)
            searchTerm1 = splitedQuery[2];

        if(!advancedQuery)
            return termFrequency(document, searchTerm0) * inverceDocumentFrequency(searchTerm0);
        else if(splitedQuery[1].equals("|") || splitedQuery[1].equals("+")){
            double relevanceTerm0 = termFrequency(document, searchTerm0) * inverceDocumentFrequency(searchTerm0);
            double relevanceTerm1 = termFrequency(document, searchTerm1) * inverceDocumentFrequency(searchTerm1);

            return relevanceTerm0 + relevanceTerm1;

        }else if(splitedQuery[1].equals("-"))
            return termFrequency(document, searchTerm0) * inverceDocumentFrequency(searchTerm0);
        else
            return 0;
    }

    private double termFrequency(WordDocument document, String searchTerm){
        int numberOfOccurrences = 0;

        for(WordDocument wd : getDocumentsWhereWordExists(searchTerm)){
            if(wd.equals(document))
                numberOfOccurrences = wd.getNumberOfOccurrences();
        }

        return (double)numberOfOccurrences/(double)Documents.getWordCountInDocument(document.getDocument().name);
    }

    private double inverceDocumentFrequency(String searchTerm){
        return Math.log10((double)Documents.getDocumentsCount()/(double)getDocumentsWhereWordExists(searchTerm).size());
    }
}
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Cache for sub-query results
 */
public class Cache {
    public HashMap<CacheKey, ArrayList<WordDocument>> cache = new HashMap<CacheKey, ArrayList<WordDocument>>();

    /**
     * This method adds new entity to the cache
     * @param query is the query for which result will be stored, is a key
     * @param result is the result for a query that will be stored
     */
    public void addToCache(String query, ArrayList<WordDocument> result){
        CacheKey key = new CacheKey(query);
        cache.put(key, result);
    }

    /**
     * This method retrieves a value for a certain key
     * @param query is the key
     * @return ArrayList of WordDocument which is a result of query
     */
    public ArrayList<WordDocument> getCachedResult(String query){
        return cache.get(new CacheKey(query));
    }

    /**
     * This method checks is a query results is stored
     * @param query is the query that serve as a key
     * @return boolean true and false if
     */
    public boolean isCached(String query){
        CacheKey tempCacheKey = new CacheKey(query);
        return cache.containsKey(tempCacheKey);
    }
}
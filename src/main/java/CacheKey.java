public class CacheKey {
    private String query;

    public CacheKey(String query){
        this.query = query;
    }

    @Override
    public boolean equals(Object obj) {
        return this.hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        String[] split = query.split(" ");
        int hashCode = 0;

        for(String s : split)
            hashCode += s.hashCode();

        return hashCode;
    }
}

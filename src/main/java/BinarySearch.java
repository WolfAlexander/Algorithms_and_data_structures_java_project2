import java.util.ArrayList;

public class BinarySearch<T extends Comparable, K extends Comparable> {
    /**
     * This is the search method of binary search
     * @param list is the ArrayList where value has to be found
     * @param value is the value that has to be fount
     * @return position of the element if it is found or returns position of where an
     *          element has to be inserted
     */
    public int search(ArrayList<T> list, K value){ return search(list, value, 0, list.size());}

    private int search(ArrayList<T> list, K value, int lo, int hi){
        if(hi <= lo)
            return  -1+(-1*lo);

        int middle = lo + (hi - lo) / 2;
        int comp = list.get(middle).compareTo(value);

        if(comp > 0)
            return search(list, value, lo, middle);
        else if(comp < 0)
            return search(list, value, middle+1, hi);
        else
            return middle;
    }
}
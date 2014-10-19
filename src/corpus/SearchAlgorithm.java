package corpus; 

import java.util.Iterator; 

public abstract class SearchAlgorithm implements Iterator<Link>, Iterable<Link> {
    public abstract void doSearch(String[] keywords);  
}



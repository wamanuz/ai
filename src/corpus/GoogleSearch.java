package corpus; 


import org.json.simple.*; 
import org.json.simple.parser.*; 

import java.net.URL; 
import java.net.HttpURLConnection; 

import java.io.BufferedReader; 
import java.io.InputStreamReader; 
import java.io.*; 

import java.util.ArrayList; 
import java.util.Iterator; 



public class GoogleSearch extends SearchAlgorithm {

    public static final String KEY = "AIzaSyB_wEtOOIu8k8nDoVMFKdSqEHybIwhYOq8"; // Very secret! 
    public static final String CX =  "017207336898649127236:bpnl3dcb0ro"; // Search engine id, not secret? 

    private String URL = "https://www.googleapis.com/customsearch/v1?" + 
                          "key=" + KEY + 
                          "&cx=" + CX; 

    private static final int NUM = 10;   // The number of results that is returned, [0, 10] 
    private static final int LIMIT = 10; // Defines the maximum amount of searches 
    private int start = 1;               // The index of the first result to return 


    private String[] keywords;          // The keywords to search 
    private ArrayList<Link> links;      // Store links in this 
    private int index;                  // The index that will be returned next 

    public GoogleSearch() {
        links = new ArrayList<Link>(); 
        index = 0; 
    }

    public void doSearch(String[] keywords) {
        if(keywords.length == 0) return; 
        this.keywords = keywords; 

        String query = keywords[0]; 
        for(int i=1; i<keywords.length; i++) 
            query += "+" + keywords[i]; 

        try {
            URL url = new URL(URL + "&q=" + query + "&start=" + start + "&num=" + NUM); 
            start += NUM; 

            HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
            conn.setRequestProperty("Referer", "wamanuz.se"); // needed because of reasons 

            BufferedReader in;// = new BufferedReader(new FileReader(new File("response.json"))); 
            in = new BufferedReader(new InputStreamReader(conn.getInputStream())); 
         
            StringBuilder response = new StringBuilder(); 
            String inputLine; 
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine); 
            }

            JSONParser parser = new JSONParser(); 
            JSONObject json = (JSONObject) parser.parse(response.toString()); 
            JSONArray items = (JSONArray) json.get("items"); 
            
            for(Object o : items) {
                JSONObject jo = (JSONObject) o;  
                links.add(new Link((String) jo.get("link"), (String) jo.get("snippet"))); 
            }
            in.close(); 
        }
        catch(Exception e) {
            e.printStackTrace(); 
        }
    }

    public Iterator<Link> iterator() {
        return this; 
    }
    public boolean hasNext() {
        if(index < links.size()) 
            return true; 
        else {
            if(start/NUM > LIMIT) 
                return false; 
            doSearch(keywords); 
            return index < links.size(); 
        }
    }

    public Link next() {
        index++; 
        return links.get(index-1); 
    }

    public void remove() {
        // ??? 
    }
    public static void main(String[] args) {

        GoogleSearch gs = new GoogleSearch(); 
        String[] queries = { "banana", "vehicle" }; 
        gs.doSearch(queries); 

        for(Link l : gs) {
            System.out.println(l.getURL() + " " + l.getSummary()); 
        }
    }
}



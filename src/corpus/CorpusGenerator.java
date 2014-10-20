package corpus;

import java.lang.StringBuilder; 

import java.util.Arrays; 
import java.util.HashMap; 


/** 
 *  java CorpusGenerator [-s search_algorithm] [-e text_extraction_algorithm] keywords 
 *
 *  Generates a corpus from given keywords and prints it to stdout. 
 *
 *  -s search_algoritm  Specifies the search algorithm that is used. 
 *                      Different algorithms could behave differently, 
 *                      e.g. only choose matches on specified sites, 
 *                      use different search engines or use the 
 *                      keywords differently. 
 *
 *  -e text_extraction_algorithm 
 *                      The algorithm that determines what text that 
 *                      is extracted from a link. 
 *
 *   keywords           The keywords that are going to be used. 
 *
 */
public class CorpusGenerator {
	private static final String default_algorithm = "boilerpipe";
	
    public static void main(String[] args) throws ExtractionException {
        String runoptions = "Usage: java CorpusGenerator [-s search_algorithm] [-e text_extraction_algorithm] keywords";

        // The default algorithms 
        SearchAlgorithm sa = new GoogleSearch(); 
        ExtractionAlgorithm ea; //= TODO new ALGORITHM OJBECT HERE, 


        // Hashmaps to save algorithms that are implemented 
        HashMap<String, SearchAlgorithm> searchAlgorithms = new HashMap<>(); 
        //searchAlgorithms.put("bing", new BingSearch()); 
        searchAlgorithms.put("google", new GoogleSearch()); 

        HashMap<String, ExtractionAlgorithm> extractionAlgorithms = new HashMap<>(); 
        extractionAlgorithms.put("boilerpipe", new BoilerpipeExtractor());
		
		ea = extractionAlgorithms.get(default_algorithm);

        // Parse the options 
        int argsi = 0; 
        for(argsi=0; argsi<args.length; argsi++) {  
            try {
                // Reached end of options 
                if(!args[argsi].startsWith("-")) {
                    break; 
                }

                if("-s".equals(args[argsi])) { 
                    if(searchAlgorithms.containsKey(args[argsi+1])) {
                        sa = searchAlgorithms.get(args[argsi+1]); 
                    }
                    else {
                        System.err.println("Error: Invalid search algorithm: " + args[argsi+1]); 
                        System.exit(1); 
                    }
                    argsi += 1; 
                }
                else if("-e".equals(args[argsi])) {
                    if(extractionAlgorithms.containsKey(args[argsi+1])) {
                        ea = extractionAlgorithms.get(args[argsi+1]); 
                    }
                    else {
                        System.err.println("Error: Invalid extraction algorithm: " + args[argsi+1]); 
                        System.exit(1); 
                    }
                    argsi += 1; 
                }
                else {
                    System.err.println("Error: No option " + args[argsi]); 
                    System.err.println(runoptions);
                    System.exit(1); 
                }
            }
            catch(ArrayIndexOutOfBoundsException e) {
                System.err.println("Error: Invalid options"); 
                System.err.println(runoptions);
                System.exit(1); 
            }
        }

        if(argsi == args.length) {
            System.err.println("Error: No keywords specified"); 
            System.err.println(runoptions);
            System.exit(1); 
        }
        String[] keywords = Arrays.copyOfRange(args, argsi, args.length); 


        // TODO doSearch may return an arraylist of Link-objects 
        // The Link-objects will contain the summary of the search 

        // Another (probably better) solution would have SearchAlgorithms 
        // implement Iterable, that way you you loop over sa.next() and have 
        // the SA-object determine when it needs to query for more results.

        sa.doSearch(keywords); 
        StringBuilder corpus = new StringBuilder(); 
        int N = 10000; // ? 

        while(sa.hasNext() && corpus.length() < N) {
            Link l = sa.next(); 
            corpus.append(ea.extract(l, keywords)); 
        }

        // Print the corpus so that the user can direct it to wherever, 
        // instead of forcing them to usie specific files 
        System.out.println(corpus);  
    }
}




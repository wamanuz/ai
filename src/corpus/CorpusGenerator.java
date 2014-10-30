package corpus;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.util.Arrays; 
import java.util.HashMap; 
import java.util.Map;


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
	
    public static void main(String[] args) {
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
        extractionAlgorithms.put("sentence", new SentenceExtractor());
		
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
        int N = 1000000; // ? 

		Map<String, OutputStream> files = new HashMap<>();
		Map<String, Integer> lengths = new HashMap<>();

		String keywordsStr = "";
		for (String keyword : keywords) {
			keywordsStr += "_" + keyword;
		}

		try {
			for (String key : extractionAlgorithms.keySet()) {
				OutputStream out = new FileOutputStream(key + keywordsStr + ".corpus");
				files.put(key, out);
				lengths.put(key, 0);
			}

			int corpusLength = 0;
			while (sa.hasNext() && corpusLength < N) {
				Link l = sa.next();

				System.out.println(l.getURL());

				for (String key : extractionAlgorithms.keySet()) {
					ExtractionAlgorithm extractor = extractionAlgorithms.get(key);
					OutputStream out = files.get(key);
					try {
						String extracted = extractor.extract(l, keywords);
						lengths.put(key, lengths.get(key) + extracted.length());

						out.write(extracted.getBytes());
						out.write(10);
					} catch (IOException | ExtractionException ex) {
						System.err.println(ex.getMessage());
					} catch (RuntimeException | Error ex) {
						// Catches unknown exceptions and errors like 
						// IllegalArgumentException and StackOverflowError etc.
						System.err.println("Unknown error");
						System.err.println(l.getURL());
						ex.printStackTrace();
					}
				}
				corpusLength = Integer.MAX_VALUE;
				for (String key : extractionAlgorithms.keySet()) {
					corpusLength = Math.min(corpusLength, lengths.get(key));
				}
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} finally {
			for (OutputStream out  : files.values()) {
				try {
					out.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}




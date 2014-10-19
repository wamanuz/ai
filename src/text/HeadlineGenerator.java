package text;

import java.io.*;
import java.util.*;

/**
 *  java HeadlineGenerator [-a text_generation_algorithm] corpus_file keywords
 *
 *  Generates a corpus from given keywords and prints it to stdout.
 *
 *  -a text_generator_algorithm
 *                      Specifies the algorithm that determines how the
 *                      headline is generated.
 *
 *   corpus_file        Specifies the corpus to be used.
 *
 *   keywords           The keywords that are going to be used.
 *
 */
public class HeadlineGenerator {
	/**
	 * One of:
	 *   most-likely
	 *   template (not implemented yet)
	 *   uchimoto (not implemented ever?)
	 */
	private static final String default_algorithm = "most-likely";
	
	/**
	 */
	private static void printUsage() {
		System.out.println("Usage:");
		System.out.println("  java HeadlineGenerator [-a algorithm] corpus_file keywords...");
	}

	/**
	 */
    public static void main(String[] args) throws IOException {
		TextGenerator tg = null;
		
        // Hashmaps to save algorithms that are implemented
        HashMap<String, TextGenerator> textGenerators = new HashMap<>();
		textGenerators.put("most-likely", new MostLikelyGenerator());
		textGenerators.put("template", new TemplateGenerator());
        textGenerators.put("uchimoto", new UchimotoGenerator());

        String corpus = null;
        int argsi = 0;

        // Parse the options
        for(; argsi<args.length; argsi++) {
            try {
                // Reached end of options
                if(!args[argsi].startsWith("-")) {
                    break;
                }

                if("-a".equals(args[argsi])) {
                    if(textGenerators.containsKey(args[argsi+1])) {
                        tg = textGenerators.get(args[argsi+1]);
                    }
                    else {
                        System.err.println("Error: Invalid text generation algorithm: " + args[argsi+1]);
                        System.exit(1);
                    }
                    argsi += 1;
                }
            }
            catch(ArrayIndexOutOfBoundsException e) {
                System.err.println("Error: Invalid options");
				printUsage();
                System.exit(1);
            }
        }
		if (tg == null) {
			tg = textGenerators.get(default_algorithm);
		}

        if(argsi == args.length) {
            System.err.println("Error: No corpus specified");
			printUsage();
            System.exit(1);
        }
        corpus = args[argsi];
        argsi += 1;

        if(argsi == args.length) {
            System.err.println("Error: No keywords specified");
			printUsage();
            System.exit(1);
        }
        String[] keywords = Arrays.copyOfRange(args, argsi, args.length);

        // Add the corpus to the generator. We don't want to add it earlier
        // (to all algorithms in the HashMap) since it will propably do
        // some pre-processing.

        tg.addCorpus(corpus);

        String headline = tg.generate(keywords);
        System.out.println(headline);
    }
}

// class NWordsGenerator extends TextGenerator {
// 	private HashMap<String, Double> table = null;
	
//     public NWordsGenerator() {
// 		super();
//     }

//     public void addCorpus(String filename) {
// 		BufferedReader file = null;
		
// 		try
// 		{
// 			file = new BufferedReader(new FileReader(filename));

// 			this.table = buildNWordsTable(file);
// 		}
// 		catch (IOException x)
// 		{
// 		}
// 		finally
// 		{
// 			try
// 			{
// 				if (file != null) {
// 					file.close();
// 				}
// 			}
// 			catch (IOException x)
// 			{
// 			}
// 		}
//     }

//     public String generate(String[] keywords) {
//         String s = "Template! ";
//         for(String ss : keywords)
//             s += ss + " ";
//         return s;
//     }
// }

class TemplateGenerator extends TextGenerator {
    public TemplateGenerator() {
		super();
    }
	
    public void addCorpus(String corpusfile) {

    }

    public String generate(String[] keywords) {
        String s = "Template! ";
        for(String ss : keywords)
            s += ss + " ";
        return s;
    }
}

class UchimotoGenerator extends TextGenerator {
    public UchimotoGenerator() {
		super();
    }
	
    public void addCorpus(String corpusfile) {
		
    }
	
    public String generate(String[] keywords) {
        String s = "Uchimoto! ";
        for(String ss : keywords)
            s += ss + " ";
        return s;
    }
}

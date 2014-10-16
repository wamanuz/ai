
import java.io.*;
import java.lang.*;
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
	 *   template
	 *   uchimoto
	 */
	private static final String default_algorithm = "uchimoto";
	
	/**
	 */
	private static void printUsage() {
		System.out.println("Usage:");
		System.out.println("  java HeadlineGenerator [-a algorithm] corpus_file keywords...");
	}

	/**
	 */
    public static void main(String[] args) {
		TextGenerator tg = null;
		
        // Hashmaps to save algorithms that are implemented
        HashMap<String, TextGenerator> textGenerators = new HashMap<>();
		textGenerators.put("template", new TemplateGenerator());
        textGenerators.put("uchimoto", new UchimotoGenerator());  // I don't even know if I spelt is name correctly

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

/**
 */
abstract class TextGenerator {
	/**
	 */
	protected static final char first_char = '$';
	/**
	 */
	protected static final char last_char = '^';
	/**
	 */
	private static final int word_size = 5;
	
	/**
	 */
    public TextGenerator() {
	}

    /**
     * Adds the corpus file to the generator. Should probably be
     * overridden if you want to do pre-processing on it.
    */
    public abstract void addCorpus(String corpusfile);

    /**
     * Generates a headline from a given corpus and keywords. Should
     * be overridden.
    */
    public abstract String generate(String[] keywords);

	/**
	 */
	private static void countWord(HashMap<String, Integer> counts, final char[] chars) {
		String key = new String(chars);

		counts.put(key, counts.containsKey(key) ? counts.get(key) + 1 : 1);
	}
	/**
	 */
	protected static HashMap<String, Double> buildNWordsTable(final BufferedReader file) throws IOException {
		int total = 0;
		
		final HashMap<String, Integer> counts = new HashMap<String, Integer>();
		{
			String line;
			final char[] chars = new char[word_size];
			
			while ((line = file.readLine()) != null) {
				// skip sentences that are too short
				if (line.length() < word_size - 2) continue;
				
				final String text = line.toUpperCase();
				// TODO this could be made better
				if (text.length() == word_size - 2) {
					chars[0] = first_char;
					text.getChars(0, word_size - 2, chars, 1);
					chars[word_size - 1] = last_char;
					countWord(counts, chars);
				} else {
					chars[0] = first_char;
					text.getChars(0, word_size - 1, chars, 1);
					countWord(counts, chars);
					for (int i = 0; i < text.length() - word_size; ++i) {
						text.getChars(i, i + word_size, chars, 0);
						countWord(counts, chars);
					}
					text.getChars(text.length() - (word_size - 1), text.length(), chars, 0);
					chars[word_size - 1] = last_char;
					countWord(counts, chars);
				}
			}
		}
		final double inv_total = 1. / total;
		
		final HashMap<String, Double> table = new HashMap<String, Double>(counts.size());
		{
			final Iterator it = counts.entrySet().iterator();
			
			while (it.hasNext()) {
				final Map.Entry pairs = (Map.Entry)it.next();
				
				table.put((String)pairs.getKey(), (Double)pairs.getValue() * inv_total);
			}
		}
		return table;
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

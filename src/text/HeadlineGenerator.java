
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
    public static void main(String[] args) {
		TextGenerator tg = null;
		
        // Hashmaps to save algorithms that are implemented
        HashMap<String, TextGenerator> textGenerators = new HashMap<>();
		textGenerators.put("most-likely", new MostLikelyGenerator());
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
 * A HashMap with 3 keys.
 */
class MultiMap<K1, K2, K3, V> {
	private HashMap<K1, HashMap<K2, HashMap<K3, V>>> table = new HashMap<K1, HashMap<K2, HashMap<K3, V>>>();

	public MultiMap() {
	}

	/**
	 */
	public HashMap<K3, V> get(K1 k1, K2 k2) {
		HashMap<K2, HashMap<K3, V>> map = table.get(k1);
		if (map == null) return null;
		return map.get(k2);
	}
	/**
	 */
	public V get(K1 k1, K2 k2, K3 k3) {
		HashMap<K2, HashMap<K3, V>> map = table.get(k1);
		if (map == null) return null;
		HashMap<K3, V> list = map.get(k2);
		if (list == null) return null;
		return list.get(k3);
	}
	/**
	 */
	public Iterator<V> iterator() {
		// THIS FUNCTION IS SO UGLY, YUCK!
		final HashMap<K1, HashMap<K2, HashMap<K3, V>>> _table = table;
		final Iterator<K1> _it1 = _table.keySet().iterator();
		if (_it1.hasNext())
		{
			final HashMap<K2, HashMap<K3, V>> _map = _table.get(_it1.next());
			final Iterator<K2> _it2 = _map.keySet().iterator();
			if (_it2.hasNext())
			{
				final HashMap<K3, V> _list = _map.get(_it2.next());
				final Iterator<K3> _it3 = _list.keySet().iterator();
				if (_it3.hasNext())
				{
					return new Iterator<V>() {
						HashMap<K1, HashMap<K2, HashMap<K3, V>>> table = _table;
						HashMap<K2, HashMap<K3, V>> map = _map;
						HashMap<K3, V> list = _list;
						Iterator<K1> it1 = _it1;
						Iterator<K2> it2 = _it2;
						Iterator<K3> it3 = _it3;

						// goto!
						private V step1() {
							if (it1.hasNext())
							{
								map = table.get(it1.next());
								it2 = map.keySet().iterator();
								return step2();
							}
							return null;
						}
						// goto!
						private V step2() {
							if (it2.hasNext())
							{
								list = map.get(it2.next());
								it3 = list.keySet().iterator();
								return step3();
							}
							return step1();
						}
						// goto!
						private V step3() {
							if (it3.hasNext())
							{
								return list.get(it3.next());
							}
							return step2();
						}
						
						public boolean hasNext() {
							return it3.hasNext() || it2.hasNext() || it1.hasNext();
						}
						public V next() {
							return step3();
						}
						public void remove() {
						}
					};
				}
			}
		}
		return new Iterator<V>() {
			public boolean hasNext() {
				return false;
			}
			public V next() {
				return null;
			}
			public void remove() {
			}
		};
	}
	/**
	 */
	public void put(K1 k1, K2 k2, K3 k3, V v) {
		HashMap<K2, HashMap<K3, V>> map = table.get(k1);
		if (map == null)
		{
			map = new HashMap<K2, HashMap<K3, V>>();
			table.put(k1, map);
		}
		HashMap<K3, V> list = map.get(k2);
		if (list == null)
		{
			list = new HashMap<K3, V>();
			map.put(k2, list);
		}
		list.put(k3, v);
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
	 * Currently not used.
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

class MostLikelyGenerator extends TextGenerator {
	/**
	 */
	private static class Choice
	{
		public Choice parent;
		public double probability; // best probability so far
		public String word; // actual word

		public Choice(Choice parent, double probability, String word) {
			this.parent = parent;
			this.probability = probability;
			this.word = word;
		}
	}
	/**
	 * Note that a 'Word' is not one word but three.
	 */
	private static class Word
	{
		public int count = 0; // how many times we have registered this word
		public double probability; // its proportion of all words
	}
	
	private MultiMap<String, String, String, Word> table;
	
    public MostLikelyGenerator() {
		super();
    }

	/**
	 * This is a generic function.
	 */
	private static MultiMap<String, String, String, Word> buildPosteriorTable(BufferedReader file) throws IOException {
		int total = 0; // total word count
		
		final MultiMap<String, String, String, Word> table = new MultiMap<String, String, String, Word>();
		{
			String line;
			
			while ((line = file.readLine()) != null) {
				final String capital_line = line.toUpperCase();
				
				final String[] words = capital_line.split("\\s+"); // split line on whitespace

				// skip sentences with no words
				if (words.length == 0) continue;

				String pprevious = null;
				String previous = null;

				for (String string : words) {
					Word word = table.get(pprevious, previous, string);
					if (word == null)
					{
						word = new Word();
						table.put(pprevious, previous, string, word);
					}
					++word.count;
					++total;
					
					pprevious = previous;
					previous = string;
				}
				// end of sentence
				Word word = table.get(pprevious, previous, null);
				if (word == null)
				{
					word = new Word();
					table.put(pprevious, previous, null, word);
				}
				++word.count;
				++total;
			}
		}
		// if no words were parsed then skip this since we otherwise divide by zero :/
		if (total == 0) return table;
		
		final double inv_total = 1. / total;
		Iterator<Word> it = table.iterator();

		while (it.hasNext())
		{
			Word word = it.next();

			word.probability = word.count * inv_total;
		}
		return table;
	}
	
    public void addCorpus(String filename) {
		BufferedReader file = null;
		
		try
		{
			file = new BufferedReader(new FileReader(filename));
			
			this.table = buildPosteriorTable(file);
		}
		catch (IOException x)
		{
			System.err.println(x);
		}
		finally
		{
			try
			{
				if (file != null) {
					file.close();
				}
			}
			catch (IOException x)
			{
				System.err.println(x);
			}
		}
    }

    public String generate(String[] keywords) {
		if (table == null) return null;

		ArrayList<HashMap<String, Choice>> steps = new ArrayList<HashMap<String, Choice>>();
		
		Choice init_choice = new Choice(null, 1, null);

		double highest_continuation_probability = 0.;
		{
			HashMap<String, Choice> choices = new HashMap<String, Choice>();
			
			// get possible continuations
			HashMap<String, Word> words = table.get(null, init_choice.word);
			
			Iterator<String> it = words.keySet().iterator();
			
			while (it.hasNext())
			{
				String key = it.next();
				Word word = words.get(key);
				
				Choice choice = new Choice(init_choice, word.probability, key);
				choices.put(key, choice);

				if (word.probability > highest_continuation_probability)
				{
					highest_continuation_probability = word.probability;
				}
			}
			steps.add(choices);
		}
		double highest_probability = 0.;
		Choice most_likely_outcome = null;

		// for as long as there might be a continuation that is better than the current best complete sentence...
		while (highest_continuation_probability > highest_probability)
		{
			highest_continuation_probability = 0.;
			
			HashMap<String, Choice> previous_choices = steps.get(steps.size() - 1);
			HashMap<String, Choice> choices = new HashMap<String, Choice>();

			Iterator<String> previous_it = previous_choices.keySet().iterator();

			while (previous_it.hasNext())
			{
				String previous_key = previous_it.next();
				Choice previous_choice = previous_choices.get(previous_key);

				// get possible continuations
				HashMap<String, Word> words = table.get(previous_choice.parent.word, previous_key); // or '..., previous_choice.word);'

				// dont do anything if there is no continuation
				//
				// this could be the case when we have reached a complete
				// sentence but there is a potentially better one
				if (words == null) continue;

				Iterator<String> it = words.keySet().iterator();
				
				while (it.hasNext())
				{
					String key = it.next();
					Word word = words.get(key);

					double probability = previous_choice.probability * word.probability;
					
					Choice choice = choices.get(key);
					if (choice == null)
					{
						choice = new Choice(previous_choice, probability, key);
						choices.put(key, choice);

						if (probability > highest_continuation_probability)
						{
							highest_continuation_probability = probability;
						}
						if (key == null) // last word of a sentence
						{
							if (probability > highest_probability)
							{
								highest_probability = probability;
								most_likely_outcome = choice;
							}
						}
					}
					else
					{
						if (probability > choice.probability)
						{
							choice.parent = previous_choice;
							choice.probability = probability;
							choice.word = key;
							
							if (probability > highest_continuation_probability)
							{
								highest_continuation_probability = probability;
							}
							if (key == null) // last word of a sentence
							{
								if (probability > highest_probability)
								{
									highest_probability = probability;
									most_likely_outcome = choice;
								}
							}
						}
					}
				}
			}
			steps.add(choices);
		}
		// in case there is no most likely outcome...
		if (most_likely_outcome == null) return null;

		Choice choice = most_likely_outcome.parent;
		String sentence = "";

		while (choice.parent != null)
		{
			sentence = choice.word + " " + sentence;

			choice = choice.parent;
		}
		return sentence;
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

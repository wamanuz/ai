package text;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


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
    public abstract void addCorpus(String corpusfile) throws IOException;

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

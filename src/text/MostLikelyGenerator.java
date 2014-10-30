package text;

import java.io.*;
import java.util.*;


/**
 * A HashMap with 3 keys.
 */
class MultiMap<K1, K2, K3, V> {
	private HashMap<K1, HashMap<K2, HashMap<K3, V>>> table = new HashMap<K1, HashMap<K2, HashMap<K3, V>>>();

	public MultiMap() {
	}

	/**
	 * Pretty bad function.
	 */
	public HashMap<K1, HashMap<K2, HashMap<K3, V>>> get() {
		return table;
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

class MostLikelyGenerator extends TextGenerator {
	/**
	 */
	private static class Choice
	{
		public Choice parent;
		public double probability; // best probability so far
		public String word; // actual word
		public int checkpoint;

		public Choice(final Choice parent, final double probability, final String word, final int checkpoint) {
			this.parent = parent;
			this.probability = probability;
			this.word = word;
			this.checkpoint = checkpoint;
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

				// skip sentences with too few words
				// the 2 is arbitrary
				if (words.length < 2) continue;

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
		
		// final double inv_total = 1. / total;
		final double log_total = Math.log(total);
		Iterator<Word> it = table.iterator();

		while (it.hasNext())
		{
			Word word = it.next();

			// word.probability = word.count * inv_total;
			word.probability = Math.log(word.count) - log_total;
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

	private class Sentence
	{
		public String string;
		public double probability;

		Sentence(final String string, final double probability) {
			this.string = string;
			this.probability = probability;
		}
	}

	// private String find_sentence(final String[] checkpoints) {
	private Sentence find_sentence(final String[] checkpoints, final double at_least_this_probable) {
		final PriorityQueue<Choice> queue = new PriorityQueue<Choice>(100, new Comparator<Choice>() {
				@Override
				public int compare(final Choice a, final Choice b)
				{
					// bigger choice is returned as smaller so as to get an decreasing list
					if (a.probability < b.probability) return  1;
					if (a.probability > b.probability) return -1;
					return 0;
				}
			});
		final Choice first_choice = new Choice(null, Math.log(1.), null, 0);
		final Choice second_choice = new Choice(first_choice, Math.log(1.), null, 0);

		queue.add(second_choice);

		Choice outcome = null;

		// THIS LIMITS THE NUMBER OF ITERATIONS
		// THE MORE ITERATIONS THE MORE LIKELY IT IS TO FIND A SENTENCE
		final int max_reps = 2000000; // 2000000 is very large
		int reps = 0;

		while (queue.size() > 0 &&
			   outcome == null &&
			   ++reps < max_reps)
		{
			final Choice previous_choice = queue.poll();

			//if (previous_choice.probability < at_least_this_probable) break;
			
			// get possible continuations
			final HashMap<String, Word> continuations = table.get(previous_choice.parent.word, previous_choice.word); // or '..., previous_choice.word);'

			if (continuations == null)
			{
				// System.err.println("err: no continuations!");
				
				continue;
			}
			final Iterator<String> it = continuations.keySet().iterator();
				
			while (it.hasNext())
			{
				final String key = it.next();
				final Word word = continuations.get(key);

				// log space
				final double probability = previous_choice.probability + word.probability;
				
				Choice choice = new Choice(previous_choice, probability, key, previous_choice.checkpoint);
				
				// if (key == null ? key == checkpoints[previous_choice.checkpoint] : key.equals(checkpoints[previous_choice.checkpoint]))
				if (key == null || checkpoints[previous_choice.checkpoint] == null ? key == checkpoints[previous_choice.checkpoint] : key.contains(checkpoints[previous_choice.checkpoint]))
				{
					// bump it to the next checkpoint
					++choice.checkpoint;
				}
				// got all checkpoints yet?
				if (choice.checkpoint == checkpoints.length)
				{
					outcome = choice;
				}
				else
				{
					queue.add(choice);
				}
			}
		}
		// debug prints
		// System.err.print("dbg: ");
		// System.err.print(reps);
		// System.err.println(" reps!");

		// in case there is no most likely outcome...
		if (outcome == null)
		{
			// System.err.println("err: didn't find any likely outcome!");
			
			return new Sentence(null, Math.log(0.));
		}

		Choice choice = outcome.parent;
		String sentence = "";

		while (choice != second_choice)
		{
			sentence = choice.word + " " + sentence;

			choice = choice.parent;
		}
		return new Sentence(sentence, outcome.probability);
	}

	private void swap(final int[] indices, final int i, final int j) {
		final int tmp = indices[i];
		indices[i] = indices[j];
		indices[j] = tmp;
	}
	private void permute(final int[] indices, final int k, final ArrayList<int[]> permutations) {
		for (int i = k; i < indices.length; ++i)
		{
			swap(indices, i, k);
			// is it a null place?
			// do we have enough keywords?
			if (indices[k] == -1 && k != 0)
			// if (indices[k] == -1 && k >= indices.length - 1)
			// if (indices[k] == -1 && (indices.length - 1 == 1 ? k >= 1 : k >= 2))
			{
				final int[] permutation = new int[k];
				System.arraycopy(indices, 0, permutation, 0, k);
				permutations.add(permutation);
			}
			permute(indices, k + 1, permutations);
			swap(indices, k, i);
		}
	}
	private String[][] permute(final String[] items) {
		final ArrayList<int[]> permutations = new ArrayList<int[]>();
		{
			final int[] indices = new int[items.length + 1];
			{
				for (int i = 0; i < items.length + 1; ++i)
				{
					indices[i] = i - 1;
				}
			}
			permute(indices, 0, permutations);
		}
		String[][] ps = new String[permutations.size()][];
		{
			for (int pi = 0; pi < permutations.size(); ++pi)
			{
				final int[] permutation = permutations.get(pi);

				ps[pi] = new String[permutation.length + 1];
				{
					for (int i = 0; i < permutation.length; ++i)
					{
						ps[pi][i] = permutation[i] != -1 ? items[permutation[i]] : "-";
					}
					ps[pi][permutation.length] = null;
				}
			}
		}
		return ps;
	}

    public String generate(String[] in_keywords) {
		if (table == null) return null;

		final String[] keywords = new String[in_keywords.length];
		{
			for (int i = 0; i < in_keywords.length; ++i)
			{
				keywords[i] = in_keywords[i].toUpperCase();
			}
		}
		final String[][] permutations = permute(keywords);

		Sentence best_sentence = new Sentence(null, Math.log(0.));

		for (final String[] checkpoints : permutations)
		{
			// System.err.print("dbg: trying '");
			// for (final String str : checkpoints)
			// {
			// 	System.err.print(str);
			// 	System.err.print(" ");
			// }
			// System.err.println("'");
			final Sentence sentence = find_sentence(checkpoints, best_sentence.probability);

			System.err.print("dbg: ");
			System.err.println(sentence.string);

			if (sentence.probability > best_sentence.probability)
			{
				best_sentence = sentence;
			}
		}
		System.err.print("dbg: best probability is ");
		System.err.print(Math.exp(best_sentence.probability));
		System.err.print(" ( = ");
		System.err.print(best_sentence.probability);
		System.err.println(" in log space)");
		return best_sentence.string;
		// final String[] keywords = new String[in_keywords.length + 1];
		// {
		// 	for (int i = 0; i < in_keywords.length; ++i)
		// 	{
		// 		keywords[i] = in_keywords[i].toUpperCase();
		// 	}
		// 	keywords[in_keywords.length] = null;
		// }

		// String sentence = find_sentence(keywords);

		// return sentence;
    }
}

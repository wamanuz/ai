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
		// int total = 0; // total word count
		
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
					// ++total;
					
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
				// ++total;
			}
		}
		// if no words were parsed then skip this since we otherwise divide by zero :/
		// if (total == 0) return table;
		
		// final double inv_total = 1. / total;
		// Iterator<Word> it = table.iterator();

		// while (it.hasNext())
		// {
		// 	Word word = it.next();

		// 	word.probability = word.count * inv_total;
		// }

		HashMap<String, HashMap<String, HashMap<String, Word>>> map1 = table.get();
		Iterator<String> it1 = map1.keySet().iterator();
		while (it1.hasNext())
		{
			String key1 = it1.next();
			
			HashMap<String, HashMap<String, Word>> map2 = map1.get(key1);
			Iterator<String> it2 = map2.keySet().iterator();
			while (it2.hasNext())
			{
				String key2 = it2.next();

				int total = 0;

				HashMap<String, Word> map3 = map2.get(key2);
				Iterator<String> it3 = map3.keySet().iterator();
				while (it3.hasNext())
				{
					String key3 = it3.next();
					Word word = map3.get(key3);

					total += word.count;
				}
				final double inv_total = 1. / total;
				
				it3 = map3.keySet().iterator();
				while (it3.hasNext())
				{
					String key3 = it3.next();
					Word word = map3.get(key3);

					word.probability = word.count * inv_total;
				}
			}
		}
		// TODO what if it doesn't has next?
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

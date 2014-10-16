package corpus;

public abstract class ExtractionAlgorithm {
	public abstract String extract(Link l, String[] keywords) throws ExtractionException;
}

package corpus;

public class Link {
	private final String url;
	private final String summary;
	// Maybe save the keywords here instead?

	public Link(String url, String summary) {
		this.url = url;
		this.summary = summary;
	}

	public String getURL() {
		return url;
	}

	public String getSummary() {
		return summary;
	}
}

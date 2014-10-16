package corpus;

public class ExtractionException extends Exception {
	private static final long serialVersionUID = 1L;

	public ExtractionException() {
		super();
	}

	public ExtractionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExtractionException(String message) {
		super(message);
	}

	public ExtractionException(Throwable cause) {
		super(cause);
	}
}

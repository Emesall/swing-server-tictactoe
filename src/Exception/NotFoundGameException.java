package Exception;

public class NotFoundGameException extends Exception {

	private String message;
	public NotFoundGameException(String message) {
		this.message=message;
	}
	public String getMessage() {
		return message;
	}
	
}

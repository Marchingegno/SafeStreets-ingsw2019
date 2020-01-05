package it.polimi.marcermarchiscianamotta.safestreets.util.interfaces;

/**
 * This interface must be implemented by the classes that wish to retrieve the results of the text recognizer.
 */
public interface ImageRecognitionInterface {
	/**
	 * This method is called when the image recognition process has terminated. The result is passed as parameter.
	 *
	 * @param result the String found by the text recognition process.
	 */
	void onTextRecognized(String[] result);
}

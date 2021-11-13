package decision_generator.exceptions;

import java.io.IOException;

public class FailedGenerateBookException extends RuntimeException {

    public FailedGenerateBookException(String message) {
        super(message);
    }

    public FailedGenerateBookException(String message, IOException e) {
        super(message, e);
    }
}

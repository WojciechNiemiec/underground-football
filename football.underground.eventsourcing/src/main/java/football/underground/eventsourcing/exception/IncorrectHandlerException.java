package football.underground.eventsourcing.exception;

public class IncorrectHandlerException extends RuntimeException {
    public IncorrectHandlerException() {
        super("Event handling class does not support event");
    }

    public IncorrectHandlerException(Throwable cause) {
        super("Event handling class fails to process event", cause);
    }
}

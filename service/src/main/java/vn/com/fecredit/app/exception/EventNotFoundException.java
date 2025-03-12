package vn.com.fecredit.app.exception;

public class EventNotFoundException extends RuntimeException {
    
    public EventNotFoundException(Long id) {
        super("Event not found with id: " + id);
    }

    public EventNotFoundException(String code) {
        super("Event not found with code: " + code);
    }
}

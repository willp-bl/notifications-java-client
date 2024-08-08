package uk.gov.service.notify;

public class NotificationClientException extends Exception {
    private static final long serialVersionUID = 2L;

    public NotificationClientException(String message) {
        super(message);
    }

    NotificationClientException(Exception ex) {
        super(ex);
    }
}

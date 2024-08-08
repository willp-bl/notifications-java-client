package uk.gov.service.notify;

public class NotificationClientHttpException extends NotificationClientException {
    private static final long serialVersionUID = 2L;
    private final int httpResult;

    NotificationClientHttpException(int httpResult, String message) {
        super("Status code: " + httpResult + " " + message);
        this.httpResult = httpResult;
    }

    public int getHttpResult()
    {
        return this.httpResult;
    }
}

package uk.gov.service.notify;

import uk.gov.service.notify.domain.NotifyApiErrorResponse;

public class NotificationClientHttpException extends NotificationClientException {
    private static final long serialVersionUID = 2L;
    private final int httpResult;
    private final NotifyApiErrorResponse apiResponse;

    NotificationClientHttpException(int httpResult, String message, NotifyApiErrorResponse apiResponse) {
        super("Status code: " + httpResult + " " + message);
        this.httpResult = httpResult;
        this.apiResponse = apiResponse;
    }

    public int getHttpResult()
    {
        return this.httpResult;
    }

    public NotifyApiErrorResponse getApiResponse() {
        return apiResponse;
    }
}

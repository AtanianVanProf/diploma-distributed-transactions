package am.diploma.saga.orchestrator.orchestrator.exception;

import lombok.Getter;

@Getter
public class ServiceCallException extends RuntimeException {

    private final String serviceName;
    private final String errorCode;
    private final String errorMessage;

    public ServiceCallException(String serviceName, String errorCode, String errorMessage) {
        super("Service call to " + serviceName + " failed: [" + errorCode + "] " + errorMessage);
        this.serviceName = serviceName;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}

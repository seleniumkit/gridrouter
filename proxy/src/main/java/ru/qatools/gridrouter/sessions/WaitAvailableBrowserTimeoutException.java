package ru.qatools.gridrouter.sessions;

/**
 * @author Ilya Sadykov
 */
public class WaitAvailableBrowserTimeoutException extends AvailableBrowserCheckExeption {
    public WaitAvailableBrowserTimeoutException(String message) {
        super(message);
    }

    public WaitAvailableBrowserTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}

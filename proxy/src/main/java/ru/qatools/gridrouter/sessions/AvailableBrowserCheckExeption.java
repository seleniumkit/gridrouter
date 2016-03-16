package ru.qatools.gridrouter.sessions;

/**
 * @author Ilya Sadykov
 */
public class AvailableBrowserCheckExeption extends RuntimeException {
    public AvailableBrowserCheckExeption(String message) {
        super(message);
    }

    public AvailableBrowserCheckExeption(String message, Throwable cause) {
        super(message, cause);
    }
}

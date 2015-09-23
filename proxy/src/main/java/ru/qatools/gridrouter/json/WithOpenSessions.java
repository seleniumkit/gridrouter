package ru.qatools.gridrouter.json;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public interface WithOpenSessions {

    int getOpenSessions();

    void setOpenSessions(int value);

    default void startSession() {
        synchronized (this) {
            setOpenSessions(getOpenSessions() + 1);
        }
    }

    default void stopSession() {
        synchronized (this) {
            setOpenSessions(getOpenSessions() - 1);
        }
    }
}

package ru.qatools.gridrouter.json;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 * @author Dmitry Baev charlie@yandex-team.ru
 */
public interface Describable {

    String getBrowserName();
    String getVersion();

    default String describe() {
        return getBrowserName() + (isEmpty(getVersion()) ? "" : "-" + getVersion());
    }
}

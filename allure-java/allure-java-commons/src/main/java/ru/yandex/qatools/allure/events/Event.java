package ru.yandex.qatools.allure.events;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 11.11.13
 */
public interface Event<T> {
    void process(T context);
}

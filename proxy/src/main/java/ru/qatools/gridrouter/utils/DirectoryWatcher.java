package ru.qatools.gridrouter.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

/**
 * @author Artem Eroshenko eroshenkoam@yandex-team.ru
 */
public class DirectoryWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryWatcher.class);

    public static Thread newWatcher(final Path directory, final String pathMatcher, final EventListener listener) {
        return new Thread() {
            @Override
            public void run() {
                FileSystem fileSystem = FileSystems.getDefault();
                try (WatchService watcher = fileSystem.newWatchService()) {
                    directory.register(watcher,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_MODIFY,
                            StandardWatchEventKinds.ENTRY_DELETE
                    );
                    while (true) {
                        WatchKey key;
                        try {
                            key = watcher.take();
                        } catch (InterruptedException e) {
                            break;
                        }
                        Path directory = (Path) key.watchable();
                        key.pollEvents()
                                .stream()
                                .filter(event -> {
                                    Path candidatePath = (Path) event.context();
                                    PathMatcher matcher = fileSystem.getPathMatcher(pathMatcher);
                                    return matcher.matches(candidatePath.getFileName());
                                })
                                .forEach(event -> {
                                    Path path = directory.resolve((Path) event.context());
                                    listener.onChange(event.kind(), path);
                                });
                        if (!key.reset()) {
                            break;
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("Error in directory watcher", e);
                }
            }
        };
    }

    public interface EventListener {

        void onChange(WatchEvent.Kind kind, Path path);

    }
}

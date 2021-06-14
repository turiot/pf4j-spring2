package org.pf4j.spring2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;

public class FileWatcher implements UpdateWatcher {

    private static final Logger log = LoggerFactory.getLogger(FileWatcher.class);

    private SpringPluginManager springPluginManager;
	
    private WatchService watchService;

	@Override
	public void setSpringPluginManager(SpringPluginManager springPluginManager) {
		this.springPluginManager = springPluginManager;
	}

    @Override
    public void run() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
			var path = springPluginManager.getPluginsRoot();
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (var event : key.pollEvents()) {
                    if (event.context().toString().endsWith(".zip")) { // to pick only plugins, not deployments
                        log.info("New plugin detected : {}", event.context());
                        var pluginPath = path.resolve(event.context().toString());
                        try {
                            springPluginManager.update(pluginPath);
                        }
                        catch (UpdateException e) {
                            log.error("Update problem", e);
                        }
                    }
                }
                key.reset();
            }
        }
        catch (ClosedWatchServiceException e) {
            log.debug("Closing fileWatcher");
        }
        catch (IOException | InterruptedException e) {
            log.error("Error fileWatcher", e);
        }
    }

	@Override
    public void stop() {
        try {
            watchService.close(); // close thread via exception
        }
        catch (IOException e) {
            log.error("Error fileWatcher close", e);
        }
    }

}

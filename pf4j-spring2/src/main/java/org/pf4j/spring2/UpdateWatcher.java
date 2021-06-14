package org.pf4j.spring2;

public interface UpdateWatcher extends Runnable {

	void setSpringPluginManager(SpringPluginManager springPluginManager);
	
	@Override
	void run();
	
	void stop();

}

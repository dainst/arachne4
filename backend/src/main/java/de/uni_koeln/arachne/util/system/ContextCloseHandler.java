package de.uni_koeln.arachne.util.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import com.mysql.jdbc.AbandonedConnectionCleanupThread;

/**
 * Class to handle context close events and allow for custom code execution.
 * Currently it's only use is the shutdown of the mysql <code>AbandonedConnectionCleanupThread</code>. 
 */
@Component
class ContextClosedHandler implements ApplicationListener<ContextClosedEvent> {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextClosedHandler.class);
	
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
    	try {
    		LOGGER.info("Shutting down 'AbandonedConnectionCleanupThread'...");
    	    AbandonedConnectionCleanupThread.shutdown();
    	} catch (InterruptedException e) {
    	    LOGGER.warn("SEVERE problem cleaning up: " + e.getMessage());
    	    e.printStackTrace();
    	}
    }       
}

package managed.fleet.api.services;

import managed.fleet.api.interfaces.IWebserverSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virtualbox_6_1.ISession;
import org.virtualbox_6_1.IVirtualBox;
import org.virtualbox_6_1.VirtualBoxManager;

import java.util.List;
import java.util.function.Supplier;

public class VboxWebserverSession implements IWebserverSession {
    private static Logger logger;
    private VirtualBoxManager hostManager;

    public VboxWebserverSession() {
        logger = LoggerFactory.getLogger(this.getClass());
        hostManager = VirtualBoxManager.createInstance(null);
    }

    @Override
    public void execute(Runnable Action) {
        try {
            connect();

            Action.run();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    public <T> T execute(Supplier<T> Action) {
        try {
            connect();

            return Action.get();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }

        return null;
    }

    @Override
    public ISession getSession() {
        return hostManager.getSessionObject();
    }

    @Override
    public IVirtualBox getVbox() {
        return hostManager.getVBox();
    }

    private void connect() {
        try {
            logger.info("Connecting to Web severs");

            hostManager.connect("http://192.168.0.111:18083", null, null);
        } catch (Exception e) {
            logger.error("Web server is unavailable");
        }
    }

    private void disconnect() {
        logger.info("Disconnecting from Web severs");

        hostManager.disconnect();
    }
}

package managed.fleet.api.services;

import managed.fleet.api.interfaces.IWebserverSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virtualbox_6_1.ISession;
import org.virtualbox_6_1.IVirtualBox;
import org.virtualbox_6_1.VirtualBoxManager;

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

    @Override
    public void connect() {
        try {
            hostManager.connect("http://192.168.0.111:18083", null, null);
        } catch (Exception e) {
            logger.error("Web server is unavailable");
        }
    }

    @Override
    public void disconnect() {
        logger.info("Disconnecting from Web severs");

        hostManager.disconnect();
    }

    @Override
    public ISession getSession() {
        return hostManager.getSessionObject();
    }

    @Override
    public IVirtualBox getVbox() {
        return hostManager.getVBox();
    }
}

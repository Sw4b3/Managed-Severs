package managed.fleet.api.services;

import managed.fleet.api.interfaces.IWebserverSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virtualbox_6_1.VirtualBoxManager;

public class VboxWebserverSession implements IWebserverSession {
    private static Logger logger;

    public VboxWebserverSession() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public void execute(Runnable Action, VirtualBoxManager hostManager) {
        try {
            connect(hostManager);

            Action.run();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect(hostManager);
        }
    }

    @Override
    public void connect(VirtualBoxManager hostManager) {
        try {
            hostManager.connect("http://192.168.0.111:18083", null, null);
        } catch (Exception e) {
            logger.error("Web server is unavailable");
        }
    }

    @Override
    public void disconnect(VirtualBoxManager hostManager) {
        logger.info("Disconnecting from Web severs");

        hostManager.disconnect();
    }
}

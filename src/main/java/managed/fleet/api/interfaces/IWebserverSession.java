package managed.fleet.api.interfaces;

import org.virtualbox_6_1.VirtualBoxManager;

public interface IWebserverSession {
    void execute(Runnable Action, VirtualBoxManager hostManager);

    void connect(VirtualBoxManager hostManager);

    void disconnect(VirtualBoxManager hostManager);
}

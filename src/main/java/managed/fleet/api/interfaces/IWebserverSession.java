package managed.fleet.api.interfaces;

import org.virtualbox_6_1.ISession;
import org.virtualbox_6_1.IVirtualBox;

public interface IWebserverSession {
    void execute(Runnable Action);

    void connect();

    void disconnect();

    ISession getSession();

    IVirtualBox getVbox();
}

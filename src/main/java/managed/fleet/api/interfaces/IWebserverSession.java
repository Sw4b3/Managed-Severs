package managed.fleet.api.interfaces;

import org.virtualbox_6_1.ISession;
import org.virtualbox_6_1.IVirtualBox;

import java.util.function.Supplier;

public interface IWebserverSession {
    void execute(Runnable Action);
    <T> T execute  (Supplier<T> Action);

    void connect();

    void disconnect();

    ISession getSession();

    IVirtualBox getVbox();
}

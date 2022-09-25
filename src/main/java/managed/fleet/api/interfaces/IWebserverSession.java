package managed.fleet.api.interfaces;

import org.virtualbox_6_1.ISession;
import org.virtualbox_6_1.IVirtualBox;

import java.util.function.Supplier;

public interface IWebserverSession {
    void execute(Runnable Action);

    <TResponse> TResponse execute(Supplier<TResponse> Action);

    ISession getSession();

    IVirtualBox getVbox();
}

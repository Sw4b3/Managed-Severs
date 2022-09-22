package managed.fleet.api.services;

import managed.fleet.api.interfaces.IHostManager;
import managed.fleet.api.interfaces.IHostService;
import org.virtualbox_6_1.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VBoxHostManager implements IHostManager {
    private final IHostService hostService;
    private VirtualBoxManager hostManager;
    private IVirtualBox vbox;
     IProgress progress;

    public VBoxHostManager() {
        hostService = new HostService();

        connect();
    }

    public void startHost(String machineName) {
        System.out.println("Starting up Host");

        launchMachine(machineName);
    }

    @Override
    public void terminateHost(String machineName) {
        System.out.println("Terminating Host");

        shutdownMachine(machineName);
    }

    @Override
    public void registerClient() {
        createMachine();
    }

    public void deregisterClient() {

    }

    private void connect() {
        try {
            hostManager = VirtualBoxManager.createInstance(null);

            hostManager.connect("http://192.168.0.111:18083", null, null);

            vbox = hostManager.getVBox();
        } catch (Exception e) {
            System.out.println("Web server is unavailable");
        }
    }

    private void launchMachine(String machineName) {
        if (!hostService.machineExists(machineName)) {
            return;
        }

        IMachine machine = vbox.findMachine(machineName);

        ISession session = hostManager.getSessionObject();

        if (machine.getSessionState() == SessionState.Unlocked) {
            try {
                IProgress progress = machine.launchVMProcess(session, "gui", null);

                progress.waitForCompletion(1000);
            } finally {
                waitToUnlock(session, machine);
            }
        } else {
            System.out.println("Host is already running");
        }
    }

    private void shutdownMachine(String machineName) {
        if (!hostService.machineExists(machineName)) {
            return;
        }

        IMachine machine = vbox.findMachine(machineName);

        MachineState state = machine.getState();

        ISession session = hostManager.getSessionObject();

        machine.lockMachine(session, LockType.Shared);

        try {
            if (state.value() >= MachineState.FirstOnline.value() && state.value() <= MachineState.LastOnline.value()) {

                IProgress progress = session.getConsole().powerDown();

                wait(progress);
            }
        } finally {
            waitToUnlock(session, machine);
        }
    }

    private void createMachine() {
        try {
            System.out.println("Creating Host");

            var hostName = "vm_" + UUID.randomUUID() + "";

            var host = vbox.createMachine(null, hostName, null, "Other_64", null);

            host.setMemorySize(2048L);

            host.getGraphicsAdapter().setVRAMSize(18L);

            System.out.println("Configuring Storage");

            var storageController = host.addStorageController("SATA", StorageBus.SATA);

            storageController.setPortCount(2L);

            var hddMedium = vbox.createMedium("vdi", "D:/VirtualBox VMs/vm/", AccessMode.ReadWrite, DeviceType.HardDisk);

            var imagesMedium = vbox.getDVDImages();

            List<MediumVariant> hddMediumVariants = new ArrayList<>();

            hddMediumVariants.add(MediumVariant.Fixed);
            hddMediumVariants.add(MediumVariant.VdiZeroExpand);

            var progress = hddMedium.createBaseStorage(20737418240L, hddMediumVariants);

            progress.waitForCompletion(1000000000);

            if (hddMedium.getState().name().equals("NotCreated")) {
                throw new Exception(progress.getErrorInfo().getText());
            }

            System.out.println("Configuring Network Adapter");

            var networkAdapter = host.getNetworkAdapter(0L);

            networkAdapter.setAdapterType(NetworkAdapterType.I82540EM);

            networkAdapter.setAttachmentType(NetworkAttachmentType.Bridged);

            networkAdapter.setBridgedInterface("Killer E2500 Gigabit Ethernet Controller");

            host.saveSettings();

            System.out.println("Registering Host");

            hostManager.getVBox().registerMachine(host);

            System.out.println("Attaching Storage");

            var session = hostManager.getSessionObject();

            host.lockMachine(session, LockType.Shared);

            host = session.getMachine();

            host.attachDevice("SATA", 0, 0, DeviceType.HardDisk, hddMedium);

            host.attachDevice("SATA", 1, 0, DeviceType.DVD, imagesMedium.get(1));

            host.saveSettings();

            session.unlockMachine();

            System.out.println("Host Created");

        } catch (Exception e) {
            System.out.println("Failed to create Host:: " + e + "");

            e.printStackTrace();
        }
    }

    private void waitToUnlock(ISession session, IMachine machine) {
        session.unlockMachine();

        SessionState sessionState = machine.getSessionState();

        while (!SessionState.Unlocked.equals(sessionState)) {
            sessionState = machine.getSessionState();

            try {
                System.err.println("Waiting for session unlock...[" + sessionState.name() + "][" + machine.getName() + "]");

                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for session to be unlocked");
            }
        }
    }

    private void wait(IProgress progress) {
        //make this available for the caller
        this.progress = progress;

        progress.waitForCompletion(-1);

        if (progress.getResultCode() != 0) {
            System.err.println("Operation failed: " + progress.getErrorInfo().getText());
        }
    }
}

package managed.fleet.api.services;

import managed.fleet.api.interfaces.IHostManager;
import managed.fleet.api.interfaces.IHostService;
import managed.fleet.api.interfaces.IWebserverSession;
import managed.fleet.api.models.HostConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virtualbox_6_1.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VBoxHostManagerService implements IHostManager {
    private final IHostService hostService;
    private final IWebserverSession webserverSession;
    private VirtualBoxManager hostManager;
    private static IVirtualBox vbox;
    private static Logger logger;

    public VBoxHostManagerService() {
        logger = LoggerFactory.getLogger(this.getClass());
        hostService = new VboxHostService();
        webserverSession = new VboxWebserverSession();
        hostManager = VirtualBoxManager.createInstance(null);
    }

    @Override
    public void startHost(String machineName) {
        webserverSession.connect(hostManager);

        logger.info("Starting up Host");

        launchMachine(machineName);

        webserverSession.disconnect(hostManager);
    }

    @Override
    public void terminateHost(String machineName) {
        webserverSession.connect(hostManager);

        logger.info("Terminating Host");

        shutdownMachine(machineName);

        webserverSession.disconnect(hostManager);
    }

    @Override
    public void registerClient(HostConfiguration hostConfiguration) {
        webserverSession.connect(hostManager);

        logger.info("Creating Host");

        createMachine(hostConfiguration);

        webserverSession.disconnect(hostManager);
    }

    @Override
    public void deregisterClient(String machineName) {
        webserverSession.connect(hostManager);

        logger.info("Unregistering Host");

        deregisteredMachine(machineName);

        webserverSession.disconnect(hostManager);
    }

    private void launchMachine(String machineName) {
        vbox = hostManager.getVBox();

        if (!hostService.machineExists(machineName))
            return;

        var machine = vbox.findMachine(machineName);

        var session = hostManager.getSessionObject();

        if (machine.getSessionState() == SessionState.Unlocked) {
            try {
                var progress = machine.launchVMProcess(session, "gui", null);

                progress.waitForCompletion(1000);
            } finally {
                waitToUnlock(session, machine);
            }
        } else {
            logger.warn("Host is already running");
        }
    }

    private void shutdownMachine(String machineName) {
        vbox = hostManager.getVBox();

        if (!hostService.machineExists(machineName))
            return;

        var machine = vbox.findMachine(machineName);

        var state = machine.getState();

        var session = hostManager.getSessionObject();

        machine.lockMachine(session, LockType.Shared);

        try {
            if (state.value() >= MachineState.FirstOnline.value() && state.value() <= MachineState.LastOnline.value()) {

                var progress = session.getConsole().powerDown();

                wait(progress);
            }
        } finally {
            waitToUnlock(session, machine);
        }
    }

    private void createMachine(HostConfiguration hostConfiguration) {
        vbox = hostManager.getVBox();

        scanMachineImages();

        try {
            var hostName = "vm_" + UUID.randomUUID() + "";

            var host = vbox.createMachine(null, hostName, null, "Other_64", null);

            host.setMemorySize(hostConfiguration.getMemoryConfiguration());

            host.getGraphicsAdapter().setVRAMSize(18L);

            logger.info("Configuring Storage");

            var storageController = host.addStorageController("SATA", StorageBus.SATA);

            storageController.setPortCount(2L);

            var hddMedium = vbox.createMedium("vdi", "D:/VirtualBox VMs/vm/", AccessMode.ReadWrite, DeviceType.HardDisk);

            var imagesMedium = getMachineImageMedium(vbox.getDVDImages(), hostConfiguration.getOSImage());

            List<MediumVariant> hddMediumVariants = new ArrayList<>();

            hddMediumVariants.add(MediumVariant.Fixed);
            hddMediumVariants.add(MediumVariant.VdiZeroExpand);

            var progress = hddMedium.createBaseStorage(hostConfiguration.getStorageCapacity(), hddMediumVariants);

            progress.waitForCompletion(1000000000);

            if (hddMedium.getState().name().equals("NotCreated"))
                throw new Exception(progress.getErrorInfo().getText());

            logger.info("Configuring Network Adapter");

            var networkAdapter = host.getNetworkAdapter(0L);

            networkAdapter.setAdapterType(NetworkAdapterType.I82540EM);

            networkAdapter.setAttachmentType(NetworkAttachmentType.Bridged);

            networkAdapter.setBridgedInterface("Killer E2500 Gigabit Ethernet Controller");

            host.saveSettings();

            logger.info("Registering Host");

            hostManager.getVBox().registerMachine(host);

            logger.info("Attaching Storage");

            var session = hostManager.getSessionObject();

            host.lockMachine(session, LockType.Shared);

            host = session.getMachine();

            host.attachDevice("SATA", 0, 0, DeviceType.HardDisk, hddMedium);

            host.attachDevice("SATA", 1, 0, DeviceType.DVD, imagesMedium);

            host.saveSettings();

            session.unlockMachine();

            logger.info("Host Created");

        } catch (Exception e) {
            logger.error("Failed to create Host::" + e);
            e.printStackTrace();
        }
    }

    private void deregisteredMachine(String machineName) {
        vbox = hostManager.getVBox();

        if (!hostService.machineExists(machineName))
            return;

        var machine = vbox.findMachine(machineName);

        machine.unregister(CleanupMode.Full);
    }

    private void waitToUnlock(ISession session, IMachine machine) {
        session.unlockMachine();

        var sessionState = machine.getSessionState();

        while (!SessionState.Unlocked.equals(sessionState)) {
            sessionState = machine.getSessionState();

            try {
                logger.info("Waiting for session unlock::" + sessionState.name() + "::" + machine.getName());

                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void wait(IProgress progress) {
        progress.waitForCompletion(-1);

        if (progress.getResultCode() != 0) {
            logger.warn("Operation failed: " + progress.getErrorInfo().getText());
        }
    }

    private void scanMachineImages() {
        var path = "D:/Machine Images/";

        File directoryPath = new File(path);

        var contents = directoryPath.list();

        for (int i = 0; i < (contents != null ? contents.length : 0); i++) {
            if (contents[i].toLowerCase().contains(".iso"))
                addMachineImage(path + contents[i]);
        }
    }

    private void addMachineImage(String path) {
        logger.info("Adding machine image::" + path);

        vbox.openMedium(path, DeviceType.DVD, AccessMode.ReadOnly, true);
    }

    private IMedium getMachineImageMedium(List<IMedium> images, String desiredImage) {
        for (var image : images) {
            if (image.getName().equals(desiredImage))
                return image;
        }

        throw new IllegalArgumentException();
    }
}

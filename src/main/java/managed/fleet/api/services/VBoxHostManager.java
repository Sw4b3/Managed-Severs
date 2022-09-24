package managed.fleet.api.services;

import managed.fleet.api.interfaces.IHostManager;
import managed.fleet.api.interfaces.IHostService;
import managed.fleet.api.models.HostConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.virtualbox_6_1.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VBoxHostManager implements IHostManager {
    private final IHostService hostService;
    private VirtualBoxManager hostManager;
    private IVirtualBox vbox;
    private static Logger logger;

    public VBoxHostManager() {
        logger = LoggerFactory.getLogger(this.getClass());
        hostService = new HostService();
        hostManager = VirtualBoxManager.createInstance(null);
    }

    public void startHost(String machineName) {
        logger.info("Starting up Host");

        launchMachine(machineName);
    }

    @Override
    public void terminateHost(String machineName) {
        logger.info("Terminating Host");

        shutdownMachine(machineName);
    }

    @Override
    public void registerClient(HostConfiguration hostConfiguration) {
        createMachine(hostConfiguration);
    }

    @Override
    public void deregisterClient(String machineName) {
        logger.info("Unregistering Host");

        deregisteredMachine(machineName);
    }

    private void connect() {
        try {
            logger.info("Connecting to Web severs");

            hostManager.connect("http://192.168.0.111:18083", null, null);

            vbox = hostManager.getVBox();
        } catch (Exception e) {
            logger.info("Web server is unavailable");
        }
    }

    private void disconnect() {
        logger.info("Disconnecting from Web severs");

        hostManager.disconnect();
    }

    private void launchMachine(String machineName) {
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
        connect();

        scanMachineImages();

        try {
            logger.info("Creating Host");

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
        } finally {
            disconnect();
        }
    }

    private void deregisteredMachine(String machineName) {
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

        for (int i = 0; i < contents.length; i++) {
            if (contents[i].toLowerCase().contains(".iso"))
                addMachineImage(path + contents[i]);
        }
    }

    private IMedium addMachineImage(String path) {
        logger.info("Adding machine image::" + path);

        return vbox.openMedium(path, DeviceType.DVD, AccessMode.ReadOnly, true);
    }

    private IMedium getMachineImageMedium(List<IMedium> images, String desiredImage) {
        for (var image : images) {
            if (image.getName() == desiredImage) ;
            return image;
        }

        throw new IllegalArgumentException();
    }
}

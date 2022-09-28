package managed.fleet.api.services;

import common.utlis.ConfigurationManger;
import managed.fleet.api.interfaces.IHostManager;
import managed.fleet.api.interfaces.IHostService;
import managed.fleet.api.interfaces.IInstanceConfigurationManager;
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
    private final IInstanceConfigurationManager instanceManagerService;
    private final IWebserverSession webserverSession;
    private static Logger logger;

    public VBoxHostManagerService() {
        logger = LoggerFactory.getLogger(this.getClass());
        hostService = new VboxHostService();
        instanceManagerService = new InstanceConfigurationManager();
        webserverSession = new VboxWebserverSession();
    }

    @Override
    public void startHost(String machineName) {
        logger.info("Starting up Host");

        webserverSession.execute(() -> launchMachine(machineName));
    }

    @Override
    public void terminateHost(String machineName) {
        logger.info("Terminating Host");

        webserverSession.execute(() -> shutdownMachine(machineName));
    }

    @Override
    public void registerClient(HostConfiguration hostConfiguration) {
        logger.info("Creating Host");

        webserverSession.execute(() -> createMachine(hostConfiguration));
    }

    @Override
    public void deregisterClient(String machineName) {
        logger.info("Unregistering Host");

        webserverSession.execute(() -> deregisteredMachine(machineName));
    }

    private void launchMachine(String machineName) {
        var vbox = webserverSession.getVbox();

        if (!hostService.hostExists(machineName))
            return;

        var machine = vbox.findMachine(machineName);

        var session = webserverSession.getSession();

        if (machine.getSessionState() == SessionState.Unlocked) {
            try {
                var progress = machine.launchVMProcess(session, "gui", null);

                progress.waitForCompletion(10000);
            } finally {
                waitToUnlock(session, machine);
            }
        } else {
            logger.warn("Host is already running");
        }
    }

    private void shutdownMachine(String machineName) {
        var vbox = webserverSession.getVbox();

        if (!hostService.hostExists(machineName))
            return;

        var machine = vbox.findMachine(machineName);

        var state = machine.getState();

        var session = webserverSession.getSession();

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
        var vbox = webserverSession.getVbox();

        try {
            var instanceConfiguration = instanceManagerService.getInstanceConfiguration(hostConfiguration);

            var hostName = "vm_" + UUID.randomUUID() + "";

            var host = vbox.createMachine(null, hostName, null, instanceConfiguration.geImageTypeConfiguration().getName(), null);

            host.setDescription(instanceConfiguration.geImageTypeConfiguration().getName() + "::"
                    + instanceConfiguration.getMemoryConfiguration() + "RAM::"
                    + instanceConfiguration.getCpuCount() + "Core CPU");

            host.setMemorySize(instanceConfiguration.getMemoryConfiguration());

            host.getGraphicsAdapter().setVRAMSize(128L);

            host.setCPUCount((long) instanceConfiguration.getCpuCount());

            logger.info("Configuring Storage");

            var storageController = host.addStorageController("SATA", StorageBus.SATA);

            storageController.setPortCount(5L);

            var hddMedium = vbox.createMedium("vdi", ConfigurationManger.getSection("Path:VmSaveLocation").toString(), AccessMode.ReadWrite, DeviceType.HardDisk);

            List<MediumVariant> hddMediumVariants = new ArrayList<>();

            hddMediumVariants.add(MediumVariant.Fixed);
            hddMediumVariants.add(MediumVariant.VdiZeroExpand);

            var progress = hddMedium.createBaseStorage(instanceConfiguration.getStorageCapacity(), hddMediumVariants);

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

            vbox.registerMachine(host);

            logger.info("Attaching Storage");

            var session = webserverSession.getSession();

            host.lockMachine(session, LockType.Shared);

            host = session.getMachine();

            host.attachDevice("SATA", 0, 0, DeviceType.HardDisk, hddMedium);

            host.saveSettings();

            session.unlockMachine();

            logger.info("Host Created");

            runUnattendedInstallation(vbox, hostName, ConfigurationManger.getSection("Path:MachineImages").toString()
                    + instanceConfiguration.getOSImage());

            launchMachine(hostName);

        } catch (Exception e) {
            logger.error("Failed to create Host::" + e);
            e.printStackTrace();
        }
    }

    /*
     * https://www.virtualbox.org/ticket/20917
     * Tracking a bug related to unattended Installation regarding Ubuntu
     */
    private void runUnattendedInstallation(IVirtualBox vbox, String hostName, String IsoPath) {
        var unattended = vbox.createUnattendedInstaller();

        var machine = vbox.findMachine(hostName);

        unattended.setMachine(machine);
        unattended.setIsoPath(IsoPath);
        unattended.setFullUserName("vboxadmin");
        unattended.setUser("vboxadmin");
        unattended.setPassword("P@ssword12345!");
        unattended.setInstallGuestAdditions(true);
        unattended.setLocale("en_US");
        unattended.setTimeZone("South Africa Standard Time");
        unattended.setLanguage("en-us");

        logger.info("Host prepare");

        unattended.prepare();

        logger.info("Host construct Media");

        unattended.constructMedia();

        logger.info("Host reconfigure VM");

        unattended.reconfigureVM();

        logger.info("Host done");

        unattended.done();
    }

    private void deregisteredMachine(String machineName) {
        var vbox = webserverSession.getVbox();

        if (!hostService.hostExists(machineName))
            return;

        var machine = vbox.findMachine(machineName);

        machine.unregister(CleanupMode.Full);
    }

    private void waitToUnlock(ISession session, IMachine machine) {
        var sessionState = machine.getSessionState();

        if (SessionState.Spawning.equals(sessionState))
            return;

        session.unlockMachine();

        while (!SessionState.Unlocked.equals(sessionState)) {
            sessionState = machine.getSessionState();

            try {
                logger.info("Waiting for session unlock::" + sessionState.name() + "::" + machine.getName());

                Thread.sleep(30000L);
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
        var path = ConfigurationManger.getSection("Path:MachineImages").toString();

        File directoryPath = new File(path);

        var contents = directoryPath.list();

        for (int i = 0; i < (contents != null ? contents.length : 0); i++) {
            if (contents[i].toLowerCase().contains(".iso"))
                addMachineImage(path + contents[i]);
        }
    }

    private void addMachineImage(String path) {
        var vbox = webserverSession.getVbox();

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

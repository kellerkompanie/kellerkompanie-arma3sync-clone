package fr.soe.a3s.dao.repository;

import java.io.File;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import fr.soe.a3s.controller.ObservableCountInt;
import fr.soe.a3s.controller.ObservableText;
import fr.soe.a3s.controller.ObserverCountInt;
import fr.soe.a3s.controller.ObserverText;
import fr.soe.a3s.dao.A3SFilesAccessor;
import fr.soe.a3s.dao.DataAccessConstants;
import fr.soe.a3s.dao.FileAccessMethods;
import fr.soe.a3s.dao.connection.AutoConfigURLAccessMethods;
import fr.soe.a3s.dao.zip.DeleteZipBatchProcessor;
import fr.soe.a3s.dao.zip.ZipBatchProcessor;
import fr.soe.a3s.domain.Http;
import fr.soe.a3s.domain.repository.AutoConfig;
import fr.soe.a3s.domain.repository.Changelog;
import fr.soe.a3s.domain.repository.Changelogs;
import fr.soe.a3s.domain.repository.Event;
import fr.soe.a3s.domain.repository.Events;
import fr.soe.a3s.domain.repository.Repository;
import fr.soe.a3s.domain.repository.ServerInfo;
import fr.soe.a3s.domain.repository.SyncTreeDirectory;
import fr.soe.a3s.domain.repository.SyncTreeLeaf;
import fr.soe.a3s.domain.repository.SyncTreeNode;
import fr.soe.a3s.exception.CreateDirectoryException;
import fr.soe.a3s.exception.DeleteDirectoryException;

public class RepositoryBuildProcessor implements DataAccessConstants, ObservableCountInt, ObservableText {

	/** Parameters */
	private Repository repository = null;

	/** Variables for SHA1 computation */
	private RepositorySHA1Processor repositorySHA1Processor = null;
	private List<SyncTreeLeaf> updatedFiles = null;

	/** zsync files generation */
	private RepositoryZsyncProcessor repositoryZsyncFilesProcessor = null;
	private RepositoryDeleteZSyncProcessor repositoryDeleteZSyncProcessor = null;

	/** pbo files compression */
	private ZipBatchProcessor zipBatchProcessor = null;
	private DeleteZipBatchProcessor deleteZipBatchProcessor = null;
	private double compressionRatio;

	/** Variables for observableText and observableCount Interface */
	private ObserverText observerText;
	private ObserverCountInt observerCount;

	public void init(Repository repository) {
		this.repository = repository;
	}

	@SuppressWarnings("unchecked")
	public void run() throws Exception {

		assert (repository != null);
		assert (repository.getPath() != null);
		assert (new File(repository.getPath()).exists());

		// Read previous sync file
		File oldSyncFile = new File(repository.getPath() + "/" + A3S_FOlDER_NAME + "/" + SYNC_FILE_NAME);
		SyncTreeDirectory oldSync = null;
		if (oldSyncFile.exists()) {
			oldSync = (SyncTreeDirectory) A3SFilesAccessor.read(oldSyncFile);
		}

		// Read previous serverInfo file
		File oldServerInfoFile = new File(repository.getPath() + "/" + A3S_FOlDER_NAME + "/" + SERVERINFO_FILE_NAME);
		ServerInfo oldServerInfo = null;
		if (oldServerInfoFile.exists()) {
			oldServerInfo = (ServerInfo) A3SFilesAccessor.read(oldServerInfoFile);
		}

		// Read previous changelogs file
		File oldChangelogsFile = new File(repository.getPath() + "/" + A3S_FOlDER_NAME + "/" + CHANGELOGS_FILE_NAME);
		Changelogs oldChangelogs = null;
		if (oldChangelogsFile.exists()) {
			oldChangelogs = (Changelogs) A3SFilesAccessor.read(oldChangelogsFile);
		}

		// Read previous events file
		File oldEventsFile = new File(repository.getPath() + "/" + A3S_FOlDER_NAME + "/" + EVENTS_FILE_NAME);
		Events oldEvents = null;
		if (oldEventsFile.exists()) {
			oldEvents = (Events) A3SFilesAccessor.read(oldEventsFile);
		}

		/* Remove .a3s folder */
		File folderA3S = new File(repository.getPath() + "/" + A3S_FOlDER_NAME);
		if (folderA3S.exists()) {
			boolean deleted = FileAccessMethods.deleteDirectory(folderA3S);
			if (!deleted) {
				throw new DeleteDirectoryException(folderA3S);
			}
		}

		/* Generate new Sync */
		final SyncTreeDirectory sync = new SyncTreeDirectory(SyncTreeDirectory.RACINE, null);
		File[] subFiles = (new File(repository.getPath()).listFiles());
		if (subFiles != null) {
			for (File f : subFiles) {
				generateSync(repository.getExcludedFilesFromBuild(), sync, f);
			}
		}

		/* Extract new list of files */
		List<SyncTreeLeaf> leafsList = sync.getDeepSearchLeafsList();

		/* Determine totalNbFiles */
		int numberOfFiles = leafsList.size();

		/* Determine totalFilesSize */
		long totalFilesSize = FileUtils.sizeOfDirectory(new File(repository.getPath()));

		/* Determine SHA1 values for Sync */
		updateObserverText("Processing SHA1 signatures...");
		repositorySHA1Processor = new RepositorySHA1Processor();
		repositorySHA1Processor.init(leafsList, repository.getMapFilesForBuild(), false);
		repositorySHA1Processor.addObserverCount(this.observerCount);
		repositorySHA1Processor.run();

		/* Set updated files list */
		this.updatedFiles = repositorySHA1Processor.getUpdatedFiles();

		/* Set contentUpdated */
		boolean contentUpdated = repositorySHA1Processor.isContentUpdated();

		/* Generate new ServerInfo */
		final ServerInfo serverInfo = new ServerInfo();
		int revision = 1;
		if (oldServerInfo != null) {
			revision = oldServerInfo.getRevision() + 1;
		}
		serverInfo.setRevision(revision);
		serverInfo.setBuildDate(new Date());
		serverInfo.setNumberOfFiles(numberOfFiles);
		serverInfo.setTotalFilesSize(totalFilesSize);
		serverInfo.setNumberOfConnections(repository.getNumberOfConnections());
		serverInfo.setNoPartialFileTransfer((!repository.isUsePartialFileTransfer()));
		serverInfo.setRepositoryContentUpdated(contentUpdated);

		int index = repository.getPath().lastIndexOf(File.separator);
		String repositoryMainFolderName = repository.getPath().substring(index + 1);

		Iterator iterator = repository.getExcludedFoldersFromSync().iterator();
		while (iterator.hasNext()) {
			String path = (String) iterator.next();
			index = path.toLowerCase().indexOf(File.separator + repositoryMainFolderName.toLowerCase());
			String folderPath = path.substring(index + repositoryMainFolderName.length() + 2);
			folderPath = backslashReplace(folderPath);
			serverInfo.getHiddenFolderPaths().add(folderPath);
		}

		/* Generate new AutoConfig */
		final AutoConfig autoConfig = new AutoConfig();
		autoConfig.setRepositoryName(repository.getName());
		autoConfig.setProtocole(repository.getProtocol());
		autoConfig.getFavoriteServers().addAll(repository.getFavoriteServersSetToAutoconfig());

		/* Generate new Changelogs */
		final Changelogs changelogs = new Changelogs();
		if (oldChangelogs == null || oldChangelogs.getList().isEmpty()) {
			Changelog changelog = new Changelog();
			changelogs.getList().add(changelog);
			changelog.setRevision(revision);
			changelog.setBuildDate(new Date());
			changelog.setContentUpdated(contentUpdated);
			getAddonsByName(sync, changelog.getAddons());
			if (revision == 1) {
				for (String stg : changelog.getAddons()) {
					changelog.getNewAddons().add(stg);
				}
			}
		} else {
			Changelog changelog = new Changelog();
			changelogs.getList().addAll(oldChangelogs.getList());
			changelogs.getList().add(changelog);
			if (changelogs.getList().size() > 10) {
				changelogs.getList().remove(0);
			}
			changelog.setRevision(revision);
			changelog.setBuildDate(new Date());
			changelog.setContentUpdated(contentUpdated);
			getAddonsByName(sync, changelog.getAddons());
			Changelog previousChangelog = changelogs.getList().get(changelogs.getList().size() - 2);
			for (String stg : changelog.getAddons()) {
				if (!previousChangelog.getAddons().contains(stg)) {
					changelog.getNewAddons().add(stg);
				}
			}
			for (String stg : previousChangelog.getAddons()) {
				if (!changelog.getAddons().contains(stg)) {
					changelog.getDeletedAddons().add(stg);
				}
			}
			if (oldSync != null) {
				Map<String, SyncTreeDirectory> mapOldSync = new HashMap<String, SyncTreeDirectory>();// <AddonName,SyncTreeDirectory>
				getAddons(oldSync, mapOldSync);
				Map<String, SyncTreeDirectory> mapSync = new HashMap<String, SyncTreeDirectory>();// <AddonName,SyncTreeDirectory>
				getAddons(sync, mapSync);
				for (Iterator iter = mapSync.keySet().iterator(); iter.hasNext();) {
					String addonName = (String) iter.next();
					if (mapOldSync.containsKey(addonName)) {
						SyncTreeDirectory syncDirectory = mapSync.get(addonName);
						SyncTreeDirectory oldSyncDirectory = mapOldSync.get(addonName);
						List<SyncTreeLeaf> newLeafsList = syncDirectory.getDeepSearchLeafsList();
						Collections.sort(newLeafsList);
						List<SyncTreeLeaf> oldLeafsList = oldSyncDirectory.getDeepSearchLeafsList();
						Collections.sort(oldLeafsList);
						if (newLeafsList.size() != oldLeafsList.size()) {
							changelog.getUpdatedAddons().add(addonName);
						} else {
							for (int i = 0; i < newLeafsList.size(); i++) {
								if (!newLeafsList.get(i).getSha1().equals(oldLeafsList.get(i).getSha1())) {
									changelog.getUpdatedAddons().add(addonName);
									break;
								}
							}
						}
					}
				}
			}
		}

		/* Generate new Events */
		final Events events = new Events();
		if (oldEvents != null) {
			List<String> addonNames = new ArrayList<String>();
			getAddonsByName(sync, addonNames);
			for (Event oldEvent : oldEvents.getList()) {
				Event newEvent = new Event(oldEvent.getName());
				newEvent.setDescription(oldEvent.getDescription());
				Map<String, Boolean> oldMap = oldEvent.getAddonNames();
				for (Iterator<String> iter = oldMap.keySet().iterator(); iter.hasNext();) {
					String key = iter.next();
					Boolean value = oldMap.get(key);
					// Keep existing addon name in the repository
					if (addonNames.contains(key)) {
						newEvent.getAddonNames().put(key, value);
					}
				}
				newEvent.getUserconfigFolderNames().putAll(oldEvent.getUserconfigFolderNames());
				events.getList().add(newEvent);
			}
		}

		/* Repository update */
		repository.setSync(sync);
		repository.setServerInfo(serverInfo);
		String autoConfigURL = AutoConfigURLAccessMethods.determineAutoConfigUrl(repository.getProtocol());
		repository.setAutoConfigURL(autoConfigURL);
		repository.setChangelogs(changelogs);

		/* Determine .zsync files for HTTP based Repository */
		if (repository.getProtocol() instanceof Http) {
			updateObserverText("Processing *" + ZSYNC_EXTENSION + " files...");
			List<SyncTreeLeaf> list = new ArrayList<SyncTreeLeaf>();
			getZSyncFiles(leafsList, list);
			repositoryZsyncFilesProcessor = new RepositoryZsyncProcessor();
			repositoryZsyncFilesProcessor.init(list, repository.getProtocol());
			repositoryZsyncFilesProcessor.addObserverCount(observerCount);
			repositoryZsyncFilesProcessor.run();
		} else {
			updateObserverText("Deleting *" + ZSYNC_EXTENSION + " files...");
			repositoryDeleteZSyncProcessor = new RepositoryDeleteZSyncProcessor();
			repositoryDeleteZSyncProcessor.init(leafsList);
			repositoryDeleteZSyncProcessor.addObserverCount(observerCount);
			repositoryDeleteZSyncProcessor.run();
		}

		/* Determine .zip files */
		if (repository.isCompressed()) {
			updateObserverText("Processing *" + PBO_ZIP_EXTENSION + " files...");
			List<SyncTreeLeaf> list = new ArrayList<SyncTreeLeaf>();
			getPboFilesForCompression(leafsList, list);
			zipBatchProcessor = new ZipBatchProcessor();
			zipBatchProcessor.init(list);
			zipBatchProcessor.addObserverCount(observerCount);
			zipBatchProcessor.zipBatch();
		} else {
			observerText.update("Deleting *" + PBO_ZIP_EXTENSION + " files...");
			List<SyncTreeLeaf> list = new ArrayList<SyncTreeLeaf>();
			getPboFilesForDeletion(leafsList, list);
			deleteZipBatchProcessor = new DeleteZipBatchProcessor();
			deleteZipBatchProcessor.init(list);
			deleteZipBatchProcessor.addObserverCount(observerCount);
			deleteZipBatchProcessor.run();
		}

		/* Write files */
		File a3sFolder = new File(repository.getPath() + "/" + A3S_FOlDER_NAME);
		a3sFolder.mkdir();
		if (!a3sFolder.exists()) {
			throw new CreateDirectoryException(a3sFolder);
		}

		// Write Sync file
		File syncFile = new File(repository.getPath() + "/" + A3S_FOlDER_NAME + "/" + SYNC_FILE_NAME);
		A3SFilesAccessor.write(sync, syncFile);

		// Write ServerInfo file
		File serverInfoFile = new File(repository.getPath() + "/" + A3S_FOlDER_NAME + "/" + SERVERINFO_FILE_NAME);
		A3SFilesAccessor.write(serverInfo, serverInfoFile);

		// Write Changelogs file
		File changelogsFile = new File(repository.getPath() + "/" + A3S_FOlDER_NAME + "/" + CHANGELOGS_FILE_NAME);
		A3SFilesAccessor.write(changelogs, changelogsFile);

		// Write AutoConfig file
		File autoConfigFile = new File(repository.getPath() + "/" + A3S_FOlDER_NAME + "/" + AUTOCONFIG_FILE_NAME);
		A3SFilesAccessor.write(autoConfig, autoConfigFile);

		// Write Events file
		File eventsFile = new File(repository.getPath() + "/" + A3S_FOlDER_NAME + "/" + EVENTS_FILE_NAME);
		A3SFilesAccessor.write(events, eventsFile);
	}

	private String backslashReplace(String myStr) {

		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator = new StringCharacterIterator(myStr);
		char character = iterator.current();
		while (character != CharacterIterator.DONE) {
			if (character == '\\') {
				result.append("/");
			} else {
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}

	private void getAddonsByName(final SyncTreeDirectory syncTreeDirectory, List<String> newAddons) {

		for (SyncTreeNode node : syncTreeDirectory.getList()) {
			if (!node.isLeaf()) {
				SyncTreeDirectory directory = (SyncTreeDirectory) node;
				if (directory.isMarkAsAddon()) {
					newAddons.add(directory.getName());
				} else {
					getAddonsByName(directory, newAddons);
				}
			}
		}
	}

	private void getAddons(final SyncTreeDirectory syncTreeDirectory, Map<String, SyncTreeDirectory> map) {

		for (SyncTreeNode node : syncTreeDirectory.getList()) {
			if (!node.isLeaf()) {
				SyncTreeDirectory directory = (SyncTreeDirectory) node;
				if (directory.isMarkAsAddon()) {
					map.put(directory.getName(), directory);
				} else {
					getAddons(directory, map);
				}
			}
		}
	}

	private void generateSync(Set<String> excludedFilesFromBuild, final SyncTreeDirectory parent, final File file) {

		if (file.isDirectory()) {
			if (!file.getName().contains(A3S_FOlDER_NAME)) {// always true
				SyncTreeDirectory syncTreeDirectory = new SyncTreeDirectory(file.getName(), parent);
				parent.addTreeNode(syncTreeDirectory);
				syncTreeDirectory.setDestinationPath(file.getParentFile().getAbsolutePath());
				File[] subFiles = file.listFiles();
				if (subFiles != null) {
					for (File f : subFiles) {
						if (f.getName().toLowerCase().equals("addons")) {
							syncTreeDirectory.setMarkAsAddon(true);
						}
						generateSync(excludedFilesFromBuild, syncTreeDirectory, f);
					}
				}
			}
		}
		// exclude .zsync and .pbo.7z files
		else if (!file.getName().contains(ZSYNC_EXTENSION) && !file.getName().contains(PBO_ZIP_EXTENSION)
				&& !file.getName().contains(A3S_FOlDER_NAME)
				&& !excludedFilesFromBuild.contains(file.getAbsolutePath())) {

			final SyncTreeLeaf treeSyncTreeLeaf = new SyncTreeLeaf(file.getName(), parent);
			parent.addTreeNode(treeSyncTreeLeaf);
			treeSyncTreeLeaf.setDestinationPath(file.getParentFile().getAbsolutePath());
			long size = FileUtils.sizeOf(file);
			treeSyncTreeLeaf.setSize(size);
		}
	}

	private void determineCompressionRatio(SyncTreeNode node) {

		if (node.isLeaf()) {
			SyncTreeLeaf leaf = (SyncTreeLeaf) node;
			double ratio = (leaf.getSize() - leaf.getCompressedSize()) / leaf.getSize();
			compressionRatio = (compressionRatio + ratio) / 2;
		} else {
			SyncTreeDirectory directory = (SyncTreeDirectory) node;
			for (SyncTreeNode n : directory.getList()) {
				determineCompressionRatio(n);
			}
		}
	}

	private void getPboFilesForCompression(List<SyncTreeLeaf> leafsList, List<SyncTreeLeaf> list) {

		for (SyncTreeLeaf leaf : leafsList) {
			int index = leaf.getName().lastIndexOf(".");
			String extension = "";
			if (index != -1) {
				extension = leaf.getName().substring(index);
			}
			if (extension.toLowerCase().equals(PBO_EXTENSION) || extension.toLowerCase().equals(EBO_EXTENSION)) {
				File zipFile = new File(leaf.getDestinationPath() + "/" + leaf.getName() + ZIP_EXTENSION);
				boolean compute = false;
				if (updatedFiles.contains(leaf)) {
					// Force delete in case of the user has stopped the process
					// previously
					FileAccessMethods.deleteFile(zipFile);
					compute = true;
				} else if (!zipFile.exists()) {
					compute = true;
				} else {
					leaf.setCompressed(true);
					leaf.setCompressedSize(zipFile.length());
				}
				if (compute) {
					list.add(leaf);
				}
			}
		}
	}

	private void getPboFilesForDeletion(List<SyncTreeLeaf> leafsList, List<SyncTreeLeaf> list) {

		for (SyncTreeLeaf leaf : leafsList) {
			int index = leaf.getName().lastIndexOf(".");
			String extension = "";
			if (index != -1) {
				extension = leaf.getName().substring(index);
			}
			if (extension.toLowerCase().equals(PBO_EXTENSION) || extension.toLowerCase().equals(EBO_EXTENSION)) {
				File zipFile = new File(leaf.getDestinationPath() + "/" + leaf.getName() + ZIP_EXTENSION);
				if (zipFile.exists()) {
					list.add(leaf);
				}
				leaf.setCompressed(false);
			}
		}
	}

	private void getZSyncFiles(List<SyncTreeLeaf> leafsList, List<SyncTreeLeaf> list) {

		for (SyncTreeLeaf leaf : leafsList) {
			if (leaf.getDestinationPath() != null) {
				final File file = new File(leaf.getDestinationPath() + "/" + leaf.getName());
				if (file.exists()) {
					final File zsyncFile = new File(file.getParentFile() + "/" + file.getName() + ZSYNC_EXTENSION);
					boolean compute = false;
					if (updatedFiles.contains(leaf)) {
						compute = true;
					} else if (!zsyncFile.exists()) {
						compute = true;
					} else if (zsyncFile.length() == 0) {
						compute = true;
					}

					if (compute) {
						list.add(leaf);
					}
				}
			}
		}
	}

	public void cancel() {

		if (repositorySHA1Processor != null) {
			repositorySHA1Processor.cancel();
		}
		if (repositoryZsyncFilesProcessor != null) {
			repositoryZsyncFilesProcessor.cancel();
		}
		if (repositoryDeleteZSyncProcessor != null) {
			repositoryDeleteZSyncProcessor.cancel();
		}
		if (zipBatchProcessor != null) {
			zipBatchProcessor.cancel();
		}
		if (deleteZipBatchProcessor != null) {
			deleteZipBatchProcessor.cancel();
		}
	}

	/* Interface observableText */

	@Override
	public void addObserverText(ObserverText obs) {
		this.observerText = obs;
	}

	@Override
	public void updateObserverText(String text) {
		this.observerText.update(text);
	}

	/* Interface observableCount */

	@Override
	public void addObserverCount(ObserverCountInt obs) {
		this.observerCount = obs;
	}

	@Override
	public void updateObserverCount(int value) {
		// unused
	}
}

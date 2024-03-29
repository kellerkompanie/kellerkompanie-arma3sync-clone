package fr.soe.a3s.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.soe.a3s.constant.GameDLCs;
import fr.soe.a3s.constant.GameSystemFolders;
import fr.soe.a3s.constant.ModsetType;
import fr.soe.a3s.dao.AddonDAO;
import fr.soe.a3s.dao.ConfigurationDAO;
import fr.soe.a3s.dao.DataAccessConstants;
import fr.soe.a3s.dao.ProfileDAO;
import fr.soe.a3s.dao.repository.RepositoryDAO;
import fr.soe.a3s.domain.Addon;
import fr.soe.a3s.domain.Profile;
import fr.soe.a3s.domain.TreeDirectory;
import fr.soe.a3s.domain.TreeLeaf;
import fr.soe.a3s.domain.TreeNode;
import fr.soe.a3s.domain.repository.Repository;
import fr.soe.a3s.dto.TreeDirectoryDTO;
import fr.soe.a3s.dto.TreeLeafDTO;
import fr.soe.a3s.dto.TreeNodeDTO;

public class AddonService extends ObjectDTOtransformer implements DataAccessConstants {

	private final ConfigurationDAO configurationDAO = new ConfigurationDAO();
	private final ProfileDAO profileDAO = new ProfileDAO();
	private final AddonDAO addonDAO = new AddonDAO();
	private final RepositoryDAO repositoryDAO = new RepositoryDAO();
	private final List<String> excludedFilePathList = new ArrayList<String>();
	private TreeDirectory availabeAddonsTreeInstance;

	public void init() {

		addonDAO.getMap().clear();
		excludedFilePathList.clear();

		File arma3Directory = null;
		String profileName = configurationDAO.getConfiguration().getProfileName();
		Profile profile = profileDAO.getMap().get(profileName);
		if (profile != null) {
			String arma3ExePath = profile.getLauncherOptions().getArma3ExePath();
			if (arma3ExePath != null) {
				if (!arma3ExePath.isEmpty()) {
					arma3Directory = new File(arma3ExePath).getParentFile();
				}
			}
		}

		if (arma3Directory != null) {
			GameSystemFolders[] tab = GameSystemFolders.values();
			for (int i = 0; i < tab.length; i++) {
				File excludedFile = new File(arma3Directory + "/" + tab[i].toString());
				String osName = System.getProperty("os.name");
				if (osName.contains("Windows")) {
					this.excludedFilePathList.add(excludedFile.getAbsolutePath().toLowerCase());
				} else {
					this.excludedFilePathList.add(excludedFile.getAbsolutePath());
				}
			}
		}

		availabeAddonsTreeInstance = getAvailableAddonsTreeInstance();
	}

	private TreeDirectory getAvailableAddonsTreeInstance() {

		List<String> list = new ArrayList<String>();

		String profileName = configurationDAO.getConfiguration().getProfileName();
		Profile profile = profileDAO.getMap().get(profileName);
		if (profile != null) {
			Iterator iter = profile.getAddonSearchDirectories().iterator();
			while (iter.hasNext()) {
				list.add((String) iter.next());
			}
		}

		List<String> newList = new ArrayList<String>();

		for (int i = 0; i < list.size(); i++) {
			String ipath = list.get(i);
			String ipathForCompare = ipath;

			if (!new File(ipath).exists()) {
				continue;
			}

			String osName = System.getProperty("os.name");
			if (osName.toLowerCase().contains("windows")) {
				ipathForCompare = ipath.toLowerCase();
			}
			String pathToKeep = ipath;

			File iparentFile = new File(ipath).getParentFile();

			if (iparentFile == null) {
				continue;
			} else if (!iparentFile.exists()) {
				continue;
			}

			for (int j = 0; j < list.size(); j++) {
				String jpath = list.get(j);

				if (!new File(jpath).exists()) {
					continue;
				}

				String jpathForCompare = jpath;
				if (osName.toLowerCase().contains("windows")) {
					jpathForCompare = jpath.toLowerCase();
				}

				File jparentFile = new File(jpath).getParentFile();

				if (jparentFile == null) {
					continue;
				} else if (!jparentFile.exists()) {
					continue;
				}

				if (!iparentFile.getAbsolutePath().equals(jparentFile.getAbsolutePath())) {
					if (ipathForCompare.contains(jpathForCompare) || jpathForCompare.contains(ipathForCompare)) {
						if (jpath.length() < pathToKeep.length()) {
							pathToKeep = jpath;
						}
					}
				}
			}

			if (!newList.contains(pathToKeep)) {
				newList.add(pathToKeep);
			}
		}

		TreeDirectory racine = new TreeDirectory("racine1", null);

		for (String path : newList) {
			File file = new File(path);
			if (file.exists()) {
				TreeDirectory treeDirectory = new TreeDirectory(file.getName(), racine);
				racine.addTreeNode(treeDirectory);
				File[] subfiles = file.listFiles();
				if (subfiles != null) {
					for (File f : subfiles) {
						generateTree(f, treeDirectory);
					}
				}
			}
		}

		// keep marked directory, change terminal directory to leaf
		TreeDirectory availableAddonsTree = new TreeDirectory("racine1", null);

		if (racine.getList().size() == 1) {
			TreeDirectory parent = (TreeDirectory) racine.getList().get(0);
			for (TreeNode directory : parent.getList()) {
				TreeDirectory d = (TreeDirectory) directory;
				cleanTree(d, availableAddonsTree);
			}
		} else {
			for (TreeNode directory : racine.getList()) {
				TreeDirectory d = (TreeDirectory) directory;
				cleanTree(d, availableAddonsTree);
			}
		}

		return availableAddonsTree;
	}

	public boolean hasDuplicate(String name) {
		return addonDAO.hasDuplicate(name);
	}

	public TreeDirectoryDTO getAvailableAddonsTree() {

		TreeDirectoryDTO treeDirectoryDTO = new TreeDirectoryDTO();
		treeDirectoryDTO.setName("racine1");
		treeDirectoryDTO.setParent(null);
		if (availabeAddonsTreeInstance != null) {
			transformTreeDirectory2DTO(availabeAddonsTreeInstance, treeDirectoryDTO);
		}
		return treeDirectoryDTO;
	}

	public TreeDirectoryDTO getAvailableAddonsList() {

		TreeDirectoryDTO newTreeDirectoryDTO = new TreeDirectoryDTO();
		newTreeDirectoryDTO.setName("racine1");
		newTreeDirectoryDTO.setParent(null);
		generateTreeList(getAvailableAddonsTree(), newTreeDirectoryDTO);
		return newTreeDirectoryDTO;
	}

	public TreeDirectoryDTO getAvailableDLCList() {

		TreeDirectoryDTO treeDirectoryDTO = new TreeDirectoryDTO();
		treeDirectoryDTO.setName("racineDLC");
		treeDirectoryDTO.setParent(null);

		String profileName = configurationDAO.getConfiguration().getProfileName();
		Profile profile = profileDAO.getMap().get(profileName);

		File arma3Directory = null;
		if (profile != null) {
			String arma3ExePath = profile.getLauncherOptions().getArma3ExePath();
			if (arma3ExePath != null) {
				if (!arma3ExePath.isEmpty()) {
					arma3Directory = new File(arma3ExePath).getParentFile();
				}
			}
		}

		if (arma3Directory != null) {
			GameDLCs[] dlc = GameDLCs.values();
			for (int i = 0; i < dlc.length; i++) {
				String name = dlc[i].toString();
				String description = dlc[i].GetDescription();
				File dlcFolder = new File(arma3Directory + "/" + name);
				if (dlcFolder.exists()) {
					TreeLeafDTO leafDTO = new TreeLeafDTO();
					leafDTO.setName(name);
					leafDTO.setDescription(description);
					leafDTO.setParent(treeDirectoryDTO);
					treeDirectoryDTO.addTreeNode(leafDTO);
					String key = addonDAO.determineNewAddonKey(name);
					Addon addon = new Addon(key, name, arma3Directory.getAbsolutePath());
					addonDAO.getMap().put(key.toLowerCase(), addon);
				}
			}
		}

		return treeDirectoryDTO;
	}

	private void generateTreeList(TreeDirectoryDTO treeDirectoryDTO, TreeDirectoryDTO newTreeDirectoryDTO) {

		for (TreeNodeDTO treeNodeDTO : treeDirectoryDTO.getList()) {
			if (treeNodeDTO.isLeaf()) {
				if (newTreeDirectoryDTO.getList().isEmpty()) {
					newTreeDirectoryDTO.addTreeNode(treeNodeDTO);
				} else {
					List<String> leafNames = new ArrayList<String>();
					for (TreeNodeDTO n : newTreeDirectoryDTO.getList()) {
						leafNames.add(n.getName());
					}
					if (!leafNames.contains(treeNodeDTO.getName())) {
						newTreeDirectoryDTO.addTreeNode(treeNodeDTO);
					}
				}
			} else {
				generateTreeList((TreeDirectoryDTO) treeNodeDTO, newTreeDirectoryDTO);
			}
		}
	}

	private void generateTree(File file, TreeDirectory node) {

		if (file.isDirectory()) {
			String osName = System.getProperty("os.name");
			String path = null;
			if (osName.contains("Windows")) {
				path = file.getAbsolutePath().toLowerCase();
			} else {
				path = file.getAbsolutePath();
			}
			if (!excludedFilePathList.contains(path)) {

				TreeDirectory treeDirectory = new TreeDirectory(file.getName(), node);
				node.addTreeNode(treeDirectory);

				boolean contains = false;
				File[] subfiles = file.listFiles();
				if (subfiles != null) {
					for (File f : subfiles) {
						if (f.getName().toLowerCase().equals("addons")) {
							File[] subfiles2 = f.listFiles();
							if (subfiles2 != null) {
								for (File f2 : subfiles2) {
									if (f2.getName().contains(PBO_EXTENSION) || f2.getName().contains(EBO_EXTENSION)) {
										contains = true;
										break;
									}
								}
							}
						}
					}
				}

				if (contains) {// it is an addon
					String name = treeDirectory.getName();

					// Determine the symbolic key
					String key = addonDAO.determineNewAddonKey(name);

					Addon addon = new Addon(key, name, file.getParentFile().getAbsolutePath());

					addonDAO.getMap().put(key.toLowerCase(), addon);

					// Set directory name with addon key
					treeDirectory.setName(key);

					// Mark up every directories to true
					markRecursively(treeDirectory);

				} else if (!contains && subfiles != null) {
					for (File f : subfiles) {
						generateTree(f, treeDirectory);
					}
				}
			}
		}
	}

	private void markRecursively(TreeDirectory treeDirectory) {

		treeDirectory.setMarked(true);
		TreeDirectory parent = treeDirectory.getParent();
		if (parent != null) {
			markRecursively(parent);
		}
	}

	private void cleanTree(TreeDirectory directory, TreeDirectory directoryCleaned) {

		if (directory.isMarked() && directory.getList().size() != 0) {
			TreeDirectory newDirectory = new TreeDirectory(directory.getName(), directoryCleaned);
			directoryCleaned.addTreeNode(newDirectory);
			for (TreeNode n : directory.getList()) {
				TreeDirectory d = (TreeDirectory) n;
				cleanTree(d, newDirectory);
			}
		} else if (directory.isMarked() && directory.getList().size() == 0) {
			TreeLeaf newTreelLeaf = new TreeLeaf(directory.getName(), directoryCleaned);
			directoryCleaned.addTreeNode(newTreelLeaf);
		}
	}

	public List<String> getAddonsByPriorityList() {

		List<String> availableAddonsByName = new ArrayList<String>();
		for (Iterator<String> iter = addonDAO.getMap().keySet().iterator(); iter.hasNext();) {
			availableAddonsByName.add(iter.next());
		}

		Collections.sort(availableAddonsByName, new SortIgnoreCase());

		String profileName = configurationDAO.getConfiguration().getProfileName();
		Profile profile = profileDAO.getMap().get(profileName);
		if (profile != null) {
			List<String> addonNamesByPriority = profile.getAddonNamesByPriority();
			if (availableAddonsByName.isEmpty()) {
				addonNamesByPriority.clear();
			} else {
				Iterator iter = availableAddonsByName.iterator();
				while (iter.hasNext()) {
					String name = (String) iter.next();
					if (!addonNamesByPriority.contains(name)) {
						addonNamesByPriority.add(name);
					}
				}
				List<String> addonNamesToRemove = new ArrayList<String>();
				for (String stg : addonNamesByPriority) {
					if (!availableAddonsByName.contains(stg)) {
						addonNamesToRemove.add(stg);
					}
				}
				addonNamesByPriority.removeAll(addonNamesToRemove);
			}
			return addonNamesByPriority;
		}
		return null;
	}

	private class SortIgnoreCase implements Comparator<Object> {
		@Override
		public int compare(Object o1, Object o2) {
			String s1 = (String) o1;
			String s2 = (String) o2;
			return s1.toLowerCase().compareTo(s2.toLowerCase());
		}
	}

	public void resolveAddonGroups(TreeDirectoryDTO racine) {

		for (TreeNodeDTO node : racine.getList()) {
			if (node instanceof TreeDirectoryDTO) {// Fix unexpected class cast exception TreeLeafDTO
				TreeDirectoryDTO directory = (TreeDirectoryDTO) node;
				if (directory.getModsetType() != null) {
					if (directory.getModsetType().equals(ModsetType.REPOSITORY)) {
						String repositoryName = directory.getModsetRepositoryName();
						if (repositoryDAO.getMap().containsKey(repositoryName)) {
							Repository repository = repositoryDAO.getMap().get(repositoryName);
							String defaultDownloadLocation = repository.getDefaultDownloadLocation();
							if (defaultDownloadLocation != null) {
								resolveAddonGroup(directory, defaultDownloadLocation);
							}
						}
					} else if (directory.getModsetType().equals(ModsetType.EVENT)) {
						String repositoryName = directory.getModsetRepositoryName();
						if (repositoryDAO.getMap().containsKey(repositoryName)) {
							Repository repository = repositoryDAO.getMap().get(repositoryName);
							String eventName = directory.getName();
							Map<String, String> map = repository.getMapEventsDownloadLocation();
							if (map.containsKey(eventName)) {
								String defaultDownloadLocation = map.get(eventName);
								if (defaultDownloadLocation != null) {
									resolveAddonGroup(directory, defaultDownloadLocation);
								}
							} else {
								String defaultDownloadLocation = repository.getDefaultDownloadLocation();
								if (defaultDownloadLocation != null) {
									resolveAddonGroup(directory, defaultDownloadLocation);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Resolve addon within location on disk
	 * @param directory not null
	 * @param location not null
	 */
	private void resolveAddonGroup(TreeDirectoryDTO directory, String location) {

		List<TreeNodeDTO> list = directory.getList();

		for (TreeNodeDTO node : list) {
			if (node.isLeaf()) {
				TreeLeafDTO leaf = (TreeLeafDTO) node;
				boolean found = false;
				boolean duplicated = addonDAO.hasDuplicate(leaf.getName());
				if (duplicated) {
					List<String> duplicateKeys = addonDAO.getDuplicates(leaf.getName());
					for (String key : duplicateKeys) {
						Addon addon = addonDAO.getMap().get(key);
						if (addon.getPath().equals(location)) {
							leaf.setName(addon.getKey());
							found = true;
							break;
						} else if (addon.getPath().contains(location)) {
							File file = new File(addon.getPath());
							File parent = file.getParentFile();
							while (parent != null) {
								if (parent.getAbsolutePath().equals(location)) {
									found = true;
									break;
								} else if (parent.getAbsolutePath().contains(location)) {
									parent = parent.getParentFile();
								} else {
									break;
								}
							}
							if (found) {
								leaf.setName(addon.getKey());
								break;
							}
						}
					}
				} else {
					Addon addon = addonDAO.getMap().get(leaf.getName().toLowerCase());
					if (addon != null) {
						if (addon.getPath().equals(location)) {
							found = true;
						} else if (addon.getPath().contains(location)) {
							File file = new File(addon.getPath());
							File parent = file.getParentFile();
							while (parent != null) {
								if (parent.getAbsolutePath().equals(location)) {
									found = true;
									break;
								} else if (parent.getAbsolutePath().contains(location)) {
									parent = parent.getParentFile();
								} else {
									break;
								}
							}
						}
					}
				}

				if (found) {
					leaf.setMissing(false);
					leaf.setSourceFilePath(null);
				} else {
					Addon addon = addonDAO.getMap().get(leaf.getName().toLowerCase());
					if (addon != null) {
						leaf.setMissing(false);
						leaf.setSourceFilePath(null);
					} else {
						leaf.setMissing(true);
						leaf.setSourceFilePath(location);
					}
				}
			} else {
				TreeDirectoryDTO d = (TreeDirectoryDTO) node;
				resolveAddonGroup(d, location);
			}
		}
	}

	public void checkMissingAddons(TreeNodeDTO node, List<String> addonNames) {

		if (node.isLeaf()) {
			TreeLeafDTO leaf = (TreeLeafDTO) node;
			if (!leaf.isMissing()) {
				Addon addon = addonDAO.getMap().get(leaf.getName().toLowerCase());
				if (addon == null) {
					leaf.setMissing(true);
					addonNames.add(leaf.getName());
				}
			}
		} else {
			TreeDirectoryDTO directory = (TreeDirectoryDTO) node;
			for (TreeNodeDTO n : directory.getList()) {
				checkMissingAddons(n, addonNames);
			}
		}
	}

	public void checkMissingSelectedAddons(TreeNodeDTO node, List<String> addonNames) {

		if (node.isLeaf()) {
			TreeLeafDTO leaf = (TreeLeafDTO) node;
			if (!leaf.isMissing()) {
				Addon addon = addonDAO.getMap().get(leaf.getName().toLowerCase());
				if (addon == null) {
					leaf.setMissing(true);
				}
			}
			if (leaf.isMissing() && leaf.isSelected()) {
				addonNames.add(leaf.getName());
			}
		} else {
			TreeDirectoryDTO directory = (TreeDirectoryDTO) node;
			for (TreeNodeDTO n : directory.getList()) {
				checkMissingSelectedAddons(n, addonNames);
			}
		}
	}

	public void checkDuplicateAddons(TreeNodeDTO node, TreeDirectoryDTO racine1) {

		if (node.isLeaf()) {
			TreeLeafDTO leaf = (TreeLeafDTO) node;
			boolean duplicated = addonDAO.hasDuplicate(leaf.getName());
			leaf.setDuplicate(duplicated);
			if (duplicated) {
				findSourceRelativePath(leaf, racine1);
			} else {
				leaf.setSourceRelativePath(null);
			}
		} else {
			TreeDirectoryDTO directory = (TreeDirectoryDTO) node;
			for (TreeNodeDTO n : directory.getList()) {
				checkDuplicateAddons(n, racine1);
			}
		}
	}

	private void findSourceRelativePath(TreeLeafDTO leafDTO, TreeNodeDTO treeNodeDTO) {

		if (treeNodeDTO.isLeaf()) {
			TreeLeafDTO leaf = (TreeLeafDTO) treeNodeDTO;
			if (leaf.getName().equalsIgnoreCase(leafDTO.getName())) {
				TreeNodeDTO parent = leaf.getParent();
				String path = null;
				while (parent != null) {
					if (!parent.getName().contains("racine")) {
						if (path == null) {
							path = parent.getName();
						} else {
							path = parent.getName() + "/" + path;
						}
					}
					parent = parent.getParent();
				}
				leafDTO.setSourceRelativePath(path);
			}
		} else {
			TreeDirectoryDTO directory = (TreeDirectoryDTO) treeNodeDTO;
			for (TreeNodeDTO n : directory.getList()) {
				findSourceRelativePath(leafDTO, n);
			}
		}
	}

	public void checkDuplicateAddonsSelection(TreeDirectoryDTO racine, List<String> addonNames) {

		getDuplicateAddonsSelection(racine, addonNames);

		for (TreeNodeDTO n : racine.getList()) {
			checkDuplicateAddonsSelection(n, addonNames);
		}
	}

	private void checkDuplicateAddonsSelection(TreeNodeDTO node, List<String> addonNames) {

		if (node.isLeaf()) {
			TreeLeafDTO leaf = (TreeLeafDTO) node;
			if (addonNames.contains(leaf.getName())) {
				leaf.setDuplicatedSelection(true);
			} else {
				leaf.setDuplicatedSelection(false);
			}
		} else {
			TreeDirectoryDTO directory = (TreeDirectoryDTO) node;
			for (TreeNodeDTO n : directory.getList()) {
				checkDuplicateAddonsSelection(n, addonNames);
			}
		}
	}

	private void getDuplicateAddonsSelection(TreeNodeDTO node, List<String> addonNames) {

		List<String> selectedAddonNames = new ArrayList<String>();
		getSelectedAddonNames(node, selectedAddonNames);

		for (int i = 0; i < selectedAddonNames.size(); i++) {
			int count = 0;
			String name = selectedAddonNames.get(i);
			for (int j = 0; j < selectedAddonNames.size(); j++) {
				if (name.equals(selectedAddonNames.get(j))) {
					count++;
				}
			}
			if (count > 1) {
				addonNames.add(name);
			}
		}
	}

	private void getSelectedAddonNames(TreeNodeDTO node, List<String> addonNames) {

		if (node.isLeaf()) {
			TreeLeafDTO leaf = (TreeLeafDTO) node;
			if (leaf.isSelected()) {
				addonNames.add(leaf.getName());
			}
		} else {
			TreeDirectoryDTO directory = (TreeDirectoryDTO) node;
			for (TreeNodeDTO n : directory.getList()) {
				getSelectedAddonNames(n, addonNames);
			}
		}
	}
}

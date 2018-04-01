package fr.soe.a3s.console;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import fr.soe.a3s.constant.ConsoleCommands;
import fr.soe.a3s.constant.ProtocolType;
import fr.soe.a3s.controller.ObserverEnd;
import fr.soe.a3s.dto.RepositoryDTO;
import fr.soe.a3s.exception.CheckException;
import fr.soe.a3s.exception.LoadingException;
import fr.soe.a3s.exception.WritingException;
import fr.soe.a3s.exception.repository.RepositoryException;
import fr.soe.a3s.main.Version;
import fr.soe.a3s.service.RepositoryService;

/**
 * edited by Schwaggot
 */
public class CommandConsole extends CommandGeneral {

	private boolean devMode;

	public CommandConsole(boolean devMode) {
		this.devMode = devMode;
	}

	public void displayCommands() {

		System.out.println();
		System.out.println("ArmA3Sync console commands:");
		System.out.println(ConsoleCommands.NEW.toString()
				+ ": create a new repository");
		System.out.println(ConsoleCommands.BUILD.toString()
				+ ": build repository");
		System.out.println(ConsoleCommands.BUILDALL.toString()
				+ ": build all repositories");
		System.out.println(ConsoleCommands.CHECK.toString()
				+ ": check repository synchronization");
		System.out.println(ConsoleCommands.DELETE.toString()
				+ ": delete repository");
		System.out.println(ConsoleCommands.LIST.toString()
				+ ": list repositories");
		System.out.println(ConsoleCommands.SYNC.toString()
				+ ": synchronize content with a repository");
		System.out
				.println(ConsoleCommands.EXTRACT.toString()
						+ ": extract *.bikey files from source directory to target directory");
		System.out.println(ConsoleCommands.UPDATE.toString()
				+ ": check for updates");
		System.out.println(ConsoleCommands.COMMANDS.toString()
				+ ": display commands");
		System.out.println(ConsoleCommands.VERSION.toString()
				+ ": display version");
		System.out.println(ConsoleCommands.QUIT.toString() + ": quit");
	}

	public void execute() {

		Scanner c = new Scanner(System.in);
		System.out.println();
		System.out.print("Please enter a command = ");
		String command = c.nextLine().trim();

		if (command.equalsIgnoreCase(ConsoleCommands.VERSION.toString())) {
			displayVersion();
		} else if (command.equalsIgnoreCase(ConsoleCommands.LIST.toString())) {
			list();
		} else if (command.equalsIgnoreCase(ConsoleCommands.NEW.toString())) {
			create();
		} else if (command.equalsIgnoreCase(ConsoleCommands.CHECK.toString())) {
			check();
		} else if (command.equalsIgnoreCase(ConsoleCommands.BUILD.toString())) {
			build();
		} else if (command.equalsIgnoreCase(ConsoleCommands.BUILDALL.toString())) {
            buildAll();
        } else if (command.equalsIgnoreCase(ConsoleCommands.DELETE.toString())) {
			delete();
		} else if (command.equalsIgnoreCase(ConsoleCommands.UPDATE.toString())) {
			checkForUpdates();
		} else if (command.equalsIgnoreCase(ConsoleCommands.SYNC.toString())) {
			sync();
		} else if (command.equalsIgnoreCase(ConsoleCommands.EXTRACT.toString())) {
			extractBikeys();
		} else if (command
				.equalsIgnoreCase(ConsoleCommands.COMMANDS.toString())) {
			displayCommands();
			execute();
		} else if (command.equalsIgnoreCase(ConsoleCommands.QUIT.toString())) {
			quit();
		} else {
			System.out.println("ArmA3Sync - bad command.");
			System.out.print("");
			execute();
		}
	}

    private void list() {

		System.out.println();
		System.out.println("List repositories");

		RepositoryService repositoryService = new RepositoryService();
		try {
			repositoryService.readAll();
		} catch (LoadingException e) {
			System.out.println(e.getMessage());
			execute();
			return;
		}

		List<RepositoryDTO> repositoryDTOs = repositoryService
				.getRepositories();
        Collections.sort(repositoryDTOs);
		Iterator<RepositoryDTO> iter = repositoryDTOs.iterator();

		System.out.println("Number of repositories found: "
				+ repositoryDTOs.size());

		System.out.println();

		while (iter.hasNext()) {
			RepositoryDTO repositoryDTO = iter.next();
			String name = repositoryDTO.getName();
			String autoconfig = repositoryDTO.getAutoConfigURL();
			String path = repositoryDTO.getPath();
			String url = repositoryDTO.getProtocolDTO().getUrl();
			String login = repositoryDTO.getProtocolDTO().getLogin();
			String password = repositoryDTO.getProtocolDTO().getPassword();
			String port = repositoryDTO.getProtocolDTO().getPort();
			ProtocolType protocol = repositoryDTO.getProtocolDTO()
					.getProtocolType();

			if (name != null) {
				if (name.isEmpty()) {
					name = null;
				}
			}
			if (autoconfig != null) {
				if (autoconfig.isEmpty()) {
					autoconfig = null;
				}
			}
			if (path != null) {
				if (path.isEmpty()) {
					path = null;
				}
			}
			if (url != null) {
				if (url.isEmpty()) {
					url = null;
				}
			}
			if (login != null) {
				if (login.isEmpty()) {
					login = null;
				}
			}
			if (password != null) {
				if (password.isEmpty()) {
					password = null;
				}
			}

			System.out.println("Repository name: " + name);
			System.out.println("Protocol: " + protocol.getDescription());
			System.out.println("Url: " + url);
			System.out.println("Port: " + port);
			System.out.println("Login: " + login);
			System.out.println("Password: " + password);
			if (autoconfig == null) {
				System.out.println("Auto-config url not set");
			} else {
				System.out.println("Auto-config url: " + protocol.getPrompt()
						+ autoconfig);
			}
			System.out.println("Repository main folder path: " + path);
			System.out.println();
		}

		execute();
	}

	private void create() {

		System.out.println();
		System.out.println("Create a new repository");

		Scanner c = new Scanner(System.in);

		// Set Name
		String name;
		do {
			System.out.print("Enter repository name: ");
			name = c.nextLine();

			if(name.isEmpty() || !name.matches("[a-zA-Z0-9-_]+")) {
                System.out.println("Repository name may only contain a-z A-Z 0-9 - _");
            }
		} while (name.isEmpty());

		// Set Protocol
		String protocol = "";
		boolean protocolIsWrong;
		do {
			System.out.print("Enter repository protocol FTP, HTTP or HTTPS: ");
			String prot = c.nextLine().toUpperCase();
			if (prot.equals(ProtocolType.FTP.getDescription())) {
				protocol = ProtocolType.FTP.getDescription();
				protocolIsWrong = false;
			} else if (prot.equals(ProtocolType.HTTP.getDescription())) {
				protocol = ProtocolType.HTTP.getDescription();
				protocolIsWrong = false;
			} else if (prot.equals(ProtocolType.HTTPS.getDescription())) {
				protocol = ProtocolType.HTTPS.getDescription();
				protocolIsWrong = false;
			} else {
				protocolIsWrong = true;
			}
		} while (protocolIsWrong);

		// Set Port
		String port;
		boolean portIsWrong = false;
		do {
			System.out.print("Enter repository port ("
					+ ProtocolType.FTP.getDefaultPort() + " " + "default FTP, "
					+ ProtocolType.HTTP.getDefaultPort() + " "
					+ "default HTTP, " + ProtocolType.HTTPS.getDefaultPort()
					+ " " + "default HTTPS): ");
			port = c.nextLine();
			if (port.isEmpty()) {
				if (protocol.equals(ProtocolType.FTP.getDescription())) {
					port = ProtocolType.FTP.getDefaultPort();
                    portIsWrong = false;
				} else if (protocol.equals(ProtocolType.HTTP.getDescription())) {
					port = ProtocolType.HTTP.getDefaultPort();
                    portIsWrong = false;
				} else if (protocol.equals(ProtocolType.HTTPS.getDescription())) {
					port = ProtocolType.HTTPS.getDefaultPort();
                    portIsWrong = false;
				} else {
					portIsWrong = true;
				}
			} else if(!port.matches("[0-9]")) {
			    System.out.println("Port must be a number");
			}
		} while (portIsWrong);

		// Set Login
		String login;
		do {
			System.out.print("Enter user login (enter " + "'anonymous'"
					+ " for public access): ");
			login = c.nextLine();
		} while (login.isEmpty());

		// Set Password
		System.out.print("Enter user password (leave blank if no password): ");
		String password = c.nextLine();

		// Set Repository Url
		String url;
		do {
			System.out.print("Enter repository url: ");
			url = c.nextLine();

			if (!url.isEmpty()) {
				// Remove prompt
				String test = url.toLowerCase()
						.replaceAll(ProtocolType.FTP.getPrompt(), "")
						.replaceAll(ProtocolType.HTTP.getPrompt(), "")
						.replaceAll(ProtocolType.HTTPS.getPrompt(), "");
				if (url.length() > test.length()) {
					int index = url.length() - test.length();
					url = url.substring(index);
				}
			}
		} while (url.isEmpty());

		// Set Main Folder Location
		String path;
		boolean folderLocationIsWrong;
		do {
			System.out
					.print("Enter main folder location (leave blank to pass): ");
			path = c.nextLine();
			if (path.isEmpty()) {
				folderLocationIsWrong = false;
			} else if (!new File(path).exists()
					|| !new File(path).isDirectory()) {
				System.out.println("Target folder does not exists!");
				folderLocationIsWrong = true;
			} else {
				folderLocationIsWrong = false;
			}
		} while (folderLocationIsWrong);

		/* Proceed with command */

		ProtocolType protocole = ProtocolType.getEnum(protocol);
		assert (protocole != null);
		RepositoryService repositoryService = new RepositoryService();
		try {
			repositoryService.createRepository(name, url, port, login,
					password, protocole);
			repositoryService.setConnectionTimeout(name, "0");
			repositoryService.setReadTimeout(name, "0");
			if (!path.isEmpty()) {
				repositoryService.setRepositoryPath(name, path);
			}
			repositoryService.write(name);
			System.out
					.println("Repository creation finished.\nYou can now run the BUILD command to construct the repository");
		} catch (CheckException | WritingException | RepositoryException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println("An unexpeted error has occured.");
			e.printStackTrace();
		} finally {
			execute();
		}
	}

	private void build() {

        System.out.println();
        System.out.println("Build repository");

        Scanner c = new Scanner(System.in);

        System.out.print("Enter repository name: ");
        String name = c.nextLine();
        while (name.isEmpty()) {
            System.out.print("Enter repository name: ");
            name = c.nextLine();
        }

        build(name);

        execute();
    }

    private void build(String name) {
		/* Load Repositories */
        System.out.println("Building repository " + name);

		RepositoryService repositoryService = new RepositoryService();
		try {
			repositoryService.readAll();
		} catch (LoadingException e) {
			e.printStackTrace();
			execute();
			return;
		}

		// catch empty repository folder path
		try {
			RepositoryDTO repositoryDTO = repositoryService.getRepository(name);
			if (repositoryDTO.getPath() == null) {
				throw new IllegalStateException("repository path is not set");
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
			execute();
			return;
		}

		// TODO extract into config file
        // set maximum number of simultaneous client connections
		repositoryService.setNumberOfConnections(name, 10);

		// TODO extract into config file
		// disable compressing of pbo files -> saves CPU/disk load on client
		repositoryService.setCompressed(name, false);

		// TODO extract into config file
		// enable partial file transfer
        repositoryService.setUsePartialFileTransfer(name,true);

		ObserverEnd observerEndBuild = () -> {};

		super.build(name, observerEndBuild);
	}

    private void buildAll() {
        RepositoryService repositoryService = new RepositoryService();
        try {
            repositoryService.readAll();
        } catch (LoadingException e) {
            e.printStackTrace();
            execute();
            return;
        }

        List<RepositoryDTO> repositoryDTOs = repositoryService
                .getRepositories();

        for(RepositoryDTO repositoryDTO : repositoryDTOs) {
            build(repositoryDTO.getName());
        }
    }

	private void check() {

		System.out.println();
		System.out.println("Check repository");

		Scanner c = new Scanner(System.in);

		System.out.print("Enter repository name: ");
		String repositoryName = c.nextLine();
		while (repositoryName.isEmpty()) {
			System.out.print("Enter repository name: ");
			repositoryName = c.nextLine();
		}

		/* Load Repositories */

		RepositoryService repositoryService = new RepositoryService();
		try {
			repositoryService.readAll();
		} catch (LoadingException e) {
			System.out.println(e.getMessage());
			execute();
			return;
		}

		/* Proceed with command */

		ObserverEnd observerEndCheck = this::execute;

		super.check(repositoryName, observerEndCheck);
	}

	private void delete() {

		System.out.println();
		System.out.println("Delete repository");

		Scanner c = new Scanner(System.in);

		System.out.print("Enter repository name: ");
		String name = c.nextLine();
		while (name.isEmpty()) {
			System.out.print("Enter repository name: ");
			name = c.nextLine();
		}

		/* Load Repositories */

		RepositoryService repositoryService = new RepositoryService();
		try {
			repositoryService.readAll();
		} catch (LoadingException e) {
			System.out.println(e.getMessage());
			execute();
			return;
		}

		/* Proceed with command */

		System.out.println("Deleting repository...");
		try {
			boolean remove = repositoryService.removeRepository(name.trim());
			if (remove) {
				System.out.println("Repository " + name + " removed.");
			} else {
				System.out.println("Failded to remove repository.");
			}
		} catch (RepositoryException e) {
			System.out.println(e.getMessage());
		} finally {
			execute();
		}
	}

	private void sync() {

		System.out.println();
		System.out.println("Synchronize with repository");

		Scanner c = new Scanner(System.in);

		System.out.print("Enter repository name: ");
		String repositoryName = c.nextLine();
		while (repositoryName.isEmpty()) {
			System.out.print("Enter repository name: ");
			repositoryName = c.nextLine();
		}

		/* Load Repositories */

		RepositoryService repositoryService = new RepositoryService();
		try {
			repositoryService.readAll();
		} catch (LoadingException e) {
			System.out.println(e.getMessage());
			execute();
			return;
		}

		// Set destination folder
		boolean destinationFolderIsWrong;
		String destinationFolderPath;
		do {
			System.out.print("Enter destination folder path: ");
			destinationFolderPath = c.nextLine();
			if (destinationFolderPath.isEmpty()) {
				destinationFolderIsWrong = true;
			} else if (!new File(destinationFolderPath).exists()) {
				System.out.println("Destination folder does not exists!");
				destinationFolderIsWrong = true;
			} else {
				destinationFolderIsWrong = false;
			}
		} while (destinationFolderIsWrong);

		// Set exact file matching
		boolean withExactMatchIsWrong;
		String withExactMatch;
		do {
			System.out
					.print("Perform Exact file matching (yes/no, choosing yes will erase all extra files into the target folder): ");
			withExactMatch = c.nextLine();
			if (withExactMatch.isEmpty()) {
				withExactMatchIsWrong = true;
			} else {
                withExactMatchIsWrong = !(withExactMatch.equalsIgnoreCase("yes") || withExactMatch
                        .equalsIgnoreCase("no"));
            }
		} while (withExactMatchIsWrong);

		boolean exactMath = false;
		if (withExactMatch.equalsIgnoreCase("yes")) {
			exactMath = true;
		}

		/* Proceed with command */

		repositoryService.setExactMatch(exactMath, repositoryName);
		repositoryService.setDefaultDownloadLocation(repositoryName,
				destinationFolderPath);
		repositoryService.setConnectionTimeout(repositoryName, "0");
		repositoryService.setReadTimeout(repositoryName, "0");

		ObserverEnd observerEnd = this::execute;

		super.sync(repositoryName, observerEnd);
	}

	private void extractBikeys() {

		System.out.println();
		System.out.println("Extract *.bikey files");

		Scanner c = new Scanner(System.in);

		String sourceDirectoryPath;
		do {
			System.out
					.print("Enter source directory to search for *.bikey files: ");
			sourceDirectoryPath = c.nextLine();
		} while (sourceDirectoryPath.isEmpty());

		String targetDirectoryPath;
		do {
			System.out.print("Enter target directory to copy *.bikey files: ");
			targetDirectoryPath = c.nextLine();
		} while (targetDirectoryPath.isEmpty());

		/* Proceed with command */

		super.extractBikeys(sourceDirectoryPath, targetDirectoryPath);

		execute();
	}

	private void checkForUpdates() {

		System.out.println();
		System.out.println("Check for updates.");

		super.checkForUpdates(devMode);

		execute();
	}

	private void displayVersion() {

		System.out.println();
		System.out.println("ArmA3Sync version " + Version.getName());
		System.out.println("Build " + Version.getVersion() + " ("
				+ Version.getYear() + ")");
		System.out.println();
		execute();
	}

	private void quit() {

		System.out.println();
		System.out.println("ArmA3Sync exited.");
		System.exit(0);
	}
}

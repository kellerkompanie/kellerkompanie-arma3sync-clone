package fr.soe.a3s.dao;

public interface DataAccessConstants {

	/** Resources */
	// String INSTALLATION_PATH = System.getProperty("user.dir");
	String INSTALLATION_PATH = ".";
	String HOME_PATH = System.getProperty("user.home");
	String CONFIGURATION_FILE_PATH = INSTALLATION_PATH
			+ "/resources/configuration/a3s.cfg";
	String PREFERENCES_FILE_PATH = INSTALLATION_PATH
			+ "/resources/configuration/a3s.prefs";
	String PROFILES_FOLDER_PATH = INSTALLATION_PATH + "/profiles";
	String CONFIGURATION_FOLDER_PATH = INSTALLATION_PATH
			+ "/resources/configuration";
	String REPOSITORY_FOLDER_PATH = INSTALLATION_PATH + "/resources/ftp";
	String TEMP_FOLDER_PATH = INSTALLATION_PATH + "/resources/temp";
	String BIN_FOLDER_PATH = INSTALLATION_PATH + "/resources/bin";

	/** Extensions */
	String PROFILE_EXTENSION = ".a3s.profile";
	String REPOSITORY_EXTENSION = ".a3s.repository";
	String AUTOCONFIG_EXTENSION = ".a3s.autoconfig";
	String ZSYNC_EXTENSION = ".zsync";
	String PART_EXTENSION = ".part";
	String ZIP_EXTENSION = ".zip";
	String PBO_EXTENSION = ".pbo";
	String EBO_EXTENSION = ".ebo";
	String PBO_ZIP_EXTENSION = ".pbo.zip";
	String BIKEY = ".bikey";

	/** FTP Check for Updates */
	String UPDATE_REPOSITORY_DIR = "ArmA3/ArmA3Sync/download";
	String UPDATE_REPOSITORY_DEV_DIR = "ArmA3/ArmA3Sync/development";
	String UPDTATE_REPOSITORY_ADRESS = "www.sonsofexiled.fr";
	int UPDTATE_REPOSITORY_PORT = 21;
	String UPDTATE_REPOSITORY_LOGIN = "anonymous";
	String UPDTATE_REPOSITORY_PASS = "";

	/** REPOSITORY */
	String A3S_FOlDER_NAME = ".a3s";
	String AUTOCONFIG_FILE_NAME = "autoconfig";
	String SERVERINFO_FILE_NAME = "serverinfo";
	String SYNC_FILE_NAME = "sync";
	String CHANGELOGS_FILE_NAME = "changelogs";
	String EVENTS_FILE_NAME = "events";
	
	/** AUTOCONFIG EXPORT FILE */
	String AUTOCONFIG_EXPORT_FILE_NAME = "export" + AUTOCONFIG_EXTENSION;
	
	/** Log FILE */
	String LOG_FILE_NAME = "ArmA3Sync-log.txt";
}

package fr.soe.a3s.dto.configuration;

public class LauncherOptionsDTO {

	/* Launcher options */
	private String gameProfile;
	private boolean showScriptError;
	private boolean noPause;
	private boolean filePatching;
	private boolean windowMode;
	private boolean checkSignatures;
	private boolean autoRestart;
	private boolean missionFile;
	private String missionFilePath;
	private String maxMemorySelection;
	private int cpuCountSelection;
	private String exThreadsSelection;
	private String mallocSelection;
	private boolean enableHT;
	private boolean hugePages;
	private boolean noSplashScreen;
	private boolean defaultWorld;
	private boolean noLogs;
	/* Executable location */
	private String arma3ExePath;
	private String steamExePath;

	public String getGameProfile() {
		return gameProfile;
	}

	public void setGameProfile(String gameProfile) {
		this.gameProfile = gameProfile;
	}

	public boolean isShowScriptError() {
		return showScriptError;
	}

	public void setShowScriptError(boolean showScriptError) {
		this.showScriptError = showScriptError;
	}

	public boolean isNoPause() {
		return noPause;
	}

	public void setNoPause(boolean noPause) {
		this.noPause = noPause;
	}

	public boolean isWindowMode() {
		return windowMode;
	}

	public void setWindowMode(boolean windowMode) {
		this.windowMode = windowMode;
	}

	public String getMaxMemorySelection() {
		return maxMemorySelection;
	}

	public void setMaxMemorySelection(String maxMemorySelection) {
		this.maxMemorySelection = maxMemorySelection;
	}

	public int getCpuCountSelection() {
		return cpuCountSelection;
	}

	public void setCpuCountSelection(int cpuCountSelection) {
		this.cpuCountSelection = cpuCountSelection;
	}

	public boolean isNoSplashScreen() {
		return noSplashScreen;
	}

	public void setNoSplashScreen(boolean noSplashScreen) {
		this.noSplashScreen = noSplashScreen;
	}

	public boolean isDefaultWorld() {
		return defaultWorld;
	}

	public void setDefaultWorld(boolean defaultWorld) {
		this.defaultWorld = defaultWorld;
	}

	public String getArma3ExePath() {
		return arma3ExePath;
	}

	public void setArma3ExePath(String arma2ExePath) {
		this.arma3ExePath = arma2ExePath;
	}

	public String getSteamExePath() {
		return steamExePath;
	}

	public void setSteamExePath(String steamExePath) {
		this.steamExePath = steamExePath;
	}

	public boolean isNoLogs() {
		return noLogs;
	}

	public void setNoLogs(boolean noLogs) {
		this.noLogs = noLogs;
	}

	public String getExThreadsSelection() {
		return exThreadsSelection;
	}

	public void setExThreadsSelection(String exThreadsSelection) {
		this.exThreadsSelection = exThreadsSelection;
	}

	public boolean isEnableHT() {
		return enableHT;
	}

	public void setEnableHT(boolean enableHT) {
		this.enableHT = enableHT;
	}

	public boolean isHugePages() {
		return this.hugePages;
	}

	public void setHugePages(boolean hugePages) {
		this.hugePages = hugePages;
	}

	public boolean isFilePatching() {
		return filePatching;
	}

	public void setFilePatching(boolean noFilePatching) {
		this.filePatching = noFilePatching;
	}

	public boolean isCheckSignatures() {
		return checkSignatures;
	}

	public void setCheckSignatures(boolean checkSignatures) {
		this.checkSignatures = checkSignatures;
	}

	public boolean isAutoRestart() {
		return this.autoRestart;
	}

	public void setAutoRestart(boolean autoRestart) {
		this.autoRestart = autoRestart;
	}

	public String getMallocSelection() {
		return mallocSelection;
	}

	public void setMallocSelection(String mallocSelection) {
		this.mallocSelection = mallocSelection;
	}

	public boolean isMissionFile() {
		return missionFile;
	}

	public void setMissionFile(boolean missionFile) {
		this.missionFile = missionFile;
	}

	public String getMissionFilePath() {
		return missionFilePath;
	}

	public void setMissionFilePath(String missionFilePath) {
		this.missionFilePath = missionFilePath;
	}
}

package fr.soe.a3s.domain.configration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import fr.soe.a3s.constant.DefaultProfileName;
import fr.soe.a3s.constant.GameVersions;

public class Configuration implements Serializable {

	private static final long serialVersionUID = -8310476744472497506L;
	private int height = 0;
	private int width = 0;
	private String profileName = DefaultProfileName.DEFAULT.getDescription();
	private String gameVersion = GameVersions.ARMA3.getDescription();
	private String serverName;
	private String defaultModset;
	private boolean viewModeTree = true;
	private List<FavoriteServer> favoriteServers = new ArrayList<FavoriteServer>();
	private List<ExternalApplication> externalApplications = new ArrayList<ExternalApplication>();
	private AcreOptions acreOptions = new AcreOptions();
	private Acre2Options acre2Options = new Acre2Options();
	private TfarOptions tfarOptions = new TfarOptions();
	private AiAOptions aiaOptions = new AiAOptions();
	private RptOptions rptOptions = new RptOptions();
	private BikeyExtractOptions bikeyExtractOptions = new BikeyExtractOptions();
	@Deprecated
	private Set<String> addonSearchDirectoryPaths = new TreeSet<String>();
	@Deprecated
	private LauncherOptions launcherOptions = new LauncherOptions();
	private Proxy proxy = new Proxy();

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String getGameVersion() {
		return gameVersion;
	}

	public void setGameVersion(String gameVersion) {
		this.gameVersion = gameVersion;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public List<FavoriteServer> getFavoriteServers() {
		if (favoriteServers == null) {
			favoriteServers = new ArrayList<FavoriteServer>();
		}
		Collections.sort(this.favoriteServers);
		return this.favoriteServers;
	}

	public List<ExternalApplication> getExternalApplications() {
		if (externalApplications == null) {
			externalApplications = new ArrayList<ExternalApplication>();
		}
		return externalApplications;
	}

	public boolean isViewModeTree() {
		return viewModeTree;
	}

	public void setViewModeTree(boolean viewModeTree) {
		this.viewModeTree = viewModeTree;
	}

	public AiAOptions getAiaOptions() {
		if (aiaOptions == null) {
			aiaOptions = new AiAOptions();
		}
		return aiaOptions;
	}

	public AcreOptions getAcreOptions() {
		if (acreOptions == null) {
			acreOptions = new AcreOptions();
		}
		return acreOptions;
	}

	public Acre2Options getAcre2Options() {
		if (acre2Options == null) {
			acre2Options = new Acre2Options();
		}
		return acre2Options;
	}

	public TfarOptions getTfarOptions() {
		if (tfarOptions == null) {
			tfarOptions = new TfarOptions();
		}
		return tfarOptions;
	}

	public RptOptions getRptOptions() {
		if (rptOptions == null) {
			rptOptions = new RptOptions();
		}
		return rptOptions;
	}

	public int getHeight() {
		return height;
	}

	public BikeyExtractOptions getBikeyExtractOptions() {
		if (bikeyExtractOptions == null) {
			bikeyExtractOptions = new BikeyExtractOptions();
		}
		return bikeyExtractOptions;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public String getDefaultModset() {
		return defaultModset;
	}

	public void setDefaultModset(String defaultModset) {
		this.defaultModset = defaultModset;
	}

	public Proxy getProxy() {
		if (proxy == null) {
			proxy = new Proxy();
		}
		return proxy;
	}

	/* DEPRECATED */

	public Set<String> getAddonSearchDirectoryPaths() {
		return addonSearchDirectoryPaths;
	}

	public void resetAddonSearchDirectoryPaths() {
		addonSearchDirectoryPaths = null;
	}

	public LauncherOptions getLauncherOptions() {
		return launcherOptions;
	}

	public void resetLauncherOptions() {
		launcherOptions = null;
	}
}

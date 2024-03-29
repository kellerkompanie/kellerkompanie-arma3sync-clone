package fr.soe.a3s.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

import fr.soe.a3s.constant.GameExecutables;
import fr.soe.a3s.controller.ObservableEnd;
import fr.soe.a3s.controller.ObservableError;
import fr.soe.a3s.controller.ObserverEnd;
import fr.soe.a3s.controller.ObserverError;
import fr.soe.a3s.domain.configration.LauncherOptions;

public class LauncherDAO implements DataAccessConstants, ObservableError, ObservableEnd {

	private ObserverEnd observerEnd;

	private ObserverError observerError;

	public boolean isApplicationRunning(String executableName) {

		boolean response = false;
		try {
			String line;
			String osName = System.getProperty("os.name");
			Process p = null;

			if (osName.contains("Windows")) {
				p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
			} else {
				return false;
			}
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null && !response) {
				if (line.toLowerCase().contains(executableName.toLowerCase())) {
					response = true;
				}
			}
			input.close();
			p.destroy();
		} catch (Exception err) {
			// err.printStackTrace();
		}
		return response;
	}

	@Deprecated
	public void runArmA3WithSteam(String steamLaunchPath, String runParameters)
			throws IOException, InterruptedException {

		StringTokenizer stk = new StringTokenizer(runParameters.trim(), "-");
		int nbParameters = stk.countTokens();
		String[] cmd = new String[2 + nbParameters];
		cmd[0] = steamLaunchPath;
		cmd[1] = "-applaunch 107410";
		for (int i = 0; i < nbParameters; i++) {
			cmd[2 + i] = "-" + stk.nextToken().trim();
		}

		String command = cmd[0] + " " + cmd[1];

		for (int i = 2; i < cmd.length; i++) {
			command = command + " " + cmd[i];
		}
		Process proc = Runtime.getRuntime().exec(command);

		Process p = Runtime.getRuntime().exec(cmd);
		AfficheurFlux fluxSortie = new AfficheurFlux(p.getInputStream());
		AfficheurFlux fluxErreur = new AfficheurFlux(p.getErrorStream());

		new Thread(fluxSortie).start();
		new Thread(fluxErreur).start();
	}

	@Deprecated
	public void run(final String exePath, final String runParameters) {

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					StringTokenizer stk = new StringTokenizer(runParameters.trim(), "-");
					int nbParameters = stk.countTokens();
					String[] cmd = new String[1 + nbParameters];
					cmd[0] = exePath;
					for (int i = 0; i < nbParameters; i++) {
						cmd[1 + i] = "-" + stk.nextToken().trim();
					}

					Process p = Runtime.getRuntime().exec(cmd);
					AfficheurFlux fluxSortie = new AfficheurFlux(p.getInputStream());
					AfficheurFlux fluxErreur = new AfficheurFlux(p.getErrorStream());

					new Thread(fluxSortie).start();
					new Thread(fluxErreur).start();
					p.waitFor();

				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		t.start();
	}

	public Callable<Integer> call(final String executableName, final String exePath, final List<String> params) {

		Callable<Integer> c = new Callable<Integer>() {
			@Override
			public Integer call() {

				String osName = System.getProperty("os.name");
				int response = 0;
				try {
					if (executableName.contains(".exe")) {
						if (osName.contains("Windows")) {
							int nbParameters = params.size();
							String[] cmd = new String[1 + nbParameters];
							cmd[0] = exePath;
							for (int i = 0; i < nbParameters; i++) {
								cmd[1 + i] = params.get(i).trim();
							}
							Process p = Runtime.getRuntime().exec(cmd);
							AfficheurFlux fluxSortie = new AfficheurFlux(p.getInputStream());
							AfficheurFlux fluxErreur = new AfficheurFlux(p.getErrorStream());
							new Thread(fluxSortie).start();
							new Thread(fluxErreur).start();
							p.waitFor();
							response = p.exitValue();
						}
					} else if (executableName.contains(".bat")) {
						if (osName.contains("Windows")) {
							int nbParameters = params.size();
							String[] cmd = new String[4 + nbParameters];
							cmd[0] = "cmd.exe";
							cmd[1] = "/C";
							cmd[2] = "start";
							cmd[3] = exePath;
							for (int i = 0; i < nbParameters; i++) {
								cmd[4 + i] = params.get(i).trim();
							}
							Process p = Runtime.getRuntime().exec(cmd);
							AfficheurFlux fluxSortie = new AfficheurFlux(p.getInputStream());
							AfficheurFlux fluxErreur = new AfficheurFlux(p.getErrorStream());
							new Thread(fluxSortie).start();
							new Thread(fluxErreur).start();
							p.waitFor();
							response = p.exitValue();
						}
					} else if (executableName.contains(".sh")) {
						if (osName.contains("Linux")) {
							int nbParameters = params.size();
							String[] cmd = new String[3 + nbParameters];
							cmd[0] = "/bin/bash";
							cmd[1] = "-c";
							cmd[2] = exePath;
							for (int i = 0; i < nbParameters; i++) {
								cmd[3 + i] = params.get(i).trim();
							}
							Process p = Runtime.getRuntime().exec(cmd);
							AfficheurFlux fluxSortie = new AfficheurFlux(p.getInputStream());
							AfficheurFlux fluxErreur = new AfficheurFlux(p.getErrorStream());
							new Thread(fluxSortie).start();
							new Thread(fluxErreur).start();
							p.waitFor();
							response = p.exitValue();
						}
					} else {
						throw new Exception(executableName + " - invalid executable file.");
					}
				} catch (Exception e) {
					List<Exception> errors = new ArrayList<Exception>();
					errors.add(e);
					updateObserverError(errors);
				}
				return response;
			}
		};
		return c;
	}

	public Callable<Integer> call2(final String executableName, final String exePath, final List<String> params,
			final LauncherOptions launcherOptions) {

		Callable<Integer> c = new Callable<Integer>() {
			@Override
			public Integer call() {

				String osName = System.getProperty("os.name");
				int response = 0;

				try {
					if (osName.toLowerCase().contains("windows")) {
						if (executableName.contains(".exe")) {
							int nbParameters = params.size();
							String[] cmd = null;
							if (executableName.equalsIgnoreCase(GameExecutables.BATTLEYE.getDescription())) {
								cmd = new String[3 + nbParameters];
								cmd[0] = exePath;
								cmd[1] = "2";
								cmd[2] = "1";
								for (int i = 0; i < nbParameters; i++) {
									cmd[3 + i] = params.get(i);
								}
							} else {
								cmd = new String[1 + nbParameters];
								cmd[0] = exePath;
								for (int i = 0; i < nbParameters; i++) {
									cmd[1 + i] = params.get(i);
								}
							}

							String commandLine = "";
							for (int i = 0; i < cmd.length; i++) {
								commandLine += cmd[i] + " ";
							}

							System.out.println("Starting ArmA 3 with command line: " + commandLine);

							Process p = Runtime.getRuntime().exec(cmd);
							AfficheurFlux fluxSortie = new AfficheurFlux(p.getInputStream());
							AfficheurFlux fluxErreur = new AfficheurFlux(p.getErrorStream());
							new Thread(fluxSortie).start();
							new Thread(fluxErreur).start();
							updateObserverEnd();
							p.waitFor();
							if (launcherOptions.isAutoRestart()) {
								call();
							}
							response = p.exitValue();
						} else if (executableName.contains(".bat")) {
							int nbParameters = params.size();
							String[] cmd = new String[4 + nbParameters];
							cmd[0] = "cmd.exe";
							cmd[1] = "/C";
							cmd[2] = "start";
							cmd[3] = exePath;
							for (int i = 0; i < nbParameters; i++) {
								cmd[4 + i] = params.get(i).trim();
							}

							String commandLine = "";
							for (int i = 0; i < cmd.length; i++) {
								commandLine += cmd[i] + " ";
							}

							System.out.println("Starting ArmA 3 with command line: " + commandLine);

							Process p = Runtime.getRuntime().exec(cmd);
							AfficheurFlux fluxSortie = new AfficheurFlux(p.getInputStream());
							AfficheurFlux fluxErreur = new AfficheurFlux(p.getErrorStream());
							new Thread(fluxSortie).start();
							new Thread(fluxErreur).start();
							updateObserverEnd();
							p.waitFor();
							if (launcherOptions.isAutoRestart()) {
								call();
							}
							response = p.exitValue();
						} else {
							throw new Exception(executableName + " - invalid executable file.");
						}
					} else if (osName.toLowerCase().contains("linux")) {
						if (executableName.contains(".sh")) {
							int nbParameters = params.size();
							String[] cmd = new String[3 + nbParameters];
							cmd[0] = "/bin/bash";
							cmd[1] = "-c";
							cmd[2] = exePath;
							for (int i = 0; i < nbParameters; i++) {
								cmd[3 + i] = params.get(i).trim();
							}

							String commandLine = "";
							for (int i = 0; i < cmd.length; i++) {
								commandLine += cmd[i] + " ";
							}

							System.out.println("Starting ArmA 3 with command line: " + commandLine);

							Process p = Runtime.getRuntime().exec(cmd);
							AfficheurFlux fluxSortie = new AfficheurFlux(p.getInputStream());
							AfficheurFlux fluxErreur = new AfficheurFlux(p.getErrorStream());
							new Thread(fluxSortie).start();
							new Thread(fluxErreur).start();
							updateObserverEnd();
							p.waitFor();
							if (launcherOptions.isAutoRestart()) {
								call();
							}
							response = p.exitValue();
						} else {
							int nbParameters = params.size();
							String[] cmd = new String[1 + nbParameters];
							cmd[0] = exePath;
							for (int i = 0; i < nbParameters; i++) {
								cmd[1 + i] = params.get(i).trim();
							}

							String commandLine = "";
							for (int i = 0; i < cmd.length; i++) {
								commandLine += cmd[i] + " ";
							}

							System.out.println("Starting ArmA 3 with command line: " + commandLine);

							Process p = Runtime.getRuntime().exec(cmd);
							AfficheurFlux fluxSortie = new AfficheurFlux(p.getInputStream());
							AfficheurFlux fluxErreur = new AfficheurFlux(p.getErrorStream());
							new Thread(fluxSortie).start();
							new Thread(fluxErreur).start();
							updateObserverEnd();
							p.waitFor();
							if (launcherOptions.isAutoRestart()) {
								call();
							}
							response = p.exitValue();
						}
					} else {
						throw new Exception(executableName + " - invalid executable file.");
					}
				} catch (Exception e) {
					List<Exception> errors = new ArrayList<Exception>();
					errors.add(e);
					updateObserverError(errors);
				}
				return response;
			}
		};
		return c;
	}

	@Deprecated
	public void killSteam(String executableName) {

		try {
			Process proc = Runtime.getRuntime().exec("taskkill /IM" + executableName);
			proc.waitFor();
			System.out.println(executableName + "killed");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void runSteamAndWait(String steamExePath) throws Exception {

		String[] cmd = new String[1];
		cmd[0] = steamExePath;
		Process proc = Runtime.getRuntime().exec(cmd);

		Process p = Runtime.getRuntime().exec(cmd);
		AfficheurFlux fluxSortie = new AfficheurFlux(p.getInputStream());
		AfficheurFlux fluxErreur = new AfficheurFlux(p.getErrorStream());
		new Thread(fluxSortie).start();
		new Thread(fluxErreur).start();
		p.waitFor();
	}

	@Override
	public void addObserverEnd(ObserverEnd obs) {
		this.observerEnd = obs;
	}

	@Override
	public void updateObserverEnd() {
		this.observerEnd.end();
	}

	@Override
	public void addObserverError(ObserverError obs) {
		this.observerError = obs;
	}

	@Override
	public void updateObserverError(List<Exception> errors) {
		this.observerError.error(errors);
	}
}

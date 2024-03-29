package fr.soe.a3s.service.administration;

import java.util.ArrayList;
import java.util.List;

import fr.soe.a3s.controller.ObserverCountInt;
import fr.soe.a3s.controller.ObserverError;
import fr.soe.a3s.domain.AbstractProtocole;
import fr.soe.a3s.exception.remote.RemoteAutoconfigFileNotFoundException;
import fr.soe.a3s.exception.remote.RemoteChangelogsFileNotFoundException;
import fr.soe.a3s.exception.remote.RemoteServerInfoFileNotFoundException;
import fr.soe.a3s.exception.remote.RemoteSyncFileNotFoundException;
import fr.soe.a3s.service.ConnectionService;
import fr.soe.a3s.service.RepositoryService;

public class RepositoryCheckProcessor {

	private final String repositoryName;
	/* Services */
	private final RepositoryService repositoryService = new RepositoryService();
	private ConnectionService connexionService;
	/* observers */
	private ObserverCountInt observerCountProgress;// not null
	private ObserverCountInt observerCountErrors;// null if no recording
	private ObserverError observerEnd;// not null
	private ObserverError observerError;// not null

	public RepositoryCheckProcessor(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public void run() {

		try {
			AbstractProtocole protocole = repositoryService
					.getProtocol(repositoryName);
			connexionService = new ConnectionService(protocole);
			connexionService.getSync(repositoryName);
			connexionService.getServerInfo(repositoryName);
			connexionService.getChangelogs(repositoryName);
			connexionService.getAutoconfig(repositoryName);

			if (repositoryService.getSync(repositoryName) == null) {
				throw new RemoteSyncFileNotFoundException();
			}

			if (repositoryService.getServerInfo(repositoryName) == null) {
				throw new RemoteServerInfoFileNotFoundException();
			}

			if (repositoryService.getChangelogs(repositoryName) == null) {
				throw new RemoteChangelogsFileNotFoundException();
			}

			if (repositoryService.getAutoconfig(repositoryName) == null) {
				throw new RemoteAutoconfigFileNotFoundException();
			}

			connexionService.getConnexionDAOs().get(0)
					.addObserverCount(observerCountProgress);
			connexionService.getConnexionDAOs().get(0)
					.addObserverCountErrors(new ObserverCountInt() {
						@Override
						public void update(int value) {
							executeCountErrors(value);
						}
					});

			List<Exception> errors = connexionService
					.checkRepositoryContent(repositoryName);
			observerEnd.error(errors);

		} catch (Exception e) {
			List<Exception> errors = new ArrayList<Exception>();
			errors.add(e);
			observerError.error(errors);
		} 
	}

	private void executeCountErrors(int value) {

		if (observerCountErrors != null) {
			observerCountErrors.update(value);
		}
	}

	public void cancel() {

		if (connexionService != null) {
			connexionService.cancel();
		}
	}

	public void addObserverCountProgress(ObserverCountInt obs) {
		this.observerCountProgress = obs;
	}

	public void addObserverCountErrors(ObserverCountInt obs) {
		this.observerCountErrors = obs;
	}

	public void addObserverEnd(ObserverError obs) {
		this.observerEnd = obs;
	}

	public void addObserverError(ObserverError obs) {
		this.observerError = obs;
	}
}

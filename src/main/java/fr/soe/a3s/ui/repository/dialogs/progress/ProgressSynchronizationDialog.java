package fr.soe.a3s.ui.repository.dialogs.progress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

import fr.soe.a3s.domain.AbstractProtocole;
import fr.soe.a3s.exception.repository.RepositoryException;
import fr.soe.a3s.service.ConnectionService;
import fr.soe.a3s.service.RepositoryService;
import fr.soe.a3s.ui.AbstractProgressDialog;
import fr.soe.a3s.ui.Facade;
import fr.soe.a3s.ui.repository.dialogs.error.UnexpectedErrorDialog;

public class ProgressSynchronizationDialog extends AbstractProgressDialog {

	private final RepositoryService repositoryService = new RepositoryService();
	private ConnectionService connexionService = null;
	private Thread t = null;

	public ProgressSynchronizationDialog(Facade facade) {
		super(facade, "Synchronizing with repositories...");
	}

	public void init(final String repositoryName) {

		System.out
				.println("Synchronization with repository: " + repositoryName);
		facade.getSyncPanel().disableAllButtons();
		progressBar.setIndeterminate(true);
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					AbstractProtocole protocole = repositoryService
							.getProtocol(repositoryName);
					connexionService = new ConnectionService(protocole);
					connexionService.checkRepository(repositoryName);
				} catch (Exception e) {
					setVisible(false);
					if (e instanceof RepositoryException) {
						JOptionPane.showMessageDialog(facade.getMainPanel(),
								e.getMessage(), repositoryName,
								JOptionPane.ERROR_MESSAGE);
					} else if (!canceled) {
						if (e instanceof IOException) {
							System.out.println(e.getMessage());
							JOptionPane.showMessageDialog(
									facade.getMainPanel(), e.getMessage(),
									repositoryName, JOptionPane.WARNING_MESSAGE);
						} else {
							e.printStackTrace();
							UnexpectedErrorDialog dialog = new UnexpectedErrorDialog(
									facade, repositoryName, e, repositoryName);
							dialog.show();
						}
					}
					setVisible(true);
				}

				System.out.println("Synchronization with repositories done.");

				/*
				 * Update local repositories info with remote a3s folder content
				 * changed
				 */
				repositoryService.updateRepository(repositoryName);

				facade.getMainPanel().updateTabs(OP_REPOSITORY_CHANGED);

				terminate();
			}
		});
		t.start();
	}

	public void init(final List<String> repositoryNames) {

		assert (!repositoryNames.isEmpty());

		System.out.println("Synchronization with repositories...");

		facade.getSyncPanel().disableAllButtons();
		progressBar.setIndeterminate(true);
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				if (repositoryNames.isEmpty()) {
					System.out.println("No repository to synchronize with.");
				} else {
					List<Callable<Integer>> callables = new ArrayList<Callable<Integer>>();
					for (final String repositoryName : repositoryNames) {
						Callable<Integer> c = new Callable<Integer>() {
							@Override
							public Integer call() {
								try {
									if (!canceled) {
										AbstractProtocole protocole = repositoryService
												.getProtocol(repositoryName);
										connexionService = new ConnectionService(
												protocole);
										connexionService
												.checkRepository(repositoryName);
									}
								} catch (Exception e) {
									System.out
											.println("Error when checking repository "
													+ repositoryName
													+ ": "
													+ e.getMessage());
								}
								return 0;
							}
						};
						callables.add(c);
					}

					ExecutorService executor = Executors
							.newFixedThreadPool(Runtime.getRuntime()
									.availableProcessors());

					try {
						executor.invokeAll(callables);
					} catch (InterruptedException e) {
						System.out
								.println("Synchronizing with repositories has been anormaly interrupted.");
					}

					executor.shutdownNow();

					System.out
							.println("Synchronization with repositories done.");

					/*
					 * Update local repositories info with remote a3s folder
					 * content changed
					 */
					for (final String repositoryName : repositoryNames) {
						repositoryService.updateRepository(repositoryName);
					}

					facade.getMainPanel().updateTabs(OP_REPOSITORY_CHANGED);

					terminate();
				}
			}
		});
		t.start();
	}

	@Override
	protected void menuExitPerformed() {

		System.out.println("Synchronization with repositories canceled.");
		this.setVisible(false);
		canceled = true;
		if (connexionService != null) {
			connexionService.cancel();
		}
		terminate();
	}

	private void terminate() {

		progressBar.setIndeterminate(false);
		dispose();
		facade.getSyncPanel().enableAllButtons();
	}
}

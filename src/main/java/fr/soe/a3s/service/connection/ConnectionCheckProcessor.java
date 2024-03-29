package fr.soe.a3s.service.connection;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.soe.a3s.dao.connection.AbstractConnexionDAO;
import fr.soe.a3s.dao.connection.RemoteFile;
import fr.soe.a3s.domain.AbstractProtocole;
import fr.soe.a3s.dto.sync.SyncTreeNodeDTO;

public class ConnectionCheckProcessor extends AbstractConnectionProcessor {

	private List<Exception> errors = null;
	private List<RemoteFile> missingRemoteFiles = null;
	private int count, totalCount;
	private AbstractProtocole protocol = null;

	public ConnectionCheckProcessor(AbstractConnexionDAO abstractConnexionDAO,
			List<SyncTreeNodeDTO> filesToCheck,
			boolean isCompressedPboFilesOnly, boolean withzsync,
			AbstractProtocole protocol) {
		super(abstractConnexionDAO, filesToCheck, isCompressedPboFilesOnly,
				withzsync);
		this.errors = new ArrayList<Exception>();
		this.missingRemoteFiles = new ArrayList<RemoteFile>();
		this.protocol = protocol;
	}

	public void run() throws IOException {

		extract();

		this.totalCount = this.remoteFiles.size();
		this.count = 0;

		for (RemoteFile remoteFile : remoteFiles) {
			if (abstractConnexionDAO.isCanceled()) {
				break;
			} else {
				boolean found = abstractConnexionDAO.fileExists(protocol,
						remoteFile);
				if (!found) {
					missingRemoteFiles.add(remoteFile);
					errors.add(new FileNotFoundException(
							"File not found on repository: "
									+ remoteFile.getRelativeFilePath()));
					abstractConnexionDAO.updateObserverCountErrors(errors
							.size());
				}
				increment();
			}
		}
	}

	private synchronized void increment() {
		count++;
		int value = count * 100 / totalCount;
		abstractConnexionDAO.updateObserverCount(value);
	}

	public List<RemoteFile> getMissingRemoteFiles() {
		return missingRemoteFiles;
	}

	public List<Exception> getErrors() {
		return errors;
	}
}

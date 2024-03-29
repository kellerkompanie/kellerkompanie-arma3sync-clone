package fr.soe.a3s.domain;

import fr.soe.a3s.constant.ProtocolType;

public class Ftp extends AbstractProtocole {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1917448655850030044L;

	public Ftp(String url, String port, String login, String password, ProtocolType protocolType) {
		this.login = login;
		this.password = password;
		this.url = url;
		this.port = port;
		this.protocolType = protocolType;
		this.validateSSLCertificate = false;
	}

	@Override
	public ProtocolType getProtocolType() {
		if (this.protocolType == null) {
			return this.protocolType = ProtocolType.FTP;
		}
		return this.protocolType;
	}
}

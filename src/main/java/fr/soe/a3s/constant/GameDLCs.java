package fr.soe.a3s.constant;

public enum GameDLCs {
	
	Contact(""),GM("Global Mobilization"), vn("S.O.G Prairie Fire"), CSLA("Iron Curtain"), WS("Western Sahara");
	
	private String description;
	
	private GameDLCs(String description) {
		this.description = description;
	}
	
	public String GetDescription() {
		return this.description;
	}
}

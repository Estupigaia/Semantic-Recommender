package etsisi.semanticfieldsrecommender.experiments;

public class CranQuery {
	private Integer identifier;
	private String text;
	
	public CranQuery(Integer identifier, String text) {
		this.identifier = identifier;
		this.text = text;
	}
	
	public CranQuery(Integer identifier) {
		this.identifier = identifier;
	}

	public Integer getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Integer identifier) {
		this.identifier = identifier;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}

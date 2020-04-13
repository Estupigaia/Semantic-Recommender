package etsisi.semanticfieldsrecommender.experiments;

public class CranDocument {
	
	private Integer identifier;
	private String title;
	private String text;
	
	public CranDocument(
			Integer identifier, String title, String reference,
			String author, String text) {
		this.identifier = identifier;
		this.text = text;
	}
	
	public CranDocument(Integer identifier) {
		this.identifier = identifier;
	}

	public Integer getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Integer identifier) {
		this.identifier = identifier;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	

}

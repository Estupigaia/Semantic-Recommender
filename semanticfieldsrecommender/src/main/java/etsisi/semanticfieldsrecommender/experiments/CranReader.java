package etsisi.semanticfieldsrecommender.experiments;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CranReader {
	
	private static final String IDENTIFIER = ".I";
	private static final String TITLE = ".T";
	private static final String AUTHOR = ".A";
	private static final String TEXT = ".W";
	
	public static List<CranDocument> getDocuments(File docsFile) {
		List<CranDocument> cranDocuments = new ArrayList<CranDocument>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(docsFile));
			String line = reader.readLine();
			while(line != null) {
				if(line.contains(IDENTIFIER)) {
					line = CranReader.extractDocument(reader, line, cranDocuments);
				}
				else {
					line = reader.readLine();
				}
			}
			reader.close();
			return cranDocuments;
		}catch(IOException ex) {
			ex.printStackTrace();
			return cranDocuments;
		}
	}
	
	public static List<CranQuery> getQueries(File queriesFile) {
		List<CranQuery> cranQueries = new ArrayList<CranQuery>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(queriesFile));
			String line = reader.readLine();
			while(line != null) {
				if(line.contains(IDENTIFIER)) {
					CranQuery cranQuery = new CranQuery(Integer.parseInt(line.replace(IDENTIFIER, "").trim()));
					line = reader.readLine();
					if(line.contains(TEXT)) {
						String text = "";
						line = reader.readLine();
						while(line != null && !line.contains(IDENTIFIER)) {
							text += " " + line;
							line = reader.readLine();
						}
						cranQuery.setText(text);
					}
					cranQueries.add(cranQuery);
				}
				else {
					line = reader.readLine();
				}
			}
			reader.close();
			return cranQueries;
		}catch(IOException ex) {
			ex.printStackTrace();
			return cranQueries;
		}
	}
	
	public static Map<Integer, List<Integer>> getRelations(File relationsFile) {
		Map<Integer, List<Integer>> cranRelations = new LinkedHashMap<Integer, List<Integer>>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(relationsFile));
			String line = reader.readLine();
			while(line != null) {
				String[] splitLine = line.split(" ");
				Integer queryId = Integer.parseInt(splitLine[0]);
				Integer rating = Integer.parseInt(splitLine[2]);
				List<Integer> relatedDocs = new ArrayList<Integer>();
				while(line != null && rating != -1) {
					relatedDocs.add(Integer.parseInt(splitLine[1]));
					line = reader.readLine();
					splitLine = line.split(" ");
					rating = Integer.parseInt(splitLine[2]);
				}
				cranRelations.put(queryId, relatedDocs);
				if(line!=null)
					line = reader.readLine();
			}
			reader.close();
			return cranRelations;
		}catch(IOException ex) {
			ex.printStackTrace();
			return cranRelations;
		}
	}
	
	private static String extractDocument(BufferedReader reader, String line, List<CranDocument> cranDocuments) throws IOException {
		CranDocument document = new CranDocument(Integer.parseInt(line.replace(IDENTIFIER, "").trim()));
		line = reader.readLine();
		while(line != null && !line.contains(IDENTIFIER)) {
			if(line.contains(TITLE)) {
				String title = "";
				line = reader.readLine();
				while(line != null && !line.contains(AUTHOR)) {
					title += line;
					line = reader.readLine();
				}
				document.setTitle(title);
			}
			else {
				if(line.contains(TEXT)) {
					String text = "";
					line = reader.readLine();
					while(line != null && !line.contains(IDENTIFIER)) {
						text += " " + line;
						line = reader.readLine();
					}
					document.setText(text);
				}
				else {
					line = reader.readLine();
				}
			}
		}
		cranDocuments.add(document);
		return line;
	}

}

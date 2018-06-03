package application.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class FileCreator {
	
	private ArrayList<String> writeParams;
	
	private File fileToSave;
	
	private Path pathToDB;
	
	public FileCreator(Path pathToDB) {
		writeParams = new ArrayList<String>();		
		this.pathToDB = pathToDB;		
	}
	
	public void setFileName(String fileName) {
		this.fileToSave = new File(pathToDB + File.separator + "AlgResults" + File.separator + fileName + ".txt");
	}
	
	public void create() throws IOException {
		fileToSave.getParentFile().mkdirs();
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave.getPath()));
		for(String lineToWrite : writeParams) {
			writer.write(lineToWrite);
			writer.newLine();
		}	     
	    writer.close();
	}
	
	public void addLine(String line) {
		writeParams.add(line);
	}
	
	public void addEmptyLine() {
		writeParams.add("");
	}
}

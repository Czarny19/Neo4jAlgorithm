package application.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class FileCreator {
	
	private ArrayList<String> WriteParams;
	
	private File fileToSave;
	
	private Path DBpath;
	
	public FileCreator(Path DBpath) {
		WriteParams = new ArrayList<String>();		
		this.DBpath = DBpath;		
	}
	
	public void setFileName(String fileName) {
		this.fileToSave = new File(DBpath + File.separator + "AlgResults" + File.separator + fileName + ".txt");
	}
	
	public void create() throws IOException {
		fileToSave.getParentFile().mkdirs();
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave.getPath()));
		for(String lineToWrite : WriteParams) {
			writer.write(lineToWrite);
			writer.newLine();
		}	     
	    writer.close();
	}
	
	public void addLine(String line) {
		WriteParams.add(line);
	}
	
	public void addEmptyLine() {
		WriteParams.add("");
	}
}

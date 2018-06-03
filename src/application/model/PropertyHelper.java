package application.model;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;

public class PropertyHelper implements Runnable{
	
	private Session session;
	private Transaction tx;
	
	private String key;
	
	private boolean isFound;
	
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	public PropertyHelper(Session session) {
		this.session = session;
		this.tx = session.beginTransaction();
	}
	
	public boolean isFound() {
		return isFound;
	}
	
	public void setKey(String key) {
		this.isFound = false;
		this.key = key;
	}
	
	public void closeTransaction(){
		try {
			tx.close();
			session.close();
		}catch(Exception e) {
			logger.log(Level.INFO, "Zamkniêcie sesji z dzia³aj¹c¹ transakcj¹, klucz nie znaleziony");
		}
	}
 
	@Override
	public void run() {
		checkRelationKey();
	}
	
	private void checkRelationKey(){
		try {
			if(tx.run("match ()-[r]->() where r." + key + " is not null return r limit 1").list().size() != 0) {
				this.isFound = true;
				this.tx.close();
				this.session.close();
			}
		}catch(ServiceUnavailableException e) {
			logger.log(Level.INFO, "Klucz " + key + " nie nale¿y do atrybutów relacji", key);
		}
	}
}

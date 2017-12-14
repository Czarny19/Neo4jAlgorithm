package application.model;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

public class Algorithm_A_star implements Runnable{

	private Driver driver;
	
	public Algorithm_A_star(Driver driver) {
		
		this.driver=driver;
	}
	
	@Override
	public void run() {
		try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    StatementResult result = tx.run( "MATCH (n) RETURN distinct labels(n) LIMIT 1" );
                    System.out.println(result.single().get(0));
                    return "bee";
                }
            } );
            System.out.println( greeting);
        }
	}

}

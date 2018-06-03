package application.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.model.Neo4jConnection;

class Neo4jConnectionTest {
	
	Neo4jConnection neo4jConnTest = new Neo4jConnection();
	Thread startConnection = new Thread(neo4jConnTest);

	@BeforeEach
	void setUp() throws Exception {
		neo4jConnTest.initPath("C:\\Users\\User\\Desktop\\Neo4j Extended\\Neo4JDB_3_1_20161103\\Neo4JDB_3_1");
		neo4jConnTest.initPortNumber("7690");		
		startConnection.start();
		startConnection.join();
	}

	@Test
	void executeQueryAfterDBConnection() {
		neo4jConnTest.driver().session().beginTransaction().run("match (a)-[r]->(b) where ID(a)=1 return r");
	}
}

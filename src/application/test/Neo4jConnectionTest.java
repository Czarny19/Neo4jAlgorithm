package application.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.model.Neo4jConnection;

class Neo4jConnectionTest {
	
	Neo4jConnection Neo4jConnTest = new Neo4jConnection();
	Thread StartConnection = new Thread(Neo4jConnTest);

	@BeforeEach
	void setUp() throws Exception {
		Neo4jConnTest.initPath("C:\\Users\\User\\Desktop\\Neo4j Extended\\Neo4JDB_3_1_20161103\\Neo4JDB_3_1");
		Neo4jConnTest.initPortNumber("7690");		
		StartConnection.start();
		StartConnection.join();
	}

	@Test
	void executeQueryAfterDBConnection() {
		Neo4jConnTest.driver().session().beginTransaction().run("match (a)-[r]->(b) where ID(a)=1 return r");
	}

}

package application.test;

import org.apache.commons.lang.time.StopWatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.model.FileCreator;
import application.model.Neo4jConnection;
import application.model.coloring.GraphColoringThread;
import application.model.degree.DegreeCentralityThread;

class AlgorithmTest {

	Neo4jConnection neo4jConnTest = new Neo4jConnection();
	Thread startConnection = new Thread(neo4jConnTest);
	FileCreator algInfo = new FileCreator(null);
	StopWatch execTime = new StopWatch();
	
	@BeforeEach
	void setUp() throws Exception {
		neo4jConnTest.initPath("C:\\Users\\User\\Desktop\\Neo4j Extended\\Neo4JDB_3_1_20161103\\Neo4JDB_3_1");
		neo4jConnTest.initPortNumber("7690");		
		startConnection.start();
		startConnection.join();
	}

	@Test
	void test() {		
		DegreeCentralityThread DC = new DegreeCentralityThread(neo4jConnTest.driver(),true,true,false,null,null);
		Thread DC_Thread = new Thread(DC);
		DC_Thread.start();
		
		GraphColoringThread GC = new GraphColoringThread(neo4jConnTest.driver(),null,null);
		Thread GC_Thread = new Thread(GC);
		GC_Thread.start();
	}
	
	@AfterEach
	void changeConditions() throws Exception {
		DegreeCentralityThread DC = new DegreeCentralityThread(neo4jConnTest.driver(),true,true,false,null,null);
		Thread DC_Thread = new Thread(DC);
		DC_Thread.start();
	}
}

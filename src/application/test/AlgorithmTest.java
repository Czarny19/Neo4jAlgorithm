package application.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import application.model.Neo4jConnection;
import application.model.coloring.GraphColoring;
import application.model.degree.DegreeCentrality;

class AlgorithmTest {

	Neo4jConnection Neo4jConnTest = new Neo4jConnection();
	Thread StartConnection = new Thread(Neo4jConnTest);
	
	@BeforeEach
	void setUp() throws Exception {
		Neo4jConnTest.initPath("C:\\Users\\User\\Desktop\\Neo4j Extended\\Neo4jTest");
		Neo4jConnTest.initPortNumber("7690");		
		StartConnection.start();
		StartConnection.join();
	}

	@Test
	void test() {
		DegreeCentrality DC = new DegreeCentrality(Neo4jConnTest.driver(),true,true,false);
		Thread DC_Thread = new Thread(DC);
		DC_Thread.start();
		
		GraphColoring GC = new GraphColoring(Neo4jConnTest.driver());
		Thread GC_Thread = new Thread(GC);
		GC_Thread.start();
	}
	
	@AfterEach
	void changeConditions() throws Exception {
		DegreeCentrality DC = new DegreeCentrality(Neo4jConnTest.driver(),true,true,false);
		Thread DC_Thread = new Thread(DC);
		DC_Thread.start();
	}
}

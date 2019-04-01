package org.oiue.service.osgi;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

public class GraphManager {
	static Session session = null;
	
	private static void connection() {
		Driver driver = GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "root"));
		session = driver.session();
	}
	
	public static Session getSession() {
		if (session == null)
			connection();
		return session;
	}
	
}

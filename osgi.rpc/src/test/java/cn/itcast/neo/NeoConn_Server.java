package cn.itcast.neo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.Test;

public class NeoConn_Server {
	private String url = "jdbc:neo4j://localhost:7474/";
	private String userName = "neo4j";
	private String passWord = "root";
	@Test
	public void testConfig(){
		try {
			Class.forName("org.neo4j.jdbc.Driver");
			Connection conn =DriverManager.getConnection(url,userName,passWord);
			String sql = "match (n) return n.ACTOR1,n.ACTOR2,n.YEAR limit50";
			PreparedStatement pstat = conn.prepareStatement(sql);
			ResultSet rs = pstat.executeQuery();
			while(rs.next()){
				System.out.println("冲突方1:"+rs.getString("n.ACTOR1")+"---冲突方2:"+rs.getString("n.ACTOR2")+"---年份:"+rs.getString("n.YEAR"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
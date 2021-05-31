package data_access_layer;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import org.jdesktop.application.Application;


public class Access_JDBC {
    private String username;
    private String password;
    private String connectionString;
    private String url;
    private Connection con;

    public void connect() {
    	try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");

            String connectionstr;
            String username;
            String password;

            try{
                url = new File(".").getCanonicalPath();
            } catch (IOException e) {
                e.printStackTrace();
                Application.getInstance().exit();
            }

            url += "\\ICHEA.mdb";

            connectionstr = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
            connectionstr+= url.trim() + ";DriverID=22;READONLY=true}"; // add on to the end

            connectionString = connectionstr;

            username = "";
            password = "";

            // now we can get the connection from the DriverManager
            con = DriverManager.getConnection( connectionString ,username,password);
        }
        catch (Exception e) {
            System.err.println("Error: " + e);
        }
    }

    public void disconnect(){
    	try{
			//	closes the connection (optional)
            con.close();
    	}
        catch (Exception e) {
            System.err.println("Error: " + e);
        }
    }

    public Connection getConnect(){
    	return this.con;
    }

    public String getConnectionString() {
            return connectionString;
    }

    public void setConnectionString(String connectionString) {
            this.connectionString = connectionString;
    }

    public String getPassword() {
            return password;
    }

    public void setPassword(String password) {
            this.password = password;
    }

    public String getUrl() {
            return url;
    }

    public void setUrl(String url) {
            this.url = url;
    }

    public String getUsername() {
            return username;
    }

    public void setUsername(String username) {
            this.username = username;
    }
}


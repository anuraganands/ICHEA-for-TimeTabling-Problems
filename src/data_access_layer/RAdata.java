/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package data_access_layer;

import csp.Chromosome;
import java.util.ArrayList;
import java.sql.*;

/**
 *
 * @author sharma_au
 */
public class RAdata extends data{
    private Access_JDBC db;
    public RAdata(){
        connect();
    }

    @Override
    public void connect() {
        db = new Access_JDBC();
        db.connect();
    }

    @Override
    public void disConnect() {
        db.disconnect();
    }
    
    public void addRAcommoner(Chromosome commoner) throws SQLException{
        String sql = "";
       
        Statement s = db.getConnect().createStatement();
        sql = sql + "INSERT INTO commoner(id, lname, fname) ";
//        sql = sql + "VALUES ('" + cus.getCusId()+"','"+cus.getLName() + "','" + cus.getFName()+ "')";
        s.execute(sql);
        db.disconnect();
    }
}

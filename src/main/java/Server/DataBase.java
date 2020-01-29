package Server;

import java.sql.Connection;
import java.sql.Statement;

public class DataBase {

    private MyServer myServer;
    private static Connection conn;
    private static Statement stmt;

    public DataBase(MyServer myServer) {
        this.myServer = myServer;
    }

}

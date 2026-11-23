import java.io.File;
import java.sql.*;

public class ServerDb
{
    //definicion de constantes 
    //se definen: 
    private static final String DB_PATH = "servidor/database/database.db"; //ruta de la base de datos
    private static final String URL_CONEXION = "jdbc:sqlite:" + DB_PATH; //URL JDBC
    private static final int VERSION_ESQUEMA = 2; 

    static //carga el controlador JDBC SQLite 
    {
        try
        {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e)
        {
            System.err.println("Advertencia: Driver SQLite no encontrado en el classpath");
        }
    }

    //metodo para obtener una conexion nueva con la base de datos 
    private static Connection getConnection() throws SQLException
    {
        File dbFile = new File(DB_PATH);
        if (dbFile.getParentFile() != null)
        {
            dbFile.getParentFile().mkdirs(); //crea la carpeta servidor/database si esta no existe 
        }
        Connection conn = DriverManager.getConnection(URL_CONEXION);
        try (Statement stmt = conn.createStatement())
        {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA busy_timeout = 5000;"); // el servidor espera si la BD esta ocupada
        }
        return conn;
    }



   
}
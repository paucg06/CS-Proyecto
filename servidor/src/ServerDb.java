import java.io.File;
import java.sql.*;

public class ServerDb
{
    private static final String DB_PATH = "servidor/database/database.db";
    private static final String URL_CONEXION = "jdbc:sqlite:" + DB_PATH;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("Advertencia: Driver SQLite no encontrado en el classpath.");
        }
    }

    /**
     * Obtiene una conexión a la base de datos SQLite y habilita claves foráneas.
     */
    private static Connection getConnection() throws SQLException {
        File dbFile = new File(DB_PATH);
        if (dbFile.getParentFile() != null) {
            dbFile.getParentFile().mkdirs();
        }
        Connection conn = DriverManager.getConnection(URL_CONEXION);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    





   
}
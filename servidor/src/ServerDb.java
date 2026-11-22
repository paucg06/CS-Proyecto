import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


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

    // --------------------------------------------------------
    // INICIALIZACION
    // --------------------------------------------------------

    //prepara la base de datos la primera vez o actualiza una base antigua 
    public static void init() throws SQLException
    {
        try (Connection conn = getConnection(); Statement st = conn.createStatement())
        {
            int version;
            try (ResultSet rs = st.executeQuery("PRAGMA user_version"))
            {
                version = rs.getInt(1); //obtiene el numero de version almacenado 
            }
            if (version >= VERSION_ESQUEMA)
            {
                return;
            }

            // PRAGMA foreign_keys no tiene efecto dentro de una transaccion, se desactiva antes
            st.execute("PRAGMA foreign_keys = OFF");
            conn.setAutoCommit(false);
            try
            {
                MigrarUsuarios(conn, st);
                MigrarArchivos(conn, st);
                st.execute("CREATE TABLE IF NOT EXISTS claves_archivo ("
                        + "archivo_id INTEGER NOT NULL, "
                        + "username TEXT NOT NULL, "
                        + "clave_aes_cifrada TEXT NOT NULL, "
                        + "PRIMARY KEY (archivo_id, username), "
                        + "FOREIGN KEY (archivo_id) REFERENCES archivos(id) ON DELETE CASCADE, "
                        + "FOREIGN KEY (username) REFERENCES usuarios(username) ON DELETE CASCADE)");
                st.execute("PRAGMA user_version = " + VERSION_ESQUEMA);
                conn.commit();
            } catch (SQLException e)
            {
                conn.rollback();
                throw e;
            } finally
            {
                conn.setAutoCommit(true);
                st.execute("PRAGMA foreign_keys = ON");
            }
        }
    }

    //comprueba si existe una tabla
    private static boolean ExisteTabla(Connection conn, String tabla) throws SQLException
    {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM sqlite_master WHERE type='table' AND name=?"))
        {
            ps.setString(1, tabla);
            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next();
            }
        }
    }

    //comprueba si una tabla contiene una determinada columna 
    private static boolean ExisteColumna(Connection conn, String tabla, String columna) throws SQLException
    {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("PRAGMA table_info(" + tabla + ")"))
        {
            while (rs.next())
            {
                if (columna.equalsIgnoreCase(rs.getString("name")))
                {
                    return true;
                }
            }
        }
        return false;
    }

    //Actualiza la tabla usuarios y si no existe la crea
    private static void MigrarUsuarios(Connection conn, Statement st) throws SQLException
    {
        if (!ExisteTabla(conn, "usuarios"))
        {
            //se crea la tabla y se definen diversos parametros 
            st.execute("CREATE TABLE usuarios ("
                    + "username TEXT PRIMARY KEY, "
                    + "public_key TEXT NOT NULL, "
                    + "private_key_enc TEXT NOT NULL, "
                    + "salt TEXT NOT NULL, "
                    + "iv TEXT NOT NULL, "
                    + "auth_salt TEXT NOT NULL DEFAULT '', "
                    + "auth_hash TEXT NOT NULL DEFAULT '', "
                    + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");
            return;
        }
        // BD antigua,  agregar las columnas de autenticacion, los usuarios antiguos quedan sin hash por lo que no pueden hacer login
        if (!ExisteColumna(conn, "usuarios", "auth_salt"))
        {
            st.execute("ALTER TABLE usuarios ADD COLUMN auth_salt TEXT NOT NULL DEFAULT ''");
        }
        if (!ExisteColumna(conn, "usuarios", "auth_hash"))
        {
            st.execute("ALTER TABLE usuarios ADD COLUMN auth_hash TEXT NOT NULL DEFAULT ''");
        }
    }

    private static String DdlArchivos(String nombreTabla)
    {
        return "CREATE TABLE " + nombreTabla + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "nombre_original TEXT NOT NULL, "
                + "archivo_cifrado TEXT NOT NULL, "
                + "iv TEXT NOT NULL, "
                + "propietario TEXT NOT NULL, "
                + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "UNIQUE (propietario, nombre_original), " // dos usuarios distintos SI pueden tener "foto.jpg"
                + "FOREIGN KEY (propietario) REFERENCES usuarios(username) ON DELETE CASCADE)";
    }

    private static void MigrarArchivos(Connection conn, Statement st) throws SQLException
    {
        if (!ExisteTabla(conn, "archivos"))
        {
            st.execute(DdlArchivos("archivos"));
            return;
        }
        st.execute(DdlArchivos("archivos_nuevo"));
        st.execute("INSERT INTO archivos_nuevo (id, nombre_original, archivo_cifrado, iv, propietario, created_at) "
                + "SELECT id, nombre_original, archivo_cifrado, iv, propietario, created_at FROM archivos");
        st.execute("DROP TABLE archivos");
        st.execute("ALTER TABLE archivos_nuevo RENAME TO archivos");
    }

    private static boolean EsConflicto(SQLException e)
    {
        return e.getMessage() != null && e.getMessage().contains("SQLITE_CONSTRAINT");
    }

    // --------------------------------------------------------
    // USUARIOS
    // --------------------------------------------------------

    // datos = {username, clavePublica, clavePrivadaCifrada, salt, iv}. authSalt/authHash: verificacion de contrasenya.
    // Devuelve false si el usuario ya existe.
    public static boolean guardarUsuario(String[] datos, String authSalt, String authHash) throws SQLException
    {
        String sql = "INSERT INTO usuarios (username, public_key, private_key_enc, salt, iv, auth_salt, auth_hash) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            for (int i = 0; i < 5; i++)
            {
                ps.setString(i + 1, datos[i]);
            }
            ps.setString(6, authSalt);
            ps.setString(7, authHash);
            ps.executeUpdate();
            return true;
        } catch (SQLException e)
        {
            if (EsConflicto(e))
            {
                return false;
            }
            throw e;
        }
    }

    // Devuelve {username, clavePublica, clavePrivadaCifrada, salt, iv} o null si no existe
    public static String[] obtenerUsuario(String username) throws SQLException
    {
        String sql = "SELECT username, public_key, private_key_enc, salt, iv FROM usuarios WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery())
            {
                if (!rs.next())
                {
                    return null;
                }
                return new String[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)};
            }
        }
    }

    // Devuelve {authSalt, authHash} o null si el usuario no existe o no tiene credenciales (BD antigua)
    public static String[] obtenerAuth(String username) throws SQLException
    {
        String sql = "SELECT auth_salt, auth_hash FROM usuarios WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery())
            {
                if (!rs.next() || rs.getString(2).isEmpty())
                {
                    return null;
                }
                return new String[]{rs.getString(1), rs.getString(2)};
            }
        }
    }

    // Salt que el cliente usa con su contrasenya (publico). null si el usuario no existe
    public static String obtenerSalt(String username) throws SQLException
    {
        String sql = "SELECT salt FROM usuarios WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public static String obtenerClavePublica(String username) throws SQLException
    {
        String sql = "SELECT public_key FROM usuarios WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    // --------------------------------------------------------
    // ARCHIVOS
    // --------------------------------------------------------

    // datos = {nombre, archivoCifrado, iv, claveAesCifradaConRSA, propietario}
    // Guarda el archivo y la clave AES del propietario en una sola transaccion.
    // Devuelve false si el propietario ya tiene un archivo con ese nombre.
    public static boolean guardarArchivo(String[] datos) throws SQLException
    {
        try (Connection conn = getConnection())
        {
            conn.setAutoCommit(false);
            try
            {
                long id;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO archivos (nombre_original, archivo_cifrado, iv, propietario) VALUES (?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS))
                {
                    ps.setString(1, datos[0]);
                    ps.setString(2, datos[1]);
                    ps.setString(3, datos[2]);
                    ps.setString(4, datos[4]);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys())
                    {
                        rs.next();
                        id = rs.getLong(1);
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO claves_archivo (archivo_id, username, clave_aes_cifrada) VALUES (?,?,?)"))
                {
                    ps.setLong(1, id);
                    ps.setString(2, datos[4]);
                    ps.setString(3, datos[3]);
                    ps.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e)
            {
                conn.rollback();
                if (EsConflicto(e))
                {
                    return false;
                }
                throw e;
            }
        }
    }

    // Devuelve {nombre, archivoCifrado, iv, claveAesCifrada, propietario} con la clave AES DEL SOLICITANTE.
    // null si el archivo no existe o el solicitante no tiene acceso (no se distingue a proposito).
    public static String[] obtenerArchivo(String nombre, String propietario, String solicitante) throws SQLException
    {
        String sql = "SELECT a.nombre_original, a.archivo_cifrado, a.iv, c.clave_aes_cifrada, a.propietario "
                + "FROM archivos a JOIN claves_archivo c ON c.archivo_id = a.id "
                + "WHERE a.nombre_original = ? AND a.propietario = ? AND c.username = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, nombre);
            ps.setString(2, propietario);
            ps.setString(3, solicitante);
            try (ResultSet rs = ps.executeQuery())
            {
                if (!rs.next())
                {
                    return null;
                }
                return new String[]{rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)};
            }
        }
    }

    // Archivos a los que el usuario tiene acceso (propios y compartidos con el). Cada fila: {nombre, propietario}
    public static List<String[]> listarArchivos(String usuario) throws SQLException
    {
        String sql = "SELECT a.nombre_original, a.propietario "
                + "FROM archivos a JOIN claves_archivo c ON c.archivo_id = a.id "
                + "WHERE c.username = ? ORDER BY a.propietario, a.nombre_original";
        List<String[]> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    lista.add(new String[]{rs.getString(1), rs.getString(2)});
                }
            }
        }
        return lista;
    }

    // --------------------------------------------------------
    // COMPARTIR
    // --------------------------------------------------------

    // Devuelve null si todo va bien, o el mensaje de error.
    public static String compartirArchivo(String nombre, String propietario, String destino, String claveAesCifrada) throws SQLException
    {
        try (Connection conn = getConnection())
        {
            long id = BuscarIdArchivo(conn, nombre, propietario);
            if (id < 0)
            {
                return "Archivo no encontrado";
            }
            if (obtenerClavePublica(destino) == null)
            {
                return "El usuario destino no existe";
            }
            // REPLACE: si ya estaba compartido, se actualiza la clave
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT OR REPLACE INTO claves_archivo (archivo_id, username, clave_aes_cifrada) VALUES (?,?,?)"))
            {
                ps.setLong(1, id);
                ps.setString(2, destino);
                ps.setString(3, claveAesCifrada);
                ps.executeUpdate();
            }
            return null;
        }
    }

    public static boolean dejarDeCompartir(String nombre, String propietario, String destino) throws SQLException
    {
        if (destino.equals(propietario))
        {
            return false; // el propietario no puede quitarse a si mismo el acceso
        }
        try (Connection conn = getConnection())
        {
            long id = BuscarIdArchivo(conn, nombre, propietario);
            if (id < 0)
            {
                return false;
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM claves_archivo WHERE archivo_id = ? AND username = ?"))
            {
                ps.setLong(1, id);
                ps.setString(2, destino);
                return ps.executeUpdate() > 0;
            }
        }
    }

    private static long BuscarIdArchivo(Connection conn, String nombre, String propietario) throws SQLException
    {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM archivos WHERE nombre_original = ? AND propietario = ?"))
        {
            ps.setString(1, nombre);
            ps.setString(2, propietario);
            try (ResultSet rs = ps.executeQuery())
            {
                return rs.next() ? rs.getLong(1) : -1;
            }
        }
    }
}
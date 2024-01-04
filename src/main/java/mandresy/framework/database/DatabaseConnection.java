package mandresy.framework.database;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class de connection à la base de données
 * @author Mandresy
 *
 */
public class DatabaseConnection {
	
	private static final Logger logger = Logger.getLogger("mandresy.framework.database.DatabaseConnection");
	
	/**
	 * Avoir la connexion avec la base de données à partir des propriétés dans app.properties
	 * @return La connexion à la base des données
	 */
	public static Connection getConnection() {

		// Type de SGBD: "oracle","postgresql" ou "mysql"
		String driver;
		// Nom de la base de données
		String dbName;
		// Nom d'utilisateur
		String userName;
		// Mot de passe utilisateur
		String password;

		Properties properties = new Properties();
		URL propertiesURL = ClassLoader.getSystemResource("app.properties");
        try {
            properties.load(propertiesURL.openStream());

			driver = Optional.of(properties.getProperty("db_driver"))
						.orElseThrow(() -> new RuntimeException("Le type de SGBD(mysql, oracle, postgresql) doit être precisé dans la propriété db_driver du fichier app.properties") );
			dbName = Optional.of( properties.getProperty("db_name") )
					.orElseThrow(() -> new RuntimeException("Le nom de la base de données doit être precisé dans la propriété db_name du fichier app.properties"));
			userName = Optional.of( properties.getProperty("db_user") )
					.orElseThrow(() -> new RuntimeException("Le nom d'utilisateur doit être precisé dans la propriété db_user du fichier app.properties"));
			password = Optional.ofNullable(properties.getProperty("db_password"))
					.orElse("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
		
		try {
			String url = null;
			
			// Si la SGBD est Oracle
			if("oracle".equalsIgnoreCase(driver)) {
				Class.forName("oracle.jdbc.driver.OracleDriver");
				url = "jdbc:oracle:thin:@localhost:1521:orcl";
				
			}
			else if("mysql".equalsIgnoreCase(driver)) {
				// Si la SGBD est MYSQL
				Class.forName("com.mysql.cj.jdbc.Driver");
				url = (dbName != null) ? "jdbc:mysql://localhost:3306/" + dbName : "jdbc:mysql://localhost:3306/mysql";
			}
			else if("postgresql".equalsIgnoreCase(driver)) {
				// Si la SGBD est POSTGRESQL
				Class.forName("org.postgresql.Driver");
				url = (dbName != null) ? "jdbc:postgresql://localhost:5432/" + dbName : "jdbc:postgresql://localhost:5432/postgres";
			}

			Connection connection = null;
			try {
				connection = DriverManager.getConnection(url, userName, password);
				logger.log(Level.INFO, "Connection au base de données réussi.");
				return connection;
				
			} catch (SQLException e) {
				logger.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
	        }
			
	        if (connection == null)
	            throw new NullPointerException("La connection à la base de données a échoué.");
	        
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Pilote JDBC introuvable ou ne correspond pas à la version du SGBD utilisé.");
			e.printStackTrace();
		}

        return null;
	}
	
}

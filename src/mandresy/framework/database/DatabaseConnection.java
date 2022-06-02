package mandresy.framework.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

/**
 * Class de connection aux base de données
 * @author Mandresy
 *
 */
public class DatabaseConnection {
	
	private static final Logger logger = Logger.getLogger("myFramework.database.DatabaseConnection");
	
	/**
	 * Avoir un connexion au base de données à partir des informations du ServletContext dans le web.xml
	 * @param context ServletContext du web.xml
	 * @return Un connection au base des données à partir des données du ServletContext
	 */
	public static Connection getConnection(ServletContext context) {

		// Type de SGBD: "oracle","postgresql" ou "mysql"
		String driver = null;
		// Nom du base données
		String dbName = null;
		// Nom d'utilisateur
		String userName = null;
		// Mot de passe utilisateur
		String password = "";
		
		// Récupération des données du contexte des Servlets
		if(context.getInitParameter("db_driver") == null) {
			throw new RuntimeException("Le type de SGBD(mysql ou oracle) doit être precisé sous context-param \"db_driver\" au web.xml");
		} else {
			driver = context.getInitParameter("db_driver");
		}
		if(context.getInitParameter("db_name") != null) {
			dbName = context.getInitParameter("db_name");
		}
		if(context.getInitParameter("db_user") == null) {
			throw new RuntimeException("Le nom d'utilisateur doit être precisé sous context-param \"db_user\" au web.xml");
		} else {
			userName = context.getInitParameter("db_user");
		}
		if( (context.getInitParameter("db_password") != null) ) {
			password = context.getInitParameter("db_password");
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
				logger.log(Level.INFO, "Connection au base de données reuissi.");
				return connection;
				
			} catch (SQLException e) {
				logger.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
	        }
			
	        if (connection == null)
	            throw new NullPointerException("Connection aux base de données echoué.");
	        
		} catch (ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Pilote OJDBC introuvable ou ne correspond pas à la version du SGBD utilisé.");
			e.printStackTrace();
		}

        return null;
	}
	
}

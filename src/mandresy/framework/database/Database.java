package mandresy.framework.database;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Classe d'acces aux base de données. Cet classe permet de faire des opérations dans la base de données
 * en utilisant mon propre système de relation entre objet-relationnel.
 * 
 * @author Mandresy
 *
 */
public class Database {
	
	public static Connection connection = null;
	
	
	/**
	 * Supprimer les lignes du table qui correspond au filtre entrée en argument
	 * @param <T> Type de l'objet du filtre
	 * @param filtre Objet qui sert de filtre pour la suppression
	 */
	public static <T> void deleteFromTable(T filtre) {
		// Les attributs non null de l'object filtre
		Hashtable<String, Object> attributsNonNull = Database.getAttributsNonNull(filtre);
		
		// Requete sql pour l'insertion
		String sql;
		
		// Si tous les attributs de l'object sont null
		if(attributsNonNull.isEmpty()) {
			sql = String.format("DELETE FROM %s WHERE 1 = 1", filtre.getClass().getSimpleName()); // Nom du table = nom de l'object
			
			try(Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {
				statement.executeUpdate(sql);
			} catch (SQLException e) {
				System.out.println("Echec du delete dans le table " + filtre.getClass().getSimpleName().toUpperCase());
				e.printStackTrace();
			}
		}
		// Si l'object a aux moins un attribut non null
		else {
			// Nombre des attributs non null de l'object
			int nbAttributsNonNull = attributsNonNull.size();
			
			sql = String.format("DELETE FROM %s WHERE %s", 
					filtre.getClass().getSimpleName(), // Nom du table = nom de l'object
					getNomColonnesSelect(attributsNonNull.keys(), nbAttributsNonNull) ); // Noms des colonnes = Noms des attributs non null
			
			try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				// Les valeurs à inserer dans les colonnes filtres
				Collection<Object> valeurColonnes = attributsNonNull.values();
				//Remplir le PreparedStatement par les valeurs des colonnes du filtres
				remplirPreparedStatement(preparedStatement, valeurColonnes);
				
				preparedStatement.executeUpdate();
				
			} catch (SQLException e) {
				System.out.println("Echec du select dans le table " + filtre.getClass().getSimpleName().toUpperCase());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Executer une requête sql et transformer le resultat en Vector des objets de type T
	 * @param <T> Type des objets du Vector à retourner
	 * @param sql la requete sql à executer
	 * @return Vector Liste des objets de type T qui correspond au requête sql entrée
	 */
	public static <T> Vector<T> find(String sql, Class<T> typeObject) {
		
		try(Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
			
				// Transformer le ResultSet obtenu en Vector<typeObject>
				return (Vector<T>) resultSetToVector(resultSet, typeObject);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Trouver touts les objets qui correpsonds à l'objet filtre donnée en argument
	 * @param <T> Type de l'object qui sert de filtre
	 * @param filtre l'object utilisé comme filtre
	 * @return Vector des objets de type T qui contients les object qui ont les même attributs que le filtre
	 */
	@SuppressWarnings("unchecked")
	public static <T, A> Vector<T> find(T filtre) {
		Vector<T> resultats = new Vector<T>();
		// Les attributs non null de l'object object
		Hashtable<String, Object> attributsNonNull = Database.getAttributsNonNull(filtre);
		
		// Requete sql pour l'insertion
		String sql;
		
		// Si tous les attributs de l'object sont null
		if(attributsNonNull.isEmpty()) {
			sql = String.format("SELECT * FROM %s", filtre.getClass().getSimpleName()); // Nom du table = nom de l'object
			
			try(Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY); 
					ResultSet result = statement.executeQuery(sql)) 
			{
				// Transformer le ResultSet obtenu en Vector<filtre.getClass>
				return (Vector<T>) resultSetToVector(result, filtre.getClass());
			
			} catch (SQLException e) {
				System.out.println("Echec du select dans le table " + filtre.getClass().getSimpleName().toUpperCase());
				e.printStackTrace();
			}
		}
		// Si l'object a aux moins un attribut non null
		else {
			// Nombre des attributs non null de l'object
			int nbAttributsNonNull = attributsNonNull.size();
			
			sql = String.format("SELECT * FROM %s WHERE %s", 
					filtre.getClass().getSimpleName(), // Nom du table = nom de l'object
					getNomColonnesSelect(attributsNonNull.keys(), nbAttributsNonNull) ); // Noms des colonnes = Noms des attributs non null
			
			try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				// Les valeurs à inserer dans les colonnes filtres
				Collection<Object> valeurColonnes = attributsNonNull.values();
				//Remplir le PreparedStatement par les valeurs des colonnes du filtres
				remplirPreparedStatement(preparedStatement, valeurColonnes);
				
				// Avoir le ResultSet et le transformer en Vector<filtre.getClass()>
				ResultSet result = preparedStatement.executeQuery();
				return (Vector<T>) resultSetToVector(result, filtre.getClass());
				
			} catch (SQLException e) {
				System.out.println("Echec du select dans le table " + filtre.getClass().getSimpleName().toUpperCase());
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Seulement pour ORACLE, rechercher touts les objects qui correspondent à l'object filtre donnée. La différence
	 * entre cette méthode et find() est que cette méthode utilise 'LIKE %foo%'.
	 * @param <T> Type de l'objet qui sert de filtre
	 * @param filtre l'objet utilisé comme filtre
	 * @return Vector des objets de type T qui contients les resultats du recherhce
	 */
	@SuppressWarnings("unchecked")
	public static <T, A> Vector<T> search(T filtre) {
		Vector<T> resultats = new Vector<T>();
		// Les attributs non null de l'object object
		Hashtable<String, Object> attributsNonNull = Database.getAttributsNonNull(filtre);
		
		// Requete sql pour l'insertion
		String sql;
		String caseInsesitive1 = "ALTER SESSION SET NLS_COMP=LINGUISTIC"; 
		String caseInsesitive2 = "ALTER SESSION SET NLS_SORT=BINARY_AI";
		
		// Si tous les attributs de l'object sont null
		if(attributsNonNull.isEmpty()) {
			sql = String.format("SELECT * FROM %s", filtre.getClass().getSimpleName()); // Nom du table = nom de l'object
			
			try(Statement statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY) ) 
			{
				statement.execute(caseInsesitive1);
				statement.execute(caseInsesitive2);
				try( ResultSet result = statement.executeQuery(sql) ){
					// Transformer le ResultSet obtenu en Vector<filtre.getClass>
					return (Vector<T>) resultSetToVector(result, filtre.getClass());	
				}
			} catch (SQLException e) {
				System.out.println("Echec du select dans le table " + filtre.getClass().getSimpleName().toUpperCase());
				e.printStackTrace();
			}
		}
		// Si l'object a aux moins un attribut non null
		else {
			// Nombre des attributs non null de l'object
			int nbAttributsNonNull = attributsNonNull.size();
			
			sql = String.format("SELECT * FROM %s WHERE %s", 
					filtre.getClass().getSimpleName(), // Nom du table = nom de l'object
					getNomColonnesSearch(attributsNonNull.keys(), nbAttributsNonNull) ); // Noms des colonnes = Noms des attributs non null
			
			try(Statement statement = connection.createStatement();PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				statement.execute(caseInsesitive1);
				statement.execute(caseInsesitive2);
				// Les valeurs à inserer dans les colonnes filtres
				Collection<Object> valeurColonnes = attributsNonNull.values();
				//Remplir le PreparedStatement par les valeurs des colonnes du filtres
				remplirPreparedStatement(preparedStatement, valeurColonnes);
				// Avoir le ResultSet et le transformer en Vector<filtre.getClass()>
				try(ResultSet result = preparedStatement.executeQuery()) {
					return (Vector<T>) resultSetToVector(result, filtre.getClass());	
				}
			} catch (SQLException e) {
				System.out.println("Echec du select dans le table " + filtre.getClass().getSimpleName().toUpperCase());
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Transformer un ResultSet en un Vector des objects de type classObject
	 * @param <T> le type des objects dans le Vector
	 * @param <A> classe générique qui change selon le type de chaque attribut de l'object
	 * @param result Vector qui contient des objects typés T
	 * @param classObject Le type des contenus du Vector à retourne
	 * @return un Vector de classObject
	 */
	@SuppressWarnings("unchecked")
	private static <T, A> Vector<T> resultSetToVector(ResultSet result, Class<T> classObject) {
		Vector<T> resultats = new Vector<T>();
		
		// Les attributs du Class des objects
		Field [] attributs = classObject.getDeclaredFields();
		int nbAttribut = attributs.length;
		
		// Parcours du ResultSet
		try {
			while(result.next()) {
				T unObjectDuResultat;
				
				try {
					// unObjectDuResultat = new ClassObject()
					unObjectDuResultat = (T) classObject.getConstructor().newInstance();
					// Parcours des attributs des objects et affectation de leurs valeurs à unObjectDuResultat
					for(Field attribut : attributs) {
						// Nom et valeur d'un attribut
						String nomAttribut = attribut.getName();
						A valeurAttribut;
						
						try {
							// valeurAttribut = Resultset.getTypeAttribut(nomAttribut)
							// ResultSet.getInteger n'existe pas. Donc, on emploie getObject et on cast en Integer
							if( "integer".equalsIgnoreCase( attribut.getType().getSimpleName()) ) {
								valeurAttribut = (A) ResultSet.class.getMethod("getInt", String.class)
										.invoke(result, nomAttribut);
							} else {
								valeurAttribut = (A) ResultSet.class.getMethod("get" + attribut.getType().getSimpleName().replaceFirst("(?:"+attribut.getType().getSimpleName().charAt(0)+")+", Character.toString(attribut.getType().getSimpleName().charAt(0)).toUpperCase()), String.class)
										.invoke(result, nomAttribut);
							}
							// unObjectDuResultat.setNomAttribut(valeurAttribut)
							try {
								unObjectDuResultat.getClass().getMethod(
										// unObjectDuResultat.setAttribut()
										"set" + attribut.getName().replaceFirst("(?:"+attribut.getName().charAt(0)+")+", Character.toString(attribut.getName().charAt(0)).toUpperCase()) , attribut.getType())
										.invoke(unObjectDuResultat,	valeurAttribut);
								
							} catch ( IllegalAccessException | IllegalArgumentException |InvocationTargetException | NoSuchMethodException | SecurityException e) {
								System.out.println("Erreur à la reflection du méthode set" + attribut.getName().replaceFirst("(?:"+attribut.getName().charAt(0)+")+", Character.toString(attribut.getName().charAt(0)).toUpperCase()));
								e.printStackTrace();
							}
							
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
								| NoSuchMethodException | SecurityException e1) {
							System.out.println("Erreur à la reflection du méthode get" + nomAttribut.replaceFirst("(?:"+nomAttribut.charAt(0)+")+", Character.toString(nomAttribut.charAt(0)).toUpperCase()));
							e1.printStackTrace();
						}
					
					}
					// Vector<ClassObject> -> resultats.add(unObjectDuResultat)
					resultats.add(unObjectDuResultat);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return resultats;
	}
	
	/**
	 * Avoir le valeur d'un attribut de nom anonyme en invoquant son getter
	 * @param <K> Type du l'object
	 * @param <A> Type de l'attribut
	 * @param object L'object
	 * @param nomAttribut Le nom de l'attribut
	 * @return La valeur de l'attribut de l'object
	 */
	@SuppressWarnings("unchecked")
	private static <K, A> A getGetter(K object, String nomAttribut) {
		A valeurAttribut = null;

		// Invoquer le getter du nom de l'attribut
		try {
			valeurAttribut = 
				(A) object.getClass().getMethod(
				// Rendre en majuscule la première lettre du nom de l'attribut
				"get" + nomAttribut.replaceFirst("(?:"+nomAttribut.charAt(0)+")+", Character.toString(nomAttribut.charAt(0)).toUpperCase()) , null)
				.invoke(object, null);
		}catch (NoSuchMethodException e1) {
			System.out.println("Méthode get" + nomAttribut.replace(nomAttribut.charAt(0), Character.toUpperCase(nomAttribut.charAt(0))) + " inexistant");
			e1.printStackTrace();
		} catch (IllegalAccessException e) {
			System.out.println("Acces refusé lors de la tentative d'avoir le méthode get" + nomAttribut.replace(nomAttribut.charAt(0), Character.toUpperCase(nomAttribut.charAt(0))));
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			System.out.println("Argument lors de la tentative d'avoir le méthode get" + nomAttribut.replace(nomAttribut.charAt(0), Character.toUpperCase(nomAttribut.charAt(0))));
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			System.out.println("Erreur lors de l'invocation du méthode get" + nomAttribut.replace(nomAttribut.charAt(0), Character.toUpperCase(nomAttribut.charAt(0))));
			e.printStackTrace();
		} catch (SecurityException e) {
			System.out.println("Erreur de securité lors de la tentative d'avoir le méthode get" + nomAttribut.replace(nomAttribut.charAt(0), Character.toUpperCase(nomAttribut.charAt(0))));
			e.printStackTrace();
		}
		
		return valeurAttribut;
	}
	
	/**
	 * Modifier la ligne du table de la base de donées qui correspond à l'objet
	 * @param object : object qui contient les nouvelles données
	 * @param connection : connection sql utilisé pour la modification
	 */
	public static void updateToTable( Object object){
		// Les attributs non null de l'object object
		Hashtable<String, Object> attributsNonNull = Database.getAttributsNonNull(object);
		
		// Return si tous les attributs de l'object sont null
		if(attributsNonNull.isEmpty())
			return;
		// Nombre des attributs non null de l'object
		int nbAttributsNonNull = attributsNonNull.size();
		
		// Requete sql pour l'insertion
		String sql;
		
		try {
			try {
				sql = String.format("UPDATE %s SET %s WHERE id = '%s'", 
						object.getClass().getSimpleName(), // Nom du table = nom de l'object
						getNomColonnesUpdate(attributsNonNull.keys(), nbAttributsNonNull), // Noms des colonnes = Noms des attributs non null
						object.getClass().getMethod("getId", null).invoke(object, null));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e2) {
				e2.printStackTrace();
				return;
			}
			try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				// Les valeurs à inserer dans les cellules du table
				Collection<Object> valeurColonnes = attributsNonNull.values();

				remplirPreparedStatement(preparedStatement, valeurColonnes);
				
				preparedStatement.executeUpdate();
			} catch (SQLException e) {
				System.out.println("Echec de l'insertion dans le table " + object.getClass().getSimpleName().toUpperCase());
				e.printStackTrace();
			}

		} catch (NoSuchMethodException | SecurityException e2) {
			e2.printStackTrace();
		}
				
	}

	/**
	 * Remplir un preparedStatement par les valeurs des colonnes correspondants
	 * @param preparedStatement
	 * @param valeurColonnes
	 */
	private static void remplirPreparedStatement(PreparedStatement preparedStatement, Collection<Object> valeurColonnes) {
		int i = 1;
		String nomAttribut;

		// Insertion des valeurs des cellules du table dans le preparedStatement
		for(Object valeurColonne : valeurColonnes) {
			try {
				
				Class classValeurColonne = (Class) valeurColonne.getClass().getField("TYPE").get(null);
				nomAttribut = classValeurColonne.getSimpleName();
				PreparedStatement.class.getMethod("set" + nomAttribut.replaceFirst("(?:"+nomAttribut.charAt(0)+")+", Character.toString(nomAttribut.charAt(0)).toUpperCase()), int.class, classValeurColonne)
					.invoke(preparedStatement, i, valeurColonne);
			} catch (NoSuchMethodException e) {
				System.out.println("Méthode set" + valeurColonne.getClass().getSimpleName() + "()");
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				System.out.println("Méthode set" + valeurColonne.getClass().getSimpleName() + "()");
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				try {
					// Si la classe de l'attribut n'a pas d'équivalent primitif
					PreparedStatement.class.getMethod("set" + valeurColonne.getClass().getSimpleName(), int.class, valeurColonne.getClass()).invoke(preparedStatement, i, valeurColonne);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e1) {
					System.out.println("Méthode set" + valeurColonne.getClass().getSimpleName() + "()");   
					e1.getCause();
					e1.printStackTrace();
				}
				
			}
			
			i++;
		
		}
	}
	
	/**
	 * Inserer un objet dans le table correspondant du base de donées
	 * @param object objet à inserer
	 */
	public static void insertToTable(Object object){
		// Les attributs non null de l'object object
		Hashtable<String, Object> attributsNonNull = Database.getAttributsNonNull(object);
		
		// Return si tous les attributs de l'object sont null
		if(attributsNonNull.isEmpty())
			return;
		// Nombre des attributs non null de l'object
		int nbAttributsNonNull = attributsNonNull.size();
		
		// Requete sql pour l'insertion
		String sql = String.format("INSERT INTO %s%s VALUES %s", 
				object.getClass().getSimpleName(), // Nom du table = nom de l'object
				getNomColonnesInsert(attributsNonNull.keys(), nbAttributsNonNull), // Noms des colonnes = Noms des attributs non null
				getPointInterrogations(nbAttributsNonNull)); // "?" du preparedStatement
		try(PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			// Les valeurs à inserer dans les cellules du table
			Collection<Object> valeurColonnes = attributsNonNull.values();

			// Insertion des valeurs des cellules du table dans le preparedStatement
			remplirPreparedStatement(preparedStatement, valeurColonnes);
					
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			System.out.println("Echec de l'insertion dans le table " + object.getClass().getSimpleName().toUpperCase());
			e.printStackTrace();
		}
		
	}

	/**
	 * Avoir les noms des colonnes d'un table sous format (colonne1, colonne2, ...)
	 * @param nomColonnes : Enumeration qui contient les noms des colonnes
	 * @param nbColonne : Nombre des colonnes
	 * @return Chaîne de caractères qui contient les noms des colonnes
	 */
	private static String getNomColonnesInsert(Enumeration<String> nomColonnes, int nbColonne) {
		String nomColonne = "(";
		int n = nbColonne - 1;

		for(int i = 1; i <= n; i++)
			nomColonne = nomColonne.concat(nomColonnes.nextElement() + ", ");
		nomColonne = nomColonne.concat(nomColonnes.nextElement() +")");
		
		return nomColonne;
	}


	/**
	 * Formater les noms de colonnes sous nomColonne1 LIKE %?% OR nomColonne2 LIKE  %?% ... 
	 * @param nomColonnes
	 * @param nbColonne
	 * @return Chaîne de caractère formaté en nomColonne1 LIKE %?% OR nomColonne2 LIKE  %?% ...
	 */
	private static String getNomColonnesSearch(Enumeration<String> nomColonnes, int nbColonne) {
		String nomColonne = " ";
		int n = nbColonne - 1;

		for(int i = 1; i <= n; i++)
			nomColonne = nomColonne.concat(nomColonnes.nextElement() + " LIKE '%'||?||'%' OR ");
		nomColonne = nomColonne.concat(nomColonnes.nextElement() +" LIKE '%'||?||'%' ");
		
		return nomColonne;
	}

	/**
	 * Formater les noms de colonnes sous nomColonne1 = ? AND nomColonne2 =  AND ... 
	 * @param nomColonnes
	 * @param nbColonne
	 * @return Chaîne de caractère formaté en nomColonne1 = ? AND nomColonne2 = ? AND ...
	 */
	private static String getNomColonnesSelect(Enumeration<String> nomColonnes, int nbColonne) {
		String nomColonne = " ";
		int n = nbColonne - 1;

		for(int i = 1; i <= n; i++)
			nomColonne = nomColonne.concat(nomColonnes.nextElement() + " = ? AND ");
		nomColonne = nomColonne.concat(nomColonnes.nextElement() +" = ? ");
		
		return nomColonne;
	}
	
	/**
	 * Formater les noms de colonnes sous nomColonne1 = ?, nomColonne2 = ?, ... 
	 * @param nomColonnes
	 * @param nbColonne
	 * @return Chaîne de caractère formaté en nomColonne1 = ?, nomColonne2 = ?, ...
	 */
	private static String getNomColonnesUpdate(Enumeration<String> nomColonnes, int nbColonne) {
		String nomColonne = " ";
		int n = nbColonne - 1;

		for(int i = 1; i <= n; i++)
			nomColonne = nomColonne.concat(nomColonnes.nextElement() + " = ?, ");
		nomColonne = nomColonne.concat(nomColonnes.nextElement() +" = ? ");
		
		return nomColonne;
	}
	
	/**
	 * Avoir un chaîne de caractère formaté en (?, ?, ...) avec n nombre de "?"
 	 * @param n nombre des "?"
	 * @return un chaîne de caractère formaté en (?, ?, ...)
	 */
	private static String getPointInterrogations(int n) {
		String pointInterrogations = "(";
		int nMoinsUn = n - 1;
		
		for(int i = 0; i < nMoinsUn; i++)
			pointInterrogations = pointInterrogations.concat("?, ");
		pointInterrogations = pointInterrogations.concat("?)");
		
		return pointInterrogations;
	}
	
	/**
	 * Avoir les attributs non null d'un object
	 * @param object : l'object
	 * @return Hashtable avec le nom de l'attribut comme clé et la valeur de l'attribut comme valeur
	 */
	public static Hashtable<String, Object> getAttributsNonNull(Object object) {
		Hashtable<String, Object> attributsNonNull = new Hashtable<String, Object>();
		Field[] attributs = object.getClass().getDeclaredFields();
		
		String nomAttribut = new String(); // Nom de l'attribut
		Class<?> typeAttribut; // Type de l'attribut
		Object valeurAttribut; // Getter de l'attribut
		for (Field attribut : attributs) {
			
				nomAttribut = attribut.getName(); 
				typeAttribut = attribut.getType();
				// Avoir la valeur de l'attribut
				valeurAttribut = getGetter(object, nomAttribut); // Invoquer le getter
				
				// Si l'attribut est null
				if (valeurAttribut == null)
					continue;
				
				// Si l'attribut est un object
				else if ( !typeAttribut.isPrimitive() )
						attributsNonNull.put(nomAttribut, valeurAttribut);
				// Si l'attribut est un char
				else if ( typeAttribut == char.class ) {
					Character c = ( (Character) valeurAttribut ).charValue();
					if( !c.equals('\u0000') )
						attributsNonNull.put(nomAttribut, valeurAttribut);
				}
				// Si l'attribut est un nombre
				else {
					double c = ( (Number) valeurAttribut ).doubleValue();
					if( c != 0.0 )
						attributsNonNull.put(nomAttribut, valeurAttribut);
				}
		}
		return attributsNonNull;
	}
	

	/**
	 * Avoir le valeur actuel ou valeurs suivant d'un séquence "ORACLE"
	 * @param donnee chaîne de caractères sequence_xxxx.nextVal ou sequence_xxxx.currval
	 * @param connection
	 * @return le valeur actuel ou valeurs suivant d'un séquence
	 */
	@SuppressWarnings("unused") // Dead code -> return 0
	public static int getSequence(String donnee) {
		
		String sql = String.format("select %s from dual", donnee);
		try(Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
			if(resultSet.next())
				return resultSet.getInt(1);
			else
				throw new SQLException("Resultat sequence null");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(true)
			throw new RuntimeException("Tsy tokony ho eto");
		
		return 0;
	}
	
}

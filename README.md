# Mini-Framework Java EE
Un framework fait maison utilisant un design pattern MVC2 en employant un frontcontroller. Cet mini-framework permet de gérer les différents couche d'une application comme le celui de l'affichage, du métier ou du base de données. Les spécifications du Java EE implementés dans cet mini-framework sont l'API de Servlet, JSP et JSTL. Pour l'accès au base de données, cet framework a son propre ORM.

## Controlleur
Une classe controlleur HTTP doît être:
- Inclus dans le package app.controller
- Une classe fille du classe Controller

### • URL d'un controlleur
Pour spéficier l'URI d'une méthode d'une classe controlleur, sa signature de retour doît être un ModelView et elle dôit avoir l'annotation @ControllerMethod.

```java
@ControllerMethod(uri = "hello")
public ModelView welcome() { 
    return null;
}
```
La méthode welcome() sera executé quand "http://site.domaine/hello.do" est appelé

### • Récuperation des données GET et POST
Pour récuperer un champ nomé "email" envoyée en GET ou POST, il suffit d'appeler la méthode get("email") ou post("email") dans une classe de controlleur.

### • Controlleur du page d'acceuil
Pour faire "http://site.domaine/hello.do" comme page d'acceuil, il faut le définir dans le conntext-param -> home du fichier web.xml:
```html
<!-- URI du controlleur du page d'acceuil  -->
<context-param>
  <param-name>home</param-name>
  <param-value>hello</param-value>
</context-param>
```

## Vue
Les vues doivent être dans le dossier WebContent/vue/ et sont des fichiers jsp

### • Afficher une vue
Pour afficher une vue hello.jsp quand on appelle  "http://site.domaine/hello.do".
Il suffit de faire:

```java
@ControllerMethod(uri = "hello")
public ModelView welcome() {
	// Ajout du vue
	setVue("hello");
	
	return this.view;
}
```

### • Passer des données à une vue
Pour passer une donnée venant du controlleur à une vue.
Dans le controlleur

```java
public ModelView hello() {
	// Ajout des données du vue
	addData("nom", "Mandresy");
	addData("age", 21);
	
	// Ajout du vue
	setVue("hello");
	
	return this.view;
}
```
Dans la vue hello.jsp
```html
<h1>Hello ${nom}, vous avez ${age}ans</h1>
```

## Base de données

### • Connection au base des données
Les champs de connection au base de données sont définis dans le fichier web.xml
```html
  <!-- Type du SGBD, valeurs authorisés:  oracle, postgresql, mysql -->
  <context-param>
  	<param-name>db_driver</param-name>
  	<param-value>postgresql</param-value>
  </context-param>

  <!-- Nom du base des données -->
  <context-param>
  	<param-name>db_name</param-name>
  	<param-value>nom-du-base-de-donnees</param-value>
  </context-param>

  <!-- Nom d'utilisateur du base des données -->
  <context-param>
	<param-name>db_user</param-name>
	<param-value>nom-utilisateur</param-value>
  </context-param>

  <!-- Mot de passe d'utilisateur du base des données -->
  <context-param>
  	<param-name>db_password</param-name>
  	<param-value>mot-de-passe</param-value>
  </context-param>
```

### • Relation entre objet et table
    - Le nom et type des attributs d'une classe et des colonnes du table correspondant devront être les mêmes et équivalent
    -La classe doît avoir un attribut id
    -La classe doît avoir un getter pour chacun de ses attributs
    
Pour les exemples des opérations suivantes, on va prendre la classe Utilisateur et le table utilisateur.
Classe Utilisateur
```java
public class Utilisateur {
    private int id;
	private String nom;
	private String mdp;
	
	...
}
```
Table utilisateur
| utilisateur     | 
| ----------------| 
| id number(5, 0) | 
| nom varchar(100)|
|mdp varchar(255) |

#### - Insertion
Pour inserer un utilisateur, il suffit de faire:
```java
Utilisateur nouveauUtilisateur = new Utilisateur();

...

Database.insertToTable(nouveauUtilisateur);
```


#### - Suppression
Pour supprimer un utilisateur, il suffit de faire:
```java
Utilisateur filtre = new Utilisateur();

...

Database.deleteFromTable(filtre);
```

#### - Tous supprimer
Pour supprimer touts les utilisateurs, il suffit de faire:
```java
Database.deleteFromTable(new Utilisateur());
```

#### - Mettre à jour
Pour mettre à jour un utilisateur, il suffit de faire:
```java
Utilisateur utilisateur = new Utilisateur();
//L'ID est obligatoire pour être utilisé comme réference de l'utilisateur
utilisateur.setId(xxxxx);

...

Database.updateToTable(utilisateur);
```

#### - Récuperer toutes les lignes d'une table
Pour avoir touts les utilisateur, il suffit de faire:
```java
List<Utilisateur> utilisateurs = Database.find(new Utilisateur());
```
#### - Requête avec condition
Pour avoir touts les utilisateurs qui ont "Bema" comme nom et "abcd" comme mot de passe, il suffit de faire:
```java
Utilisateur utilisateur = new Utilisateur();
utilisateur.setNom("Bema");
utilisateur.setMdp("1234");

// Tous les utilisateurs qui ont "Bema" comme nom et "abcd" comme mot de passe
List<Utilisateur> utilisateurs = Database.find(utilisateur);

// Si vous ne voulez qu'un seul utilisateur
Utilisateur bema = Database.find(utilisateur).get(0);
```

#### - Requête SQL brut
Pour utiliser du requete SQL brut, il suffit de faire:
```java
List<Utilisateur> utilisateurs = Database.find("SELECT * FROM utilisateur WHERE name LIKE %foo% AND ...", Utilisateur.class);
```

_Je vous remercie pour avoir donner votre temps à lire la documentation de mon petit framework. Vous pouvez me contacter en cliquant [ici](mailto:mandresyrabenj@gmail.com), je serais la pour vous repondre._
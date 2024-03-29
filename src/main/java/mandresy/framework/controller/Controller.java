package mandresy.framework.controller;

import java.util.Map;

import mandresy.framework.vue.ModelView;

/**
 * Classe mère des controlleurs HTTP. Chaque controlleur HTTP de l'application doît heriter cette classe.
 * 
 * @author Mandresy
 *
 */
public class Controller {
	/**
	 * Model du vue de cette controlleur
	 */
	protected ModelView view = new ModelView();

	/**
	 * Données reçues par cet controlleur en POST
	 */
	protected Map<String, String> post;
	/**
	 * Données reçues par cet controlleur en GET
	 */
	protected Map<String, String> get;
	
	/**
	 * Ajouter une donnée pour la vue de cet controlleur. Les données seront ensuite accessible facilement
	 *  par la vue en utilisant de l'EL(Expression Language).<br>
	 *  <b>Exemple:</b><br>
	 *  <b>Code du controlleur:</b><br>
	 *  <code>addData("nom", "Mandresy")<code><br>
	 *  <b>Code du vue:</b><br>
	 *  <code>Hello ${nom}</code> => Hello Mandresy
	 *  
	 * @param key Clés
	 * @param value Valeur
	 */
	protected void addData(String key, Object value) {
		this.view.getData().put(key, value);
	}
	
	
	/**
	 * Ajouter une vue pour controlleur.
	 * @param name Nom du vue sans l'extension ".jsp".
	 */
	protected void setVue(String name) {
		this.view.setPage(name);
	}

	public ModelView getView() {
		return view;
	}


	public void setView(ModelView view) {
		this.view = view;
	}


	public Map<String, String> getPost() {
		return post;
	}


	public void setPost(Map<String, String> post) {
		this.post = post;
	}


	public Map<String, String> getGet() {
		return get;
	}


	public void setGet(Map<String, String> get) {
		this.get = get;
	}
	
	/**
	 * Avoir une donnée de Http GET en utilisant sa clé correspondante
	 * @param key Clé de la donnée GET
	 * @return Valeur correspondant à cette clé
	 */
	public String get(String key) {
		return this.get.get(key);
	}

	/**
	 * Avoir une donnée de Http POST en utilisant sa clé correspondant
	 * @param key Clé de la donnée POST
	 * @return Valeur correspondant à cette clé
	 */
	public String post(String key) {
		return this.post.get(key);
	}
	
}

package mandresy.framework.vue;

import java.util.HashMap;

/**
 * Modelview contentant le vue et ses datas
 * @author Mandresy
 *
 */
public class ModelView {

	/**
	 * Data du vue
	 */
	private HashMap<String, Object> data = new HashMap<String, Object>();
	/**
	 * Nom du fichier jsp(sans l'extension ".jsp")
	 */
	private String page = null;
	
	
	@Override
	public String toString() {
		return "ModelView [data="+ 
				data  + ", page=" + page + "]";
	}
	public ModelView() {
		super();
		page = null;
	}
	/**
	 * Constructeur en donnant les données du vue
	 * @param data les données du vue
	 */
	public ModelView(HashMap<String, Object> data) {
		super();
		page = null;
		this.data = data;
	}
	/**
	 * Constructeur en donnant le nom du fichier jsp(sans l'extension ".jsp")
	 * @param page Le nom du fichier jsp(sans l'extension ".jsp")
	 */
	public ModelView(String page) {
		super();
		this.page = page;
	}
	/**
	 * Constructeur en donnant les données du vue et le nom du fichier jsp(sans l'extension ".jsp")
	 * @param data les données du vue
	 * @param page Le nom du fichier jsp(sans l'extension ".jsp")
	 */
	public ModelView(HashMap<String, Object> data, String page) {
		super();
		this.data = data;
		this.page = page;
	}
	public HashMap<String, Object> getData() {
		return data;
	}
	public void setData(HashMap<String, Object> data) {
		this.data = data;
	}
	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	
}

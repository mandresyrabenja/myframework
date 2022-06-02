package app.controller;

import mandresy.framework.controller.ControllerMethod;
import mandresy.framework.controller.Controller;
import mandresy.framework.vue.ModelView;

/**
 * Exemple d'un controlleur. Une classe controlleur HTTP doît être:<br>
 *  <ul>
 *  	<li>Inclus dans le package app.controller</li>
 *  	<li>Une classe fille du classe {@link mandresy.framework.controller.Controller}</li>
 *  </ul><br>
 *  Les méthodes du controlleur qu'on veut appeler par requête HTTP doît être annotées 
 *  avec l'annotation {@link mandresy.framework.controller.ControllerMethod} en précisant son uri.
 * 
 * @author Mandresy
 *
 */
public class Welcome extends Controller {

	@ControllerMethod(uri = "hello")
	public ModelView hello() {
		// Récuperation d'un donnée du Http GET et affectation au donnée du vue de cet controlleur
		addData("name", get("name"));
		
		// Ajout du vue de cet controlleur
		setVue("hello");
		return this.view;
	}
	
}

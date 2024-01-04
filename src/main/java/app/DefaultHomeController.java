package app;

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
public class DefaultHomeController extends Controller {

	@ControllerMethod(uri = "defaultHome")
	public ModelView hello() {
		// Récuperation d'une donnée de la requête HTTP GET et affectation aux données de la vue de cet controlleur
		addData("name", get("name"));

		// Ajout de la vue de cet controlleur
		setVue("default-home");
		return this.view;
	}
	
}

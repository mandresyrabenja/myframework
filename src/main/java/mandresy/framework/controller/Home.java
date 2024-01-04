package mandresy.framework.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dans le cas où aucun URI de controlleur n'est spécifié(c-à-d, quand l'URL du requête HTTP est "http://site.domaine").
 * Le Servlet {@link mandresy.framework.controller.FrontController} ne peut pas faire son travail. Alors, cet servlet intervient
 * en récupérant la page d'accueil precisé dans le context-param 'home' du web.xml avant de formuler une URL pour ensuite
 * permettre à  {@link mandresy.framework.controller.FrontController} de pouvoir faire son travail.
 *
 * @author Mandresy
 */
@WebServlet(description = "Controlleur du page d'acceuil", urlPatterns = { "/home" })
public class Home extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger logger = Logger.getLogger("mandresy.framework.controller.Home");

    /**
     * @see HttpServlet#HttpServlet()
     */
    public Home() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Properties properties = new Properties();
			properties.load(
					ClassLoader.getSystemResource("app.properties")
							.openStream()
			);

			String homeURI = properties.getProperty("home") != null ? properties.getProperty("home") + ".do" : "defaultHome.do";
			response.sendRedirect(homeURI);
		} catch (IOException e) {
			String msg = "La page d'accueil doit être precisé dans la propriété home du fichier app.properties";
			logger.log(Level.SEVERE, msg);
			response.getWriter().print(msg);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}

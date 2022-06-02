package mandresy.framework.controller;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Dans le cas où aucun URI de controlleur n'est spécifié(c-à-d, quand l'URL du requête HTTP est "http://site.domaine").
 * Le Servlet {@link mandresy.framework.controller.FrontController} ne peut pas faire son travail. Alors, cet servlet intervient
 * en récuperant la page d'acceuil precisé dans le context-param 'home' du web.xml avant de formuler une URL pour ensuite
 * permettre à  {@link mandresy.framework.controller.FrontController} de pouvoir faire son travail.
 * 
 * @author Mandresy
 */
@WebServlet(description = "Controlleur du page d'acceuil", urlPatterns = { "/home" })
public class Home extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger logger = Logger.getLogger("myFramework.controller.Home");

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
			response.sendRedirect(getServletContext().getInitParameter("home") + ".do");
		} catch (NullPointerException e) {
			String msg = "La page d'acceuil doit être precisé dans le context-param \"home\" du web.xml";
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

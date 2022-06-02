package mandresy.framework.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.reflections.Reflections;

import mandresy.framework.database.Database;
import mandresy.framework.database.DatabaseConnection;
import mandresy.framework.vue.ModelView;

/**
 * FrontController de l'application. A part les requêtes HTTP des fichiers statique, 
 * touts les requêtes seront affectés à cet Servlet. Cet Servlet aura pour fonction d'envoyer convenablement chaque
 * requête au méthode du controlleur correspondant. Elle aura aussi pour rôle d'encapsuler les données reçus par GET ou par
 * POST en Map de String comme clé et d'object comme valeur, puis elle chargera la vue du méthode du controlleur une fois que
 * cette derniere a fini ses opérations/traitements. 
 * 
 * @author Mandresy
 */
@WebServlet(description = "Front Controller des urls", urlPatterns = { "*.do" }, loadOnStartup = 1)
public class FrontController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger logger = Logger.getLogger("myFramework.controller.FrontController");
	
	/**
	 * URI controlleur<br>
	 * Exemple:<br>
	 * /hello/displayhello.do[?name=Koto]
	 */
	private static Pattern controlerPattern = Pattern.compile("^/([a-zA-Z_]+)/([a-zA-Z_]+)\\.do(.)*$");
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FrontController() {
        super();
    }

    @Override
    public void init() throws ServletException {
    	super.init();
    	Database.connection = DatabaseConnection.getConnection(this.getServletContext());
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		frontController(request, response, "GET");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		frontController(request, response, "POST");
	}

	/**
	 * Traîtement de l'url du client HTTP
	 * @param request HttpRequest
	 * @param response HttpResponse
	 * @param requestType <b>"GET"</b> pour requete GET<br> 
	 * et <b>"POST"</b> pour requête POST
	 * @throws IOException 
	 * @throws ServletException 
	 */
	private void frontController(HttpServletRequest request, HttpServletResponse response, String requestType) throws IOException, ServletException {
		response.setContentType("text/html;charset=UTF-8");
		
		// Traitement url d'un controlleur
		Matcher matcher = controlerPattern.matcher(request.getRequestURI());
		if(matcher.matches()) {
			// Méthode de l'URL 
			String url = matcher.group(2);
			Method controlerMethod = getUrlMethod(url);
			if(null == controlerMethod) {
				response.getWriter().write("Aucun controlleur correspond à " + url);
				logger.log(Level.SEVERE, "Aucun controlleur correspond à " + url);
				return;
			}
			
			// Mettre le premier caractère en majuscule
			Class<?> controlerClass = controlerMethod.getDeclaringClass();
			
			try {
				// Instanciation du controlleur
				Object controler = controlerClass.getConstructor().newInstance();
				
				// Ajout des données du GET ou POST au controlleur
				if("GET".equalsIgnoreCase(requestType)){
					controlerClass.getMethod("setGet", Map.class).invoke(controler, getParamsMap(request));
				} else if("POST".equalsIgnoreCase(requestType)){
					controlerClass.getMethod("setPost", Map.class).invoke(controler, getParamsMap(request));
				}
				
				//Résultats du requete http
				ModelView modelView =  (ModelView) controlerClass.getMethod(controlerMethod.getName(), null).invoke(controler);
				if(null == modelView) {
					throw new RuntimeException("La méthode " + controlerClass.getName() + "."
							+ controlerMethod.getName() + " doit retourner un ModelView.");
				}
				
				// Données envoyées aux vues
				if( !modelView.getData().isEmpty()) {
					// Ajout chaques données aux attributs du HttpRequest
					// pour qu'elles soient accessibles en utilisant ${sonNom } aux jsp
					for(Map.Entry<String, Object> d : modelView.getData().entrySet()) {
						request.setAttribute(d.getKey(), d.getValue());
					}
				} else {
					logger.log(
							Level.INFO, "La méthode " + controlerClass.getName() + "." 
							+ controlerMethod.getName() + "() ne retourne aucun data."
							);
				}
				
				// Redirection vers la vue jsp
				if(null != modelView.getPage()) {
					request.getRequestDispatcher("/vue/" + modelView.getPage() + ".jsp").forward(request, response);
				} else {
					response.getWriter().write("Pas de vue");
					logger.log(Level.INFO, "Pas de vue");
				}
				
			}catch(NoSuchMethodException e1) {
				response.getWriter().write("La classe " + controlerClass.getSimpleName() + " n'a pas de méthode " + controlerMethod.getName() + "()");
				logger.log(Level.SEVERE, "La classe " + controlerClass.getSimpleName() + " n'a pas de méthode " + controlerMethod.getName() + "()");
			}catch(InvocationTargetException e2) {
				logger.log(Level.SEVERE, e2.getCause().getMessage());
				e2.getCause().printStackTrace();
			}
			catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				 | SecurityException e) {
				logger.log(Level.SEVERE, e.getMessage());
				e.printStackTrace();
			}
				
		} 		
	}	

	/**
	 * Avoir la méthode du controlleur corespondant à l'URL entré
	 * @param url URL du méthode
	 * @param classes Vector qui contient les classes des contrôlleurs
	 * @return la méthode du controlleur correspondant à l'URL 
	 */
	private Method getUrlMethod(String url) {
		// Set des classes du package app.controler
		Reflections reflections = new Reflections("app.controller");
		Set<Class<? extends Controller>> classes = reflections.getSubTypesOf(Controller.class);		
		
		// Parcours du liste des classes des controlleurs
	    for (Class<?> klass : classes) {
	    	//Parcours des méthodes de chaque classe
	        for (Method method : klass.getDeclaredMethods()) {
	        	// Si la méthode est annoté avec @ControllerMethod
	            if (method.isAnnotationPresent(ControllerMethod.class)) {
	                ControllerMethod annotInstance = method.getAnnotation(ControllerMethod.class);
	                // Si la méthode à la même URL que celle entrée
	                if ( url.equalsIgnoreCase(annotInstance.uri()) ) {
	                	return method;
	                }
	            }
	        }
		}
	    
	    // Si aucune méthode n'a la même URL
	    return null;
	}
	
	/**
	 * Avoir les données d'un HttpServletRequest en Map
	 * @param request HttpServletRequest
	 * @return les données du HttpServletRequest en Map
	 */
	private Map<String, String> getParamsMap(HttpServletRequest request) {
		// Rélation String => String[]
		Map<String, String[]> paramMap = request.getParameterMap();
		// Celle qu'on veut
		// Rélation String => String
		Map<String, String> results = new HashMap<String, String>();
		
		// Transformation
		for(String paramName : paramMap.keySet()) {
			String[] values = paramMap.get(paramName);
			if( (null != values) && (values.length > 0) ) {
				results.put(paramName, values[0]);
			} else {
				results.put(paramName, null);
			}
		}
		
		return results;
	}
}
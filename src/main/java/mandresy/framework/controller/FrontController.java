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

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mandresy.framework.database.Database;
import mandresy.framework.database.DatabaseConnection;
import mandresy.framework.vue.ModelView;
import org.reflections.Reflections;

/**
 * FrontController de l'application. A part les requêtes HTTP des fichiers statique, 
 * toutes les requêtes seront affectés à cet Servlet. Cet Servlet aura pour fonction d'envoyer convenablement chaque
 * requête à la méthode du controlleur correspondant. Elle aura aussi pour rôle d'encapsuler les données reçues par GET ou par
 * POST en Map de String comme clé et object comme valeur, puis elle chargera la vue de la méthode du controlleur une fois que
 * cette derniere a fini ses opérations/traitements. 
 * 
 * @author Mandresy
 */
@WebServlet(description = "Front Controller des urls", urlPatterns = { "*.do" }, loadOnStartup = 1)
public class FrontController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger logger = Logger.getLogger("mandresy.framework.controller.FrontController");
	
	/**
	 * URI controlleur<br>
	 * Exemple:<br>
	 * /hello/displayhello.do[?name=Koto]
	 */
	private static final Pattern CONTROLER_PATH_PATTERN = Pattern.compile("^/([a-zA-Z_]+)\\.do(.)*$");
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FrontController() {
        super();
    }

    @Override
    public void init() throws ServletException {
    	super.init();
    	Database.connection = DatabaseConnection.getConnection();
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		dispatchRequest(request, response, "GET");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		dispatchRequest(request, response, "POST");
	}

	/**
	 * Traîtement de l'URL du client HTTP
	 * @param request HttpRequest
	 * @param response HttpResponse
	 * @param requestType <b>"GET"</b> pour la requête GET<br>
	 * et <b>"POST"</b> pour requête POST
	 * @throws IOException 
	 * @throws ServletException 
	 */
	private void dispatchRequest(HttpServletRequest request, HttpServletResponse response, String requestType) throws IOException, ServletException {
		response.setContentType("text/html;charset=UTF-8");
		
		// Traitement url du controlleur
		Matcher matcher = CONTROLER_PATH_PATTERN.matcher(request.getRequestURI());
		if(matcher.matches()) {
			// Méthode de l'URL 
			String url = matcher.group(1);
			Method controlerMethod = getUrlMethod(url);
			if(null == controlerMethod) {
				String msg = "Aucun controlleur correspond à " + url;
				response.getWriter().write(msg);
				logger.log(Level.SEVERE, msg);
				return;
			}
			
			// Mettre le premier caractère en majuscule
			Class<?> controlerClass = controlerMethod.getDeclaringClass();
			
			try {
				// Instanciation de la classe du controlleur
				Object controler = controlerClass.getConstructor().newInstance();
				
				// Ajout des données de la requête HTTP GET ou POST au controlleur
				if("GET".equalsIgnoreCase(requestType)){
					controlerClass.getMethod("setGet", Map.class).invoke(controler, getParamsMap(request));
				} else if("POST".equalsIgnoreCase(requestType)){
					controlerClass.getMethod("setPost", Map.class).invoke(controler, getParamsMap(request));
				}
				
				//Résultats du requête http
				ModelView modelView =  (ModelView) controlerClass.getMethod(controlerMethod.getName(), null).invoke(controler);
				if(null == modelView) {
					throw new RuntimeException("La méthode " + controlerClass.getName() + "."
							+ controlerMethod.getName() + " doit retourner un ModelView.");
				}
				
				// Données envoyées aux vues
				if( !modelView.getData().isEmpty()) {
					// Ajouter chaque donnée aux attributs du HttpRequest
					// pour qu'elles soient accessibles en utilisant ${sonNom} aux jsp
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
	 * @param url URL de la méthode
	 * @return la méthode du controlleur correspondant à l'URL
	 */
	private Method getUrlMethod(String url) {
		// Set des classes du package app
		Reflections reflections = new Reflections("app");
		Set<Class<? extends Controller>> classes = reflections.getSubTypesOf(Controller.class);		
		
		// Parcours de la liste des classes des controlleurs
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
	 * Récupérer les données du HttpServletRequest en Map
	 * @param request HttpServletRequest
	 * @return les données du HttpServletRequest en Map
	 */
	private Map<String, String> getParamsMap(HttpServletRequest request) {
		// Rélation String => String[]
		Map<String, String[]> paramMap = request.getParameterMap();
		// Celle qu'on veut
		// Rélation String => String
		Map<String, String> results = new HashMap<>();
		
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
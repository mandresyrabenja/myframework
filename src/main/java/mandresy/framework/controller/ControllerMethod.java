package mandresy.framework.controller;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation utilisée pour préciser l'URI d'une méthode d'un controlleur HTTP.
 * 
 * @author Mandresy
 *
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface ControllerMethod {

	/**
	 * URI du méthode annotée sans le suffixe ".do".<br>
	 * <b>Exemple:</b><br>
	 * S'une méthode a "hello" comme URI, l'URL d'accès à cet méthode sera http://site.domaine/hello.do
	 * @return L'URI du méthode
	 */
	public abstract String uri() default "";
	
}

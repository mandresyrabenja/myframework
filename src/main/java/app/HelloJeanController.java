package app;

import mandresy.framework.controller.Controller;
import mandresy.framework.controller.ControllerMethod;
import mandresy.framework.vue.ModelView;

public class HelloJeanController extends Controller {
    @ControllerMethod(uri = "helloJean")
    public ModelView helloJean() {
        view.setPage("hello-jean");

        return view;
    }
}

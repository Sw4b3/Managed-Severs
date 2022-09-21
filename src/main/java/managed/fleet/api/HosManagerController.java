package managed.fleet.api;
import managed.fleet.common.Services.VBoxHostManager;
import org.springframework.web.bind
        .annotation.GetMapping;
import org.springframework.web.bind
        .annotation.RequestMapping;
import org.springframework.web.bind
        .annotation.RestController;


// Creating the REST controller
@RestController
@RequestMapping(path = "/HosManager")
public class HosManagerController {

    // Implementing a GET method
    // to get the list of all
    // the employees
    @GetMapping(path = "/", produces = "application/json")

    public String CreateHost()
    {
        new VBoxHostManager().createHost();

        return "Ok";
    }
}
package fi.hsl.transitloghfpapi.config.enabledocs;

import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

@Controller
public class DocumentationRedirect {

    @RequestMapping("/")
    public String greetingRedirect() {
        return "redirect:/swagger-ui.html";
    }
}

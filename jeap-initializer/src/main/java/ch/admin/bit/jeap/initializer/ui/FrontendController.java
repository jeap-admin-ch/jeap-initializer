package ch.admin.bit.jeap.initializer.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class FrontendController {

    @GetMapping
    public String redirectIndex() {
        return "redirect:/wizard/step/select-template";
    }
}

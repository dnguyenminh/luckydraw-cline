package vn.com.fecredit.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DrawController {

    private final LuckyDrawService luckyDrawService;

    public DrawController(LuckyDrawService luckyDrawService) {
        this.luckyDrawService = luckyDrawService;
    }

    @GetMapping("/draw")
    public String drawWinners(@RequestParam(defaultValue = "1") int numWinners, Model model) {
        try {
            List<SpinResult> spinResults = luckyDrawService.drawWinners(numWinners);
            model.addAttribute("spinResults", spinResults);
        } catch (NoParticipantsException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "draw-results"; // Trả về tên template Thymeleaf


    }


    @GetMapping("/addParticipant")
    public String showAddParticipantForm(Model model) {

        return "add-participant";
    }

    @PostMapping("/addParticipant")
    public String addParticipant(@RequestParam String customerId, Model model) {
        luckyDrawService.createParticipant(customerId);
        model.addAttribute("message", "Đã thêm người chơi " + customerId);
        return "add-participant";

    }

}
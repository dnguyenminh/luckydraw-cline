package vn.com.fecredit.app;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/spin")
public class SpinController {
    private final RewardService rewardService;

    public SpinController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @PostMapping
    public String spin(Principal principal, Model model) { // Use Principal to get authenticated user information
        try {
            // Call rewardService to determine the spin result for the current user
            SpinResult spinResult = rewardService.determineWinningReward(principal.getName());

            // Add the spin result to the model for the view
            model.addAttribute("spinResult", spinResult);

            // Return the name of the view to display the spin result
            return "spin-result";
        } catch (Exception ex) { // Handle exceptions like NoParticipantsException
            // Add an error message to the model
            model.addAttribute("errorMessage", ex.getMessage());
            // Return the same view to display the error
            return "spin-result";
        }
    }
}
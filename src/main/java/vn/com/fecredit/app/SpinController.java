package vn.com.fecredit.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/spin")
public class SpinController {
    @Autowired
    private RewardService rewardService;

    @PostMapping
    public ResponseEntity<String> spin(@RequestParam String customerAccount) {
        String result = rewardService.spin(customerAccount);
        return ResponseEntity.ok(result);
    }
}
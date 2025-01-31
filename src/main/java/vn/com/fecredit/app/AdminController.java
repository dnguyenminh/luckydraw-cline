package vn.com.fecredit.app;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class AdminController {
    private final ConfigUploadService configUploadService;

    public AdminController(ConfigUploadService configUploadService) {
        this.configUploadService = configUploadService;
    }

    // Trang upload cấu hình
    @GetMapping("/admin/upload")
    public String uploadPage(Model model) {
        return "upload-config";
    }

    // Xử lý upload file phần thưởng
    @PostMapping("/admin/upload-rewards")
    public String uploadRewards(@RequestParam("file") MultipartFile file, Model model) {
        try {
            configUploadService.uploadRewardConfig(file);
            model.addAttribute("message", "Upload thành công!");
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi: " + e.getMessage());
        }
        return "upload-config";
    }

    // Xử lý upload file giờ vàng
    @PostMapping("/admin/upload-golden-hours")
    public String uploadGoldenHours(@RequestParam("file") MultipartFile file, Model model) {
        try {
            configUploadService.uploadGoldenHourConfig(file);
            model.addAttribute("message", "Upload thành công!");
        } catch (Exception e) {
            model.addAttribute("message", "Lỗi: " + e.getMessage());
        }
        return "upload-config";
    }
}
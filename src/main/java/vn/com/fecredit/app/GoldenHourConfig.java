package vn.com.fecredit.app;


import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoldenHourConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CsvBindByName(column = "startTime")
    @CsvDate("yyyy-MM-dd HH:mm") // Định dạng ngày giờ trong CSV
    @Column(nullable = false)
    private LocalDateTime startTime;

    @CsvBindByName(column = "endTime")
    @CsvDate("yyyy-MM-dd HH:mm") // Định dạng ngày giờ trong CSV
    @Column(nullable = false)
    private LocalDateTime endTime;

    @CsvBindByName(column = "rewardName")
    @Column(nullable = false)
    private String rewardName;
}
package vn.com.fecredit.app;


import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoldenHour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @CsvBindByName(column = "startDate")
    @CsvDate("yyyy-MM-dd")
    private LocalDate startDate;
    @CsvBindByName(column = "endDate")
    @CsvDate("yyyy-MM-dd")
    private LocalDate endDate;
    @CsvBindByName(column = "morningGoldenHour")
    private String morningGoldenHour;
    @CsvBindByName(column = "eveningGoldenHour")
    private String eveningGoldenHour;

    // Getters and setters

//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public LocalDate getStartDate() {
//        return startDate;
//    }
//
//    public void setStartDate(LocalDate startDate) {
//        this.startDate = startDate;
//    }
//
//    public LocalDate getEndDate() {
//        return endDate;
//    }
//
//    public void setEndDate(LocalDate endDate) {
//        this.endDate = endDate;
//    }
//
//    public String getMorningGoldenHour() {
//        return morningGoldenHour;
//    }
//
//    public void setMorningGoldenHour(String morningGoldenHour) {
//        this.morningGoldenHour = morningGoldenHour;
//    }
//
//    public String getEveningGoldenHour() {
//        return eveningGoldenHour;
//    }
//
//    public void setEveningGoldenHour(String eveningGoldenHour) {
//        this.eveningGoldenHour = eveningGoldenHour;
//    }
}
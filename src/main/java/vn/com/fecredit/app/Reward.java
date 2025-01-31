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
public class Reward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CsvBindByName(column = "name")
    private String name;
    @CsvBindByName(column = "totalQuantity")
    private Integer totalQuantity;
    @CsvBindByName(column = "probability")
    private Double probability;
    @CsvBindByName(column = "goldenHourProbability")
    private Double goldenHourProbability;
    @CsvBindByName(column = "startDate")
    @CsvDate("yyyy-MM-dd")
    private LocalDate startDate;
    @CsvBindByName(column = "endDate")
    @CsvDate("yyyy-MM-dd")
    private LocalDate endDate;
    @CsvBindByName(column = "limitFromDate")
    @CsvDate("yyyy-MM-dd")
    private LocalDate limitFromDate;
    @CsvBindByName(column = "limitToDate")
    @CsvDate("yyyy-MM-dd")
    private LocalDate limitToDate;
    @CsvBindByName(column = "maxQuantityPerPeriod")
    private Integer maxQuantityPerPeriod;

    // Getters and setters

//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public Integer getTotalQuantity() {
//        return totalQuantity;
//    }
//
//    public void setTotalQuantity(Integer totalQuantity) {
//        this.totalQuantity = totalQuantity;
//    }
//
//    public Double getProbability() {
//        return probability;
//    }
//
//    public void setProbability(Double probability) {
//        this.probability = probability;
//    }
//
//    public Double getGoldenHourProbability() {
//        return goldenHourProbability;
//    }
//
//    public void setGoldenHourProbability(Double goldenHourProbability) {
//        this.goldenHourProbability = goldenHourProbability;
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
//    public LocalDate getLimitFromDate() {
//        return limitFromDate;
//    }
//
//    public void setLimitFromDate(LocalDate limitFromDate) {
//        this.limitFromDate = limitFromDate;
//    }
//
//    public LocalDate getLimitToDate() {
//        return limitToDate;
//    }
//
//    public void setLimitToDate(LocalDate limitToDate) {
//        this.limitToDate = limitToDate;
//    }
//
//    public Integer getMaxQuantityPerPeriod() {
//        return maxQuantityPerPeriod;
//    }
//
//    public void setMaxQuantityPerPeriod(Integer maxQuantityPerPeriod) {
//        this.maxQuantityPerPeriod = maxQuantityPerPeriod;
//    }
}
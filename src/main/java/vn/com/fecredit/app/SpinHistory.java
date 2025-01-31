package vn.com.fecredit.app;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpinHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerId;
    private String rewardName;
    private boolean winner;
    private LocalDate spinDate;

//    public String getCustomerId() {
//        return customerId;
//    }
//
//    public String getRewardName() {
//        return rewardName;
//    }
//
//    public boolean isWinner() {
//        return isWinner;
//    }
//
//    public LocalDate getSpinDate() {
//        return spinDate;
//    }
}



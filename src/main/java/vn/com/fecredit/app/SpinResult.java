package vn.com.fecredit.app;

public class SpinResult {
    private boolean isWinner;
    private String rewardName;

    // Constructor
    public SpinResult(boolean isWinner, String rewardName) {
        this.isWinner = isWinner;
        this.rewardName = rewardName;
    }

    // Getters
    public boolean isWinner() {
        return isWinner;
    }

    public String getRewardName() {
        return rewardName;
    }
}
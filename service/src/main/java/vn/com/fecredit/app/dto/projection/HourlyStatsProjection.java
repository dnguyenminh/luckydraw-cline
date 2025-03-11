package vn.com.fecredit.app.dto.projection;

public interface HourlyStatsProjection {
    Integer getHour();
    Long getTotalSpins();
    Long getWinningSpins();
    Double getWinRate();
}

package vn.com.fecredit.app.projection;

public interface HourlyStatsProjection {
    int getHour();
    long getCount();
    long getWinCount();
}

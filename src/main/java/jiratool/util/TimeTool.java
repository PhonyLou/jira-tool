package jiratool.util;

import java.util.Calendar;

public class TimeTool {
    public static Float getWorkDay(final long startMillis, final long endMillis) {
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(startMillis);
        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(endMillis);

        long day = 86400000;
        float dayGap = (float) (endMillis - startMillis) / day;

        if (dayGap < 1) {
            return dayGap;
        }

        int whichDayIsStartDay = start.get(Calendar.DAY_OF_WEEK);
        int weekGap = (int) (dayGap / 7);
        int weekends = weekGap * 2;
        int weeks = (int) (dayGap % 7);

        if (whichDayIsStartDay + weeks > 7) weekends = weekends + 2;
        if (whichDayIsStartDay + weeks == 7) weekends++;

        return dayGap - weekends;
    }

    public static Double roundUp(final float data, int decimal) {
        double reserved = Math.pow(10, decimal);
        return Math.round(data * reserved) / reserved;
    }
}

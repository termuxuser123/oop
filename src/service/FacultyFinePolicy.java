package service;

public class FacultyFinePolicy implements FinePolicy {
    private static final double RATE_PER_DAY = 5.0;
    private static final int GRACE_PERIOD_DAYS = 3;

    @Override
    public double calculateFine(int overdueDays) {
        if (overdueDays <= GRACE_PERIOD_DAYS) {
            return 0.0;
        }

        int billableDays = overdueDays - GRACE_PERIOD_DAYS;
        return billableDays * RATE_PER_DAY;
    }

}
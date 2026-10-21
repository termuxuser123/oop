package service;

public class StandardFinePolicy implements FinePolicy {
    // 10 rupees per day for per overdue day
    private static final double RATE_PER_DAY = 10.0;

    @Override

    public double calculateFine(int overdueDays) {
        if (overdueDays <= 0) {
            return 0.0;
        }
        return overdueDays * RATE_PER_DAY;
    }

}
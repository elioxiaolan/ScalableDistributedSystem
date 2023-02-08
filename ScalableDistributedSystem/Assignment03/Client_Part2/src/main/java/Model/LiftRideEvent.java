package Model;

public class LiftRideEvent {
    private int resortId;
    private String seasonId;
    private String dayId;
    private int skierId;
    private int liftId;
    private int time;

    public LiftRideEvent(int resortId, String seasonId, String dayId, int skierId, int liftId, int time) {
        this.resortId = resortId;
        this.seasonId = seasonId;
        this.dayId = dayId;
        this.skierId = skierId;
        this.liftId = liftId;
        this.time = time;
    }

    public int getResortId() {
        return this.resortId;
    }

    public String getSeasonId() {
        return this.seasonId;
    }

    public String getDayId() {
        return this.dayId;
    }

    public int getSkierId() {
        return this.skierId;
    }

    public int getLiftId() {
        return this.liftId;
    }

    public int getTime() {
        return this.time;
    }
}

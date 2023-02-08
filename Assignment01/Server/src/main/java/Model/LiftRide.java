package Model;

public class LiftRide {
    private Integer time;
    private Integer liftID;

    public LiftRide(Integer time, Integer liftID) {
        this.time = time;
        this.liftID = liftID;
    }

    public Integer getTime() {
        return this.time;
    }

    public Integer getLiftID() {
        return this.liftID;
    }

    public String toString() {
        return "Time: " + this.time + ", " + "LiftID: " + this.liftID;
    }
}

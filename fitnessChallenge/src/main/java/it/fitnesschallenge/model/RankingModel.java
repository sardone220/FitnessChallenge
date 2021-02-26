package it.fitnesschallenge.model;

public class RankingModel implements Comparable<RankingModel> {

    private String user;
    private double point;

    public RankingModel(String user, double point) {
        this.user = user;
        this.point = point;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public double getPoint() {
        return point;
    }

    public void setPoint(double point) {
        this.point = point;
    }

    @Override
    public int compareTo(RankingModel rankingModel) {
        Double thisPoint = this.point;
        Double otherPoint = rankingModel.getPoint();
        return thisPoint.compareTo(otherPoint);
    }
}

package com.ved.veddriver.Model;

/**
 * Created by NaRan on 8/21/17 at 10:31.
 */

public class RouteStats {

    private String auth_code;
    private String startDateMorning;
    private String endDateMorning;
    private String startDateEvening;
    private String endDateEvening;

    public RouteStats() {
    }

    public RouteStats(String auth_code, String startDateMorning, String endDateMorning, String startDateEvening, String endDateEvening) {
        this.auth_code = auth_code;
        this.startDateMorning = startDateMorning;
        this.endDateMorning = endDateMorning;
        this.startDateEvening = startDateEvening;
        this.endDateEvening = endDateEvening;
    }

    public String getAuth_code() {
        return auth_code;
    }

    public void setAuth_code(String auth_code) {
        this.auth_code = auth_code;
    }

    public String getStartDateMorning() {
        return startDateMorning;
    }

    public void setStartDateMorning(String startDateMorning) {
        this.startDateMorning = startDateMorning;
    }

    public String getEndDateMorning() {
        return endDateMorning;
    }

    public void setEndDateMorning(String endDateMorning) {
        this.endDateMorning = endDateMorning;
    }

    public String getStartDateEvening() {
        return startDateEvening;
    }

    public void setStartDateEvening(String startDateEvening) {
        this.startDateEvening = startDateEvening;
    }

    public String getEndDateEvening() {
        return endDateEvening;
    }

    public void setEndDateEvening(String endDateEvening) {
        this.endDateEvening = endDateEvening;
    }
}

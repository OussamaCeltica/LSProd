package com.leadersoft.celtica.lsprod.Maintenances;

public class DateMaintenance extends Maintenance {
    public String date;

    public DateMaintenance(String date) {
        super(date);
        this.date = date;
    }

}

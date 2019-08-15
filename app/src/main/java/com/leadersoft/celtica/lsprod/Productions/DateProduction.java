package com.leadersoft.celtica.lsprod.Productions;

import com.leadersoft.celtica.lsprod.Productions.PublicationProduction;

/**
 * Created by celtica on 14/11/18.
 */

public class DateProduction extends PublicationProduction {
    String date;

    public DateProduction( String date) {
        super("date");
        this.date = date;
    }
}

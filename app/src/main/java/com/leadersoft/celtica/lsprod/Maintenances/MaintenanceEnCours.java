package com.leadersoft.celtica.lsprod.Maintenances;

/**
 * Created by celtica on 17/02/19.
 */

public class MaintenanceEnCours extends Maintenance {
    public MaintenanceEnCours(String id, String date_debut, String date_fin, String code_ligne, String code_employé, String type_maintenance) {
        super(id, date_debut, date_fin, code_ligne, code_employé, type_maintenance);
    }
}

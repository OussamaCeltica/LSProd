package com.leadersoft.celtica.lsprod.Productions;

/**
 * Created by celtica on 04/04/19.
 */

public class ProductionSupprimé extends Production {
    public ProductionSupprimé(String id, String codebar, String code_chef, String code_ligne, String heure, String heure_fin, double qt) {
        super(id, codebar, code_chef, code_ligne, heure, heure_fin, qt);
        super.type="supprimé";
    }
}

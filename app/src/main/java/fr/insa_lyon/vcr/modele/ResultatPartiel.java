package fr.insa_lyon.vcr.modele;

/**
 * Created by Lilian on 29/04/2015.
 */
public class ResultatPartiel {
    private String resultatAffiche;
    private String identifiantPlace;

    public ResultatPartiel(String resultatAffiche, String identifiantPlace){
        this.resultatAffiche = resultatAffiche;
        this.identifiantPlace = identifiantPlace;
    }

    public String getIdentifiantPlace(){
        return identifiantPlace;
    }

    @Override
    public String toString(){
        return resultatAffiche;
    }
}

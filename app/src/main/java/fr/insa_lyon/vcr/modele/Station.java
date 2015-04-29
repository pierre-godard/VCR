package fr.insa_lyon.vcr.modele;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lilian on 29/04/2015.
 */
public class Station {
    String nom;
    LatLng position;
    int capacite;
    int velosRestant;

    public Station(JSONObject json){
        try {
            nom = json.getString("name");
            position= new LatLng(json.getDouble("latitude"),json.getDouble("longitude"));
        }
        catch (JSONException e) {
            Log.e("STATION", "le parsage de Json marche pas :(");
        }
        //TODO ajouter support mesures quand ce sera fait
    }

    public String getNom(){
        return nom;
    }

    public LatLng getPosition(){
        return position;
    }

    public String getSnippetDeposerVelo(){
        return "Snippet du mode déposer";
        //TODO: Generer un snippet pour le mode déposer indiquant entre autres le nombre de places libres restantes
    }

    public String getSnippetRetirerVelo(){
        return "Snippet du mode retirer";
        //TODO: Generer un snippet pour le mode retirer indiquant entre autres le nombre de vélos restants
    }
}

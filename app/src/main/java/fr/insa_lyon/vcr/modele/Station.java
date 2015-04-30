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
            //capacite= json.getString()
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

    public String getSnippetDeposer(){
        return "Snippet mode déposer";
        //TODO: Generer un snippet avec le nombre de places libres pour mode déposer
    }

    public String getSnippetRetirer(){
        return "Snippet mode retirer";
        //TODO: Generer un snippet avec le nombre de vélos disponibles pour mode retirer
    }
}

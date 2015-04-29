package fr.insa_lyon.vcr.modele;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Lilian on 29/04/2015.
 */
public class MarqueurPerso {
    private Station station;
    private Marker marqueur;

    public MarqueurPerso(Station station, Marker marqueur){
       this.station = station;
       this.marqueur = marqueur;
    }

    public Station getStation(){
        return station;
    }

    public Marker getMarqueur(){
        return marqueur;
    }
}

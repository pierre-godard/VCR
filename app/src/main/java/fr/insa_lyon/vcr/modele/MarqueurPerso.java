package fr.insa_lyon.vcr.modele;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

import fr.insa_lyon.vcr.vcr.R;

/**
 * Created by Lilian on 29/04/2015.
 */
public class MarqueurPerso {
    private Station station;
    private Marker marqueur;

    public MarqueurPerso(Station station, Marker marqueur){
       this.station = station;
       this.marqueur = marqueur;
        //marquer vert par défaut
        //this.marqueur.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        this.marqueur.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marqueurperso));
    }

    public Station getStation(){
        return station;
    }

    public Marker getMarqueur(){
        return marqueur;
    }
}

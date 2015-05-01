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
        this.marqueur.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.marqueurperso));
    }

    public Station getStation(){
        return station;
    }

    public Marker getMarqueur(){
        return marqueur;
    }

    public void setMarkerSnippet(String text) {
        marqueur.setSnippet(text);
    }

    public void setMarkerTitle(String text) {
        marqueur.setTitle(text);
    }


    /**
     * Will be used to change the color of the icon of a station
     * according to the number of bikes inside
     *
     * @param color
     */
    public void switchIcon(int color) {
        switch (color) {
            case 1:
                // green
                break;
            case 2:
                // less green
                break;
            case 3:
                // yellow
                break;
            case 4:
                // orange
                break;
            case 5:
                // red
                break;
            default:

                break;
        }
    }
}

package fr.insa_lyon.vcr.utilitaires;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Lilian on 04/05/2015.
 */
public class UsefulConstants {

    public static LatLng sudOuestLyon = new LatLng(45.708931, 4.745801);
    public static LatLng nordEstLyon = new LatLng(45.805918, 4.924447);
    public static LatLngBounds rectangleLyon = new LatLngBounds(sudOuestLyon, nordEstLyon);

}

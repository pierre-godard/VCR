package fr.insa_lyon.vcr.vcr;

//import android.app.FragmentManager;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;




public class VelocityRaptorMain extends Activity implements android.app.FragmentManager.OnBackStackChangedListener {

    // Est-ce qu'on affiche le user input field ou non (sinon on affiche la map).
    private boolean mShowingBack = false;
    private Handler mHandler = new Handler();
    public static FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        fragmentManager = getFragmentManager();

        if (savedInstanceState == null) {       // pas d'instance sauvegardée
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, new GoogleMapFragment())     // google map comme fragment principal de la carte
                    .commit();
        } else {
            mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
        }
        getFragmentManager().addOnBackStackChangedListener(this);
    }

    public void cardFlipButton(View v)
    {
        flipCard();
    }


    private void flipCard() {
        if (mShowingBack) {
            getFragmentManager().popBackStack();
            return;
        }

        // Afficher le "dos" de la cart
        mShowingBack = true;
        // Create and commit a new fragment transaction that adds the fragment for the back of
        // the card, uses custom animations, and is part of the fragment manager's back stack.

        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                .replace(R.id.container, new UserInputFragment())
                .addToBackStack(null)               // permet de "revenir en arrière" sur la transaction
                .commit();

        // Defer an invalidation of the options menu (on modern devices, the action bar). This
        // can't be done immediately because the transaction may not yet be committed. Commits
        // are asynchronous in that they are posted to the main thread's message loop.
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    public void onBackStackChanged() {
        mShowingBack = (getFragmentManager().getBackStackEntryCount() > 0);
        // When the back stack changes, invalidate the options menu (action bar).
        invalidateOptionsMenu();
    }
}

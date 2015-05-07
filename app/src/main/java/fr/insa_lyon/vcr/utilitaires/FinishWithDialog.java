package fr.insa_lyon.vcr.utilitaires;

/**
 * Created by julien on 30/04/15.
 */
public interface FinishWithDialog {

    /**
     * When user click "Ok" button on the dialog, this methods is called in VelocityRaptor main to exit
     * application.
     */
    public void onChoose();
}

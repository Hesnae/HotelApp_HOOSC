package org.mbds.nfchotel_hoosc.write;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;



import org.mbds.nfchotel_hoosc.model.TagType;
import org.mbds.nfctag.R;

public class NFCWriterActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    private NfcTagViewModel viewModel;
    private TagType tagType;
    private String tagText;

    // TODO Analyser le code et comprendre ce qui est fait
    // TODO Ajouter un formulaire permettant à un utilisateur d'entrer le texte à mettre dans le tag
    // TODO Le texte peut être 1) une URL 2) un numéro de téléphone 3) un plain texte
    // TODO Utiliser le view binding
    // TODO L'app ne doit pas crasher si les tags sont mal formattés
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_tag_layout);

        // init ViewModel
        viewModel = new ViewModelProvider(this).get(NfcTagViewModel.class);

        viewModel.getTagWritten().observe(this, writeSuccess -> {
                    Toast.makeText(NFCWriterActivity.this, "Tag written successfully", Toast.LENGTH_SHORT).show();
                }
        );

        viewModel.getWrittenFailed().observe(this, writeFailed -> {
            Toast.makeText(NFCWriterActivity.this, "Tag writing failed!", Toast.LENGTH_SHORT).show();
        });


        //Get default NfcAdapter and PendingIntent instances
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // check NFC feature:
        if (nfcAdapter == null) {
            // process error device not NFC-capable…

        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // single top flag avoids activity multiple instances launching
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Enable NFC foreground detection
        //

        if (nfcAdapter != null) {

            if (!nfcAdapter.isEnabled()) {
                // TODO afficher un message d'erreur à l'utilisateur si le NFC n'est pas activé
                Toast.makeText(this, "Please enable NFC in your device settings.", Toast.LENGTH_SHORT).show();
                // TODO rediriger l'utilisateur vers les paramètres du téléphone pour activer le NFC
                Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                startActivity(intent);

            } else {
                nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
            }
        } else {
            // TODO afficher un message d'erreur à l'utilisateur si le téléphone n'est pas NFC-capable

            // TODO Fermer l'activité ou rediriger l'utilisateur vers une autre activité
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Erreur");
            builder.setMessage("Votre téléphone ne prend pas en charge le NFC");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Action à effectuer lorsque l'utilisateur clique sur le bouton Fermer
                        finish(); // Ferme l'activité en cours
                    }
                });
            builder.setCancelable(false); // Empêche l'utilisateur de fermer la boîte de dialogue en appuyant sur le bouton retour
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Disable NFC foreground detection
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    public void parseForm()
    {
        tagText = "";
        tagType = TagType.TEXT;

        EditText txt = findViewById(R.id.TEXT_input);
        EditText number = findViewById(R.id.PHONE_input);
        EditText url = findViewById(R.id.URL_input);

        String text_str = txt.getText().toString();
        String number_str = number.getText().toString();
        String url_str = url.getText().toString();
        // Utilisez une expression régulière pour vérifier si le texte saisi est une URL, un numéro de téléphone ou du texte simple
        if(!text_str.isEmpty()) {
            tagText = text_str;
            tagType = TagType.TEXT;
        }
        else if(!number_str.isEmpty()) {
            if (number_str.matches("^(05|06|07)[0-9]{8}$")) {
                // Le texte saisi est un numéro de téléphone
                // Traitez le texte comme un numéro de téléphone
                tagText = number_str;
                tagType = TagType.PHONE;
            }
            else
                Toast.makeText(NFCWriterActivity.this, "Phone number incorrect", Toast.LENGTH_SHORT).show();

        }
        else if(!url_str.isEmpty()) {
            if (url_str.matches("^https?://.+")) {
                // Le texte saisi est une URL
                // Traitez le texte comme une URL
                tagText = url_str;
                tagType = TagType.URL;
            }
            else
                Toast.makeText(NFCWriterActivity.this, "URL bad formatted!", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * This method is called when a new intent is detected by the system, for instance when a new NFC tag is detected.
     *
     * @param intent The new intent that was started for the activity.
     */
    @Override
    public void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        String action = intent.getAction();
        // check the event was triggered by the tag discovery
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            // get the tag object from the received intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            parseForm();
            if(!tagText.isEmpty())
                viewModel.writeTag(tagText, tag, tagType);
        } else {
            // TODO Indiquer à l'utilisateur que ce type de tag n'est pas supporté
            Toast.makeText(NFCWriterActivity.this, "Tag type not supported", Toast.LENGTH_SHORT).show();

        }
    }
}

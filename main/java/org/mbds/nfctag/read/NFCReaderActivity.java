package org.mbds.nfchotel_hoosc.read;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import org.mbds.nfchotel_hoosc.R;
import org.mbds.nfchotel_hoosc.model.TagContent;
import org.mbds.nfchotel_hoosc.model.TagType;
import org.mbds.nfchotel_hoosc.utils.Animation;
import org.mbds.nfchotel_hoosc.write.NFCWriterActivity;
import org.mbds.nfchotel_hoosc.write.NfcTagViewModel;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NFCReaderActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    public static String TAG = "TAG";
    private NfcReaderViewModel nfcReaderViewModel;

    // TODO Lire le contenu d'un tag et effectuer les actions en fonction du contenu
    // TODO Si c'est un numéro de téléphone, lancer un appel
    // TODO Si c'est une page web lancer un navigateur pour afficher la page
    // TODO Sinon afficher le contenu dans la textviewx
    // TODO utiliser le view binding
    // TODO Faire en sorte que l'app ne crash pas si le tag est vierge ou mal formatté
    // TODO Demander à l'utilisateur d'activer le NFC, s'il ne l'est pas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.read_tag_layout);

        nfcReaderViewModel= new ViewModelProvider(this).get(NfcReaderViewModel.class);

        nfcReaderViewModel.getReadFailed().observe(this, readFailed -> {
            Toast.makeText(this, readFailed.getMessage(), Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getApplicationContext(), NFCWriterActivity.class);
            startActivity(i);
        });

        nfcReaderViewModel.getTagRead().observe(this, readSuccess -> {
            for (TagContent s : readSuccess) {
                String content = new String(s.getContent());
                Log.e("MEL", content);
                if (s.getType() ==TagType.PHONE) {
                    // Lancer un appel
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + content));
                    startActivity(intent);
                } else if (s.getType() == TagType.URL) {
                    // Lancer un navigateur web
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(content));
                    startActivity(intent);
                } else {
                    // Afficher le contenu dans la textview
                    Toast.makeText(this, s.getContent(), Toast.LENGTH_SHORT).show();

                }
            }
        });
        //Get default NfcAdapter and PendingIntent instances
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // check NFC feature:
        if (nfcAdapter == null) {
            // TODO Afficher un message d'erreur si le téléphone n'est pas compatible NFC
            Toast.makeText(this, "Votre téléphone n'a pas de NFC", Toast.LENGTH_SHORT).show();
            finish();
        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Enable NFC foreground detection
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled()) {
                // TODO Afficher un popup demandant à l'utilisateur d'activer le NFC
                // TODO rediriger l'utilisateur vers les paramètres du téléphone
                new AlertDialog.Builder(this)
                        .setTitle("NFC désactivé")
                        .setMessage("Voulez-vous activer le NFC ?")
                        .setPositiveButton("Activer", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Redirige l'utilisateur vers les paramètres du téléphone pour activer le NFC
                                startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                            }
                        })
                        .setNegativeButton("Annuler", null)
                        .show();
            }
             else {
                nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
            }
        } else {
            // TODO indiquer à l'utilisateur que son téléphone n'a pas de NFC
            Toast.makeText(this, "Votre téléphone n'a pas de NFC", Toast.LENGTH_LONG).show();
        }



        //TODO Réaliser les actions en fonction du contenu du tag
        // TODO Si c'est un numéro de téléphone, lancer un appel
        // TODO Si c'est une page web lancer un navigateur pour afficher la page
        // TODO Sinon afficher le contenu dans la textview

    }

    private boolean isWebUrl(String s) {
        return URLUtil.isValidUrl(s);
    }

    private boolean isPhoneNumber(String s) {
        // Expression régulière qui match les numéros de téléphone valides
        String phoneNumberRegex = "^(\\+[1-9][0-9]*(\\([0-9]*\\)|-[0-9]*-))?[0]?[1-9][0-9\\- ]*$";
        Pattern pattern = Pattern.compile(phoneNumberRegex);
        Matcher matcher = pattern.matcher(s);
        return matcher.matches();
    }

    {
    animateNfcTag();

}

    private void animateNfcTag() {
        //Animation.animateCard(findViewById(R.id.card_image));
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Disable NFC foreground detection
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }

    }

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
            // TODO Vérifier que le tag est bien formaté en NDEF
            boolean isNdef = false;
            for (String tech : tag.getTechList()) {
                Log.e("MEL", tech);
                if (tech.equals(Ndef.class.getName())) {
                    isNdef = true;
                    break;
                }
            }

            if (!isNdef) {
                Intent writeTagIntent = new Intent(this, NFCWriterActivity.class);
                // Afficher un message d'erreur et rediriger l'utilisateur vers l'activité d'écriture
                Toast.makeText(this, "Le tag NFC n'est pas formaté en NDEF", Toast.LENGTH_SHORT).show();
                // TODO Rediriger l'utilisateur vers l'activité d'écriture
                startActivity(writeTagIntent);
            }
            else {
                Log.e("MEL", "PROCESS TAG");
                Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                nfcReaderViewModel.processNfcTag(rawMsgs);
                // TODO Si non, afficher un message d'erreur et rediriger l'utilisateur vers l'activité d'écriture
                // hadi dertha f la ligne 97
            }
        }
    }






}


package org.mbds.nfchotel_hoosc.read;

import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.os.Parcelable;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.mbds.nfchotel_hoosc.model.TagContent;
import org.mbds.nfchotel_hoosc.model.TagType;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NfcReaderViewModel extends ViewModel {

    private MutableLiveData<List<TagContent>> onTagRead = new MutableLiveData<>();
    private MutableLiveData<Exception> onReadFailed = new MutableLiveData<>();

    public LiveData<Exception> getReadFailed() {
        return onReadFailed;
    }

    public LiveData<List<TagContent>> getTagRead() {
        return onTagRead;
    }

    private String getTextFromNdefRecord(NdefRecord ndefRecord) {
        return null;
    }

    private TagType getTypeFromNdefRecord(NdefRecord ndefRecord) {
        Log.e("MEL", "record inf: " + ndefRecord.getTnf());
        //check NDEF record TNF:
        //======================
        switch (ndefRecord.getTnf()) {
            case NdefRecord.TNF_ABSOLUTE_URI:
                break;
            case NdefRecord.TNF_EXTERNAL_TYPE:
                // manage NDEF record as an URN (<domain_name>:<service_name>)
                break;
            case NdefRecord.TNF_MIME_MEDIA:
                // manage NDEF record as the MIME type is:
                // picture, video, sound, JSON, etc…
                break;

            case NdefRecord.TNF_WELL_KNOWN:
                if(Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_URI)) {
                    Uri uri = ndefRecord.toUri();
                    if ("tel".equals(uri.getScheme()))
                        return TagType.PHONE;
                    else
                        return TagType.URL;
                }
                // manage NDEF record as the type is:
                // contact (business card), phone number, email…
                break;
            default:
                // manage NDEF record as text…
        }

        return TagType.TEXT;
    }

    private String getNdefContent(NdefRecord ndefRecord) throws UnsupportedEncodingException {
        byte[] payload = ndefRecord.getPayload();

        if(getTypeFromNdefRecord(ndefRecord) == TagType.URL)
        {
            Uri uri = ndefRecord.toUri();
            String url = uri.toString();
            return url;
        }

        if(getTypeFromNdefRecord(ndefRecord) == TagType.PHONE)
        {
            Uri uri = ndefRecord.toUri();
            String phoneNumber = uri.getSchemeSpecificPart();
            return phoneNumber;
        }
        //parse NDEF record as String:
        //============================
        String encoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTf-8";
        int languageSize = payload[0] & 0063;
        return new String(payload, languageSize + 1,
                payload.length - languageSize - 1, encoding);
    }

    public void processNfcTag(Parcelable[] rawMsgs) {

        if (rawMsgs != null && rawMsgs.length != 0) {
            List<TagContent> tagContents = new ArrayList<>();

            // instantiate a NDEF message array to get NDEF records
            NdefMessage[] ndefMessage = new NdefMessage[rawMsgs.length];
            // loop to get the NDEF records
            for (int i = 0; i < rawMsgs.length; i++) {
                ndefMessage[i] = (NdefMessage) rawMsgs[i];
                for (int j = 0; j < ndefMessage[i].getRecords().length; j++) {
                    NdefRecord ndefRecord = ndefMessage[i].getRecords()[j];
                    try {
                        tagContents.add(new TagContent(getNdefContent(ndefRecord), getTypeFromNdefRecord(ndefRecord)));
                    } catch (UnsupportedEncodingException e) {
                        onReadFailed.setValue(e);
                    }
                }
            }
            onTagRead.setValue(tagContents);
        } else {
            onReadFailed.setValue(new Exception("No NDEF message found!"));
        }
    }
}

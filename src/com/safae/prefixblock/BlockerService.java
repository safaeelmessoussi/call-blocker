package com.safae.prefixblock;

import android.net.Uri;
import android.telecom.Call;
import android.telecom.CallScreeningService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Android asks this service about every incoming call from a number that is not in
 * your contacts, BEFORE the phone rings. Matching calls are rejected immediately,
 * so your phone never rings and the network handles the call as "rejected".
 */
public class BlockerService extends CallScreeningService {
    private static final int DIRECTION_INCOMING = 0;   // Call.Details.DIRECTION_INCOMING
    private static final int PRESENTATION_ALLOWED = 1; // TelecomManager.PRESENTATION_ALLOWED

    @Override
    public void onScreenCall(Call.Details details) {
        boolean block = false;
        String number = "";
        String reason = "";
        Store store = new Store(this);
        try {
            if (store.isEnabled() && details.getCallDirection() == DIRECTION_INCOMING) {
                Uri handle = details.getHandle();
                number = handle == null ? "" : handle.getSchemeSpecificPart();
                boolean hidden = number == null || Rules.normalize(number).length() == 0
                        || details.getHandlePresentation() != PRESENTATION_ALLOWED;
                if (hidden) {
                    if (store.blockHidden()) { block = true; reason = "hidden number"; number = "Hidden"; }
                } else {
                    String p = Rules.match(number, store.getPrefixes());
                    if (p != null) { block = true; reason = "starts with " + p; }
                }
            }
        } catch (Throwable t) {
            block = false; // never break normal calls because of an error
        }

        CallResponse response;
        if (block) {
            response = new CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipCallLog(!store.showInCallLog())
                    .setSkipNotification(true)
                    .build();
            String time = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(new Date());
            store.addLog(time + "   " + number + "   (" + reason + ")");
        } else {
            response = new CallResponse.Builder().build();
        }
        respondToCall(details, response);
    }
}

/*
 * Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.integration.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tburke.loyaltykeyring.R;

/**
 * <p>
 * A utility class which helps ease integration with Barcode Scanner via
 * {@link Intent}s. This is a simple way to invoke barcode scanning and receive
 * the result, without any need to integrate, modify, or learn the project's
 * source code.
 * </p>
 * 
 * <h2>Initiating a barcode scan</h2>
 * 
 * <p>
 * To integrate, create an instance of {@code IntentIntegrator} and call
 * {@link #initiateScan()} and wait for the result in your app.
 * </p>
 * 
 * <p>
 * It does require that the Barcode Scanner (or work-alike) application is
 * installed. The {@link #initiateScan()} method will prompt the user to
 * download the application, if needed.
 * </p>
 * 
 * <p>
 * There are a few steps to using this integration. First, your {@link Activity}
 * must implement the method {@link Activity#onActivityResult(int, int, Intent)}
 * and include a line of code like this:
 * </p>
 * 
 * <pre>
 * {@code
 * public void onActivityResult(
 *     int requestCode, int resultCode, Intent intent) {
 *   IntentResult scanResult = IntentIntegrator.parseActivityResult(
 *       requestCode, resultCode, intent);
 *   if (scanResult != null) {
 *     // handle scan result
 *   }
 *   // else continue with any other code you need in the method
 *   ...
 * }
 * }
 * </pre>
 * 
 * <p>
 * This is where you will handle a scan result.
 * </p>
 * 
 * <p>
 * Second, just call this in response to a user action somewhere to begin the
 * scan process:
 * </p>
 * 
 * <pre>
 * {
 *     &#064;code
 *     IntentIntegrator integrator = new IntentIntegrator(yourActivity);
 *     integrator.initiateScan();
 * }
 * </pre>
 * 
 * <p>
 * Note that {@link #initiateScan()} returns an {@link AlertDialog} which is
 * non-null if the user was prompted to download the application. This lets the
 * calling app potentially manage the dialog. In particular, ideally, the app
 * dismisses the dialog if it's still active in its {@link Activity#onPause()}
 * method.
 * </p>
 * 
 * <p>
 * You can use {@link #setTitle(String)} to customize the title of this download
 * prompt dialog (or, use {@link #setTitleByID(int)} to set the title by string
 * resource ID.) Likewise, the prompt message, and yes/no button labels can be
 * changed.
 * </p>
 * 
 * <p>
 * Finally, you can use {@link #addExtra(String, Object)} to add more parameters
 * to the Intent used to invoke the scanner. This can be used to set additional
 * options not directly exposed by this simplified API.
 * </p>
 * 
 * <p>
 * By default, this will only allow applications that are known to respond to
 * this intent correctly do so. The apps that are allowed to respond can be set
 * with {@link #setTargetApplications(List)}. For example, set to
 * {@link #TARGET_BARCODE_SCANNER_ONLY} to only target the Barcode Scanner app
 * itself.
 * </p>
 * 
 * <h2>Sharing text via barcode</h2>
 * 
 * <p>
 * To share text, encoded on-screen in your choice of format, similarly, see
 * {@link #shareText(CharSequence, CharSequence)}.
 * </p>
 * 
 * <p>
 * Some code, particularly download integration, was contributed from the
 * Anobiit application.
 * </p>
 * 
 * <h2>Enabling experimental barcode formats</h2>
 * 
 * <p>
 * Some formats are not enabled by default even when scanning with
 * {@link #ALL_CODE_TYPES}, such as PDF417. Use
 * {@link #initiateScan(java.util.Collection)} with a collection containing the
 * names of formats to scan for explicitly, like "PDF_417", to use such formats.
 * </p>
 * 
 * @author Sean Owen
 * @author Fred Lin
 * @author Isaac Potoczny-Jones
 * @author Brad Drehmer
 * @author gcstang
 * @author tburke
 */
public final class IntentIntegrator {

    /**
     * Request code to identify requests started/handled by the integrator.
     * Apparently, only use bottom 16 bits.
     */
    public static final int REQUEST_CODE = 0x0000c0de;

    /**
     * Package name for ZXing barcode scanner.
     */
    private static final String BS_PACKAGE = "com.google.zxing.client.android";
    /**
     * Package name for Barcode Scanner+.
     */
    private static final String BSPLUS_PACKAGE = "com.srowen.bs.android";

    // supported barcode formats
    /**
     * Set of product-code barcode formats, for use with
     * {@link #initiateScan(Collection)}.
     */
    public static final Collection<String> PRODUCT_CODE_TYPES = list("UPC_A",
            "UPC_E", "EAN_8", "EAN_13", "RSS_14");
    /**
     * Set of all 1-D barcode formats, for use with
     * {@link #initiateScan(Collection)}.
     */
    public static final Collection<String> ONE_D_CODE_TYPES = list("UPC_A",
            "UPC_E", "EAN_8", "EAN_13", "CODE_39", "CODE_93", "CODE_128",
            "ITF", "RSS_14", "RSS_EXPANDED");
    /**
     * Just the QR code format, for use with {@link #initiateScan(Collection)}.
     */
    public static final Collection<String> QR_CODE_TYPES = Collections
            .singleton("QR_CODE");
    /**
     * Just the Data Matrix format, for use with
     * {@link #initiateScan(Collection)}.
     */
    public static final Collection<String> DATA_MATRIX_TYPES = Collections
            .singleton("DATA_MATRIX");

    /**
     * Set of all barcode formats, for use with
     * {@link #initiateScan(Collection)}.
     */
    public static final Collection<String> ALL_CODE_TYPES = null;

    /**
     * Just the Barcode Scanner application.
     */
    public static final List<String> TARGET_BARCODE_SCANNER_ONLY = Collections
            .singletonList(BS_PACKAGE);
    /**
     * Set of all applications know to support the SCAN {@link Intent}.
     */
    public static final List<String> TARGET_ALL_KNOWN = list(BS_PACKAGE, // Barcode
                                                                         // Scanner
            BSPLUS_PACKAGE, // Barcode Scanner+
            BSPLUS_PACKAGE + ".simple" // Barcode Scanner+ Simple
                    // What else supports this intent?
    );

    /**
     * Default capacity for the extra-parameter {@link Map}.
     */
    private static final int DEFAULT_EXTRAS_CAPACITY = 3;

    /**
     * The activity that invoked the {@link Intent} (and may expect a response).
     */
    private final Activity activity;
    /**
     * Title to use in the download prompt.
     */
    private String title;
    /**
     * Message to use in the download prompt.
     */
    private String message;
    /**
     * Button text to start the download.
     */
    private String buttonYes;
    /**
     * Button text to cancel.
     */
    private String buttonNo;
    /**
     * List of applications allowed to respond to the SCAN {@link Intent}.
     */
    private List<String> targetApplications;
    /**
     * Additional parameters to attach to the SCAN {@link Intent}.
     */
    private final Map<String, Object> moreExtras;

    /**
     * Create an IntentIntegrator, and tie it to an Activity.
     * 
     * @param parentActivity
     *            the Activity that is generating SCAN {@link Intent}s.
     */
    public IntentIntegrator(final Activity parentActivity) {
        activity = parentActivity;
        setTitleByID(R.string.install_scanner_prompt);
        setMessageByID(R.string.install_scanner_detail);
        setButtonYesByID(android.R.string.yes);
        setButtonNoByID(android.R.string.no);
        targetApplications = TARGET_ALL_KNOWN;
        moreExtras = new HashMap<String, Object>(DEFAULT_EXTRAS_CAPACITY);
    }

    /**
     * Get the title to be used in the download prompt.
     * 
     * @return the title to be used in the download prompt
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set the title to be used in the download prompt.
     * 
     * @param titleText
     *            the title to be used in the download prompt
     */
    public void setTitle(final String titleText) {
        title = titleText;
    }

    /**
     * Set the title to be used in the download prompt.
     * 
     * @param titleID
     *            the string resource to use as the title for the download
     *            prompt
     */
    public void setTitleByID(final int titleID) {
        title = activity.getString(titleID);
    }

    /**
     * Get the message to use in the download prompt.
     * 
     * @return the message to use in the download prompt
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set the message to use in the download prompt.
     * 
     * @param messageText
     *            the message to use in the download prompt
     */
    public void setMessage(final String messageText) {
        message = messageText;
    }

    /**
     * Set the message to use in the download prompt.
     * 
     * @param messageID
     *            the string resource to use as the message in the download
     *            prompt
     */
    public void setMessageByID(final int messageID) {
        message = activity.getString(messageID);
    }

    /**
     * Get the message to use when confirming the download request.
     * 
     * @return the message to use when confirming the download request
     */
    public String getButtonYes() {
        return buttonYes;
    }

    /**
     * Set the message to use when confirming the download request.
     * 
     * @param buttonYesText
     *            the message to use when confirming the download request
     */
    public void setButtonYes(final String buttonYesText) {
        buttonYes = buttonYesText;
    }

    /**
     * Set the message to use when confirming the download request.
     * 
     * @param buttonYesID
     *            the string resource to use as the message when confirming the
     *            download request
     */
    public void setButtonYesByID(final int buttonYesID) {
        buttonYes = activity.getString(buttonYesID);
    }

    /**
     * Get the message to use when rejecting the download request.
     * 
     * @return the message to use when rejecting the download request
     */
    public String getButtonNo() {
        return buttonNo;
    }

    /**
     * Set the message to use when rejecting the download request.
     * 
     * @param buttonNoText
     *            the message to use when rejecting the download request
     */
    public void setButtonNo(final String buttonNoText) {
        buttonNo = buttonNoText;
    }

    /**
     * Set the message to use when rejecting the download request.
     * 
     * @param buttonNoID
     *            the string resource to use as the message when rejecting the
     *            download request
     */
    public void setButtonNoByID(final int buttonNoID) {
        buttonNo = activity.getString(buttonNoID);
    }

    /**
     * Get the list of applications allowed to respond to the SCAN
     * {@link Intent}.
     * 
     * @return a list of applications
     */
    public Collection<String> getTargetApplications() {
        return targetApplications;
    }

    /**
     * Set the list of applications allowed to respond to the SCAN
     * {@link Intent}.
     * 
     * @param allowedTargetApplications
     *            a list of applications
     */
    public void setTargetApplications(
            final List<String> allowedTargetApplications) {
        if (targetApplications.isEmpty()) {
            throw new IllegalArgumentException("No target applications");
        }
        targetApplications = allowedTargetApplications;
    }

    /**
     * Set the single application allowed to respond to the SCAN {@link Intent}.
     * 
     * @param allowedTargetApplication
     *            the application to allow
     */
    public void setSingleTargetApplication(final String allowedTargetApplication) {
        targetApplications = Collections
                .singletonList(allowedTargetApplication);
    }

    /**
     * Get extra arguments to be passed to the application handling the SCAN
     * {@link Intent}.
     * 
     * @return a Map of key-value pairs
     */
    public Map<String, ?> getMoreExtras() {
        return moreExtras;
    }

    /**
     * Set and extra argument to be passed to the application handling the SCAN
     * {@link Intent}.
     * 
     * @param key
     *            the name of the extra argument
     * @param value
     *            the value of the extra argument
     */
    public void addExtra(final String key, final Object value) {
        moreExtras.put(key, value);
    }

    /**
     * Initiates a scan for all known barcode types.
     * 
     * @return the {@link AlertDialog} that was shown to the user prompting them
     *         to download the app if a prompt was needed, or null otherwise
     */
    public AlertDialog initiateScan() {
        return initiateScan(ALL_CODE_TYPES);
    }

    /**
     * Initiates a scan only for a certain set of barcode types, given as
     * strings corresponding to their names in ZXing's {@code BarcodeFormat}
     * class like "UPC_A". You can supply constants like
     * {@link #PRODUCT_CODE_TYPES} for example.
     * 
     * @param desiredBarcodeFormats
     *            the barcode formats that may be scanned
     * @return the {@link AlertDialog} that was shown to the user prompting them
     *         to download the app if a prompt was needed, or null otherwise
     */
    public AlertDialog initiateScan(
            final Collection<String> desiredBarcodeFormats) {
        Intent intentScan = new Intent(BS_PACKAGE + ".SCAN");
        intentScan.addCategory(Intent.CATEGORY_DEFAULT);

        // check which types of codes to scan for
        if (desiredBarcodeFormats != null) {
            // set the desired barcode types
            StringBuilder joinedByComma = new StringBuilder();
            for (String format : desiredBarcodeFormats) {
                if (joinedByComma.length() > 0) {
                    joinedByComma.append(',');
                }
                joinedByComma.append(format);
            }
            intentScan.putExtra("SCAN_FORMATS", joinedByComma.toString());
        }

        String targetAppPackage = findTargetAppPackage(intentScan);
        if (targetAppPackage == null) {
            return showDownloadDialog();
        }
        intentScan.setPackage(targetAppPackage);
        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        attachMoreExtras(intentScan);
        startActivityForResult(intentScan, REQUEST_CODE);
        return null;
    }

    /**
     * Start an activity.<br>
     * This method is defined to allow different methods of activity starting
     * for newer versions of Android and for compatibility library.
     * 
     * @param intent
     *            Intent to start.
     * @param code
     *            Request code for the activity
     * @see android.app.Activity#startActivityForResult(Intent, int)
     * @see android.app.Fragment#startActivityForResult(Intent, int)
     */
    protected void startActivityForResult(final Intent intent, final int code) {
        activity.startActivityForResult(intent, code);
    }

    /**
     * Loop through the list of applications that can respond to the given
     * {@link Intent}, looking for one that's also in
     * {@link #targetApplications}.
     * 
     * @param intent
     *            the Intent
     * @return a package name from {@link #targetApplications}, or null
     */
    private String findTargetAppPackage(final Intent intent) {
        PackageManager pm = activity.getPackageManager();
        List<ResolveInfo> availableApps = pm.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (availableApps != null) {
            for (ResolveInfo availableApp : availableApps) {
                String packageName = availableApp.activityInfo.packageName;
                if (targetApplications.contains(packageName)) {
                    return packageName;
                }
            }
        }
        return null;
    }

    /**
     * Show the download prompt.
     * 
     * @return the AlertDialog that is shown
     */
    private AlertDialog showDownloadDialog() {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface,
                            final int i) {
                        String packageName = targetApplications.get(0);
                        Uri uri = Uri.parse("market://details?id="
                                + packageName);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        try {
                            activity.startActivity(intent);
                        } catch (ActivityNotFoundException anfe) {
                            // Hmm, market is not installed
                            Log.w(this.getClass().getSimpleName(),
                                    "Google Play is not installed; cannot install "
                                            + packageName);
                        }
                    }
                });
        downloadDialog.setNegativeButton(buttonNo,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface,
                            final int i) {
                    }
                });
        return downloadDialog.show();
    }

    /**
     * <p>
     * Call this from your {@link Activity}'s
     * {@link Activity#onActivityResult(int, int, Intent)} method.
     * </p>
     * 
     * @param requestCode
     *            the request code returned by the activity
     * @param resultCode
     *            the result code returned from the activity
     * @param intent
     *            the data returned from the activity
     * @return null if the event handled here was not related to this class, or
     *         else an {@link IntentResult} containing the result of the scan.
     *         If the user cancelled scanning, the fields will be null.
     */
    public static IntentResult parseActivityResult(final int requestCode,
            final int resultCode, final Intent intent) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String formatName = intent.getStringExtra("SCAN_RESULT_FORMAT");
                byte[] rawBytes = intent.getByteArrayExtra("SCAN_RESULT_BYTES");
                int intentOrientation = intent.getIntExtra(
                        "SCAN_RESULT_ORIENTATION", Integer.MIN_VALUE);
                Integer orientation;
                if (intentOrientation == Integer.MIN_VALUE) {
                    orientation = null;
                } else {
                    orientation = intentOrientation;
                }
                String errorCorrectionLevel = intent
                        .getStringExtra("SCAN_RESULT_ERROR_CORRECTION_LEVEL");
                return new IntentResult(contents, formatName, rawBytes,
                        orientation, errorCorrectionLevel);
            }
            return new IntentResult();
        }
        return null;
    }

    /**
     * Defaults to type "TEXT_TYPE".
     * 
     * @param format
     *            the barcode format to use
     * @param text
     *            the text string to encode as a barcode
     * @return the {@link AlertDialog} that was shown to the user prompting them
     *         to download the app if a prompt was needed, or null otherwise
     * @see #shareText(CharSequence, CharSequence)
     */
    public AlertDialog shareText(final CharSequence format,
            final CharSequence text) {
        return shareText(format, text, "TEXT_TYPE");
    }

    /**
     * List of valid start/end characters for CodaBar barcodes.
     */
    private static final char[] VALID_START_END_CHARS = { 'A', 'T', 'B', 'N',
            'C', '*', 'D', 'E' };

    /**
     * Check whether an array contains the given character.
     * 
     * @param array
     *            the array to check
     * @param key
     *            the character to look for
     * @return true if <code>array</code> contains <code>key</code>; false
     *         otherwise
     */
    private static boolean arrayContains(final char[] array, final char key) {
        if (array != null) {
            for (char c : array) {
                if (c == key) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Shares the given text by encoding it as a barcode, such that another user
     * can scan the text off the screen of the device.
     * 
     * @param format
     *            the barcode format to use
     * @param text
     *            the text string to encode as a barcode
     * @param type
     *            type of data to encode. See
     *            {@code com.google.zxing.client.android.Contents.Type}
     *            constants.
     * @return the {@link AlertDialog} that was shown to the user prompting them
     *         to download the app if a prompt was needed, or null otherwise
     */
    public AlertDialog shareText(final CharSequence format,
            final CharSequence text, final CharSequence type) {
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setAction(BS_PACKAGE + ".ENCODE");
        String data = (String) text;
        if ("CODABAR".equals(format)) {
            // CodaBar decoder doesn't include the start/end
            // characters (as of 4.3.2), so assume library
            // (start with A, end with B).
            if (!arrayContains(VALID_START_END_CHARS, data.charAt(0))) {
                data = "A" + data;
            }
            // The *encoder* doesn't recognize A, B, C, or D
            // as valid ending chars, so use alternate T, N,
            // *, E characters (which it *does* recognize).
            if (!arrayContains(VALID_START_END_CHARS,
                    data.charAt(data.length() - 1))) {
                data = data + "N";
            }
        }
        intent.putExtra("ENCODE_FORMAT", format);
        intent.putExtra("ENCODE_TYPE", type);
        intent.putExtra("ENCODE_DATA", data);
        intent.putExtra("ENCODE_SHOW_CONTENTS", false);
        String targetAppPackage = findTargetAppPackage(intent);
        if (targetAppPackage == null) {
            return showDownloadDialog();
        }
        intent.setPackage(targetAppPackage);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        attachMoreExtras(intent);
        activity.startActivity(intent);
        return null;
    }

    /**
     * Helper function to take a set of arguments and turn it into an
     * unmodifiable list.
     * 
     * @param values
     *            the items for the list
     * @return the unmodifiable list
     */
    private static List<String> list(final String... values) {
        return Collections.unmodifiableList(Arrays.asList(values));
    }

    /**
     * Loop through additional arguments added via
     * {@link #addExtra(String, Object)} and add them to the SCAN {@link Intent}
     * .
     * 
     * @param intent
     *            the SCAN Intent
     */
    private void attachMoreExtras(final Intent intent) {
        for (Map.Entry<String, Object> entry : moreExtras.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            // Kind of hacky
            if (value instanceof Integer) {
                intent.putExtra(key, (Integer) value);
            } else if (value instanceof Long) {
                intent.putExtra(key, (Long) value);
            } else if (value instanceof Boolean) {
                intent.putExtra(key, (Boolean) value);
            } else if (value instanceof Double) {
                intent.putExtra(key, (Double) value);
            } else if (value instanceof Float) {
                intent.putExtra(key, (Float) value);
            } else if (value instanceof Bundle) {
                intent.putExtra(key, (Bundle) value);
            } else {
                intent.putExtra(key, value.toString());
            }
        }
    }

}

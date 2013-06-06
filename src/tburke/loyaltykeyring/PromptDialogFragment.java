package tburke.loyaltykeyring;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

/**
 * Simple dialog to prompt the user for a string and return the result.
 * 
 * @author tburke
 */
public class PromptDialogFragment extends DialogFragment {
    /**
     * Key for the request code to be passed back (OPTIONAL).
     */
    public static final String DIALOG_REQUEST_CODE = "DIALOG_REQUEST_CODE";
    /**
     * Key for the title string reference to be displayed (REQUIRED).
     */
    public static final String DIALOG_TITLE = "DIALOG_TITLE";
    /**
     * Key for the prompt string reference to be displayed (REQUIRED).
     */
    public static final String DIALOG_PROMPT = "DIALOG_PROMPT";
    /**
     * Key for the default string to be used (OPTIONAL).
     */
    public static final String DIALOG_DEFAULT = "DIALOG_DEFAULT";

    /**
     * Event listener to be used with PromptDialogFragment.
     */
    public interface Listener {
        /**
         * Event to be called when the dialog closes.
         * 
         * @param requestCode
         *            the request code, as provided via
         *            {@link PromptDialogFragment#DIALOG_REQUEST_CODE}
         * @param input
         *            the string the user entered, or null if the user canceled
         */
        void onResponse(int requestCode, String input);
    }

    /**
     * The event listener to receive the user's response.
     */
    private Listener resultListener = null;
    /**
     * The request code to be passed back.
     */
    private int code;
    /**
     * The identifier for the string resource to be used as the dialog title.
     */
    private int title;
    /**
     * The identifier for the string resource to be used as the dialog prompt.
     */
    private int prompt;
    /**
     * The string that should be pre-loaded in the dialog (for example, for
     * editing).
     */
    private String defaultValue;
    /**
     * The user's response.
     */
    private String result = null;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);

        title = getArguments().getInt(DIALOG_TITLE);
        if (title == 0) {
            throw new IllegalArgumentException("Argument required:"
                    + DIALOG_TITLE);
        }

        prompt = getArguments().getInt(DIALOG_PROMPT);
        if (prompt == 0) {
            throw new IllegalArgumentException("Argument required:"
                    + DIALOG_PROMPT);
        }

        code = getArguments().getInt(DIALOG_REQUEST_CODE, 0);
        defaultValue = getArguments().getString(DIALOG_DEFAULT);

        if (resultListener == null) {
            try {
                resultListener = (Listener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement PromptDialogFragment.Listener");
            }
        }
    }

    /**
     * Set the event listener.
     * 
     * @param handler the new event listener.
     */
    public void setHandler(final Listener handler) {
        resultListener = handler;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final DialogFragment self = this;
        final EditText input = new EditText(getActivity());
        input.setLines(1);
        input.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(final View view, final int keyCode,
                    final KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    result = input.getText().toString();
                    self.getDialog().dismiss();
                    return true;
                }
                return false;
            }
        });
        input.setText(defaultValue);

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(prompt)
                .setView(input)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int which) {
                                result = input.getText().toString();
                            }
                        }).setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Override
    public void onStop() {
        super.onStop();
        resultListener.onResponse(code, result);
    }
}

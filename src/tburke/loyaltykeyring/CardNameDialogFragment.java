package tburke.loyaltykeyring;

import android.app.Activity;

/**
 * Dialog to get names for {@link LoyaltyCard}s.
 * 
 * @author tburke
 */
public final class CardNameDialogFragment extends PromptDialogFragment
        implements PromptDialogFragment.Listener {
    /**
     * Key to be used when including the (required) barcode format via
     * Bundle.putString.
     */
    public static final String BARCODE_FORMAT = "BARCODE_FORMAT";

    /**
     * Key to be used when including the (required) barcode data via
     * Bundle.putString.
     */
    public static final String BARCODE_DATA = "BARCODE_DATA";

    /**
     * Event listener to be used with CardNameDialogFragment.
     */
    public interface Listener {
        /**
         * Event called when user enters a valid name.
         * 
         * @param newCard
         *            the complete {@link LoyaltyCard}, with the new name.
         */
        void onAddCard(LoyaltyCard newCard);

        /**
         * Event called when the user cancels the dialog, or provides and empty
         * name.
         */
        void onAddCardCancel();
    }

    /**
     * The event listener for the current session.
     */
    private Listener resultListener = null;
    /**
     * The barcode format, as supplied by caller.
     */
    private String barcodeFormat;
    /**
     * The barcode data, as supplied by caller.
     */
    private String barcodeData;
    /**
     * The name returned from {@link PromptDialogFragment}.
     */
    private String name;

    @Override
    public void onAttach(final Activity activity) {
        getArguments().putInt(PromptDialogFragment.DIALOG_TITLE,
                R.string.new_card_label);
        getArguments().putInt(PromptDialogFragment.DIALOG_PROMPT,
                R.string.new_card_prompt);
        super.setHandler(this);
        super.onAttach(activity);

        barcodeFormat = getArguments().getString(BARCODE_FORMAT);
        if (barcodeFormat == null) {
            throw new IllegalArgumentException("Argument required:"
                    + BARCODE_FORMAT);
        }

        barcodeData = getArguments().getString(BARCODE_DATA);
        if (barcodeData == null) {
            throw new IllegalArgumentException("Argument required:"
                    + BARCODE_DATA);
        }

        if (resultListener == null) {
            try {
                resultListener = (Listener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement AddCardDialogFragment.Listener");
            }
        }
    }

    /**
     * Set the event listener.
     * 
     * @param handler
     *            the new event listener.
     */
    public void setHandler(final Listener handler) {
        resultListener = handler;
    }

    @Override
    public void onStop() {
        super.onStop(); // which triggers onResponse, setting name...
        if (name == null || "".equals(name)) {
            resultListener.onAddCardCancel();
        } else {
            LoyaltyCard card = new LoyaltyCard(name, barcodeFormat, barcodeData);
            resultListener.onAddCard(card);
        }
    }

    @Override
    public void onResponse(final int requestCode, final String input) {
        name = input;
    }
}

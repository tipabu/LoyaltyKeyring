package tburke.loyaltykeyring;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data object representing a loyalty card.
 * 
 * @author tburke
 */
public final class LoyaltyCard implements Serializable {
    /**
     * Determines if a de-serialized file is compatible with this class.
     */
    private static final long serialVersionUID = -5095650763727350079L;
    /**
     * The (user-supplied) display name for the card.
     * 
     * @serial
     */
    private final String name;
    /**
     * The barcode format used by the card.
     * 
     * @serial
     */
    private final String format;
    /**
     * The data stored in the barcode.
     * 
     * @serial
     */
    private final String data;

    /**
     * Create a new, immutable LoyaltyCard.
     * 
     * @param cardName
     *            the (user-supplied) display name for the card
     * @param barcodeFormat
     *            the barcode format used by the card
     * @param barcodeData
     *            the data stored in the barcode
     */
    LoyaltyCard(final String cardName, final String barcodeFormat,
            final String barcodeData) {
        name = cardName;
        format = barcodeFormat;
        data = barcodeData;
    }

    /**
     * Getter for the card's display name.
     * 
     * @return the display name for the card
     */
    public String getName() {
        return name;
    }

    /**
     * Getter for the card's barcode format.
     * 
     * @return the barcode format used by the card
     */
    public String getFormat() {
        return format;
    }

    /**
     * Getter for the card's barcode data.
     * 
     * @return the data stored in the card's barcode
     */
    public String getData() {
        return data;
    }

    /**
     * Getter for a unique identifier for the card.
     * 
     * @return a combination of the card's format and data
     * 
     * @see LoyaltyCard#createID(String, String)
     */
    public String getID() {
        return createID(format, data);
    }

    /**
     * Create a unique identifier for a card.
     * 
     * @param format
     *            the barcode format used by the card
     * @param data
     *            the data stored in the barcode
     * @return a combination of the card's format and data
     * 
     * @see #getFormatFromID(String)
     * @see #getDataFromID(String)
     */
    public static String createID(final String format, final String data) {
        return format + ":" + data;
    }

    /**
     * RegEx corresponding to the format used in
     * {@link LoyaltyCard#createID(String, String)}. Contains two groups:
     * <ol>
     * <li>The barcode format</li>
     * <li>The barcode data</li>
     * </ol>
     * 
     * @see #getFormatFromID(String)
     * @see #getDataFromID(String)
     */
    private static final Pattern ID_PATTERN = Pattern.compile("([^:]*):(.*)");

    /**
     * Get the barcode format used, based on the loyalty card ID.
     * 
     * @param id
     *            the loyalty card ID
     * @return the barcode format
     * 
     * @see #createID(String, String)
     * @see #ID_PATTERN
     * @see #getDataFromID(String)
     */
    public static String getFormatFromID(final String id) {
        Matcher m = ID_PATTERN.matcher(id);
        if (m.matches()) {
            return m.group(1);
        } else {
            Log.wtf("LoyaltyCard:getFormatFromID",
                    "Couldn't parse format/data from '" + id
                            + "' using pattern '" + ID_PATTERN.pattern() + "'");
            return null;
        }
    }

    /**
     * Get the barcode data stored, based on the loyalty card ID.
     * 
     * @param id
     *            the loyalty card ID
     * @return the barcode data
     * 
     * @see #createID(String, String)
     * @see #ID_PATTERN
     * @see #getFormatFromID(String)
     */
    public static String getDataFromID(final String id) {
        Matcher m = ID_PATTERN.matcher(id);
        if (m.matches()) {
            return m.group(2);
        } else {
            Log.wtf("LoyaltyCard:getDataFromID",
                    "Couldn't parse format/data from '" + id
                            + "' using pattern '" + ID_PATTERN.pattern() + "'");
            return null;
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof LoyaltyCard)) {
            return false;
        } else {
            LoyaltyCard c = (LoyaltyCard) o;
            return c.getData().equals(getData())
                    && c.getFormat().equals(getFormat())
                    && c.getName().equals(getName());
        }
    }

    /**
     * Seed value used when computing {@link #hashCode()}.
     */
    private static final int HASH_CODE_SEED = 1;
    /**
     * Multiplicand used when computing {@link #hashCode()}.
     */
    private static final int HASH_CODE_MULT = 37;

    @Override
    public int hashCode() {
        int result = HASH_CODE_SEED;
        result = result * HASH_CODE_MULT + data.hashCode();
        result = result * HASH_CODE_MULT + format.hashCode();
        result = result * HASH_CODE_MULT + name.hashCode();
        return result;
    }

    /**
     * Deserialize a loyalty card that was serialized with
     * {@link #writeObject(ObjectOutputStream)}.
     * 
     * @param inputStream the stream from which to read serialized data
     * @throws ClassNotFoundException
     *             if the data in <code>inputStream</code> was not serialized
     *             with {@link #writeObject(ObjectOutputStream)}
     * @throws IOException
     *             if there was an error reading from <code>inputStream</code>
     */
    private void readObject(final ObjectInputStream inputStream)
            throws ClassNotFoundException, IOException {
        inputStream.defaultReadObject();
    }

    /**
     * Serialize a loyalty card to be later deserialized with
     * {@link #readObject(ObjectInputStream)}.
     * 
     * @param outputStream the stream to which to write serialized data
     * @throws IOException
     *             if there was an error writing to <code>outputStream</code>
     */
    private void writeObject(final ObjectOutputStream outputStream)
            throws IOException {
        // perform the default serialization for all non-transient, non-static
        // fields
        outputStream.defaultWriteObject();
    }
}

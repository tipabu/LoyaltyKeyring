package tburke.loyaltykeyring;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide a method of saving information about {@link LoyaltyCard}s.
 * 
 * @author tburke
 */
class DBHelper extends SQLiteOpenHelper {
    /**
     * Tag to be used when logging.
     */
    private static final String LOG_TAG = DBHelper.class.getSimpleName();
    /**
     * The database name.
     */
    private static final String DB_NAME = "LoyaltyKeyring";
    /**
     * The database version.
     */
    private static final int DB_VERSION = 2;
    /**
     * The table used to store card data.
     */
    private static final String TABLE_CARDS = "LoyaltyCards";
    /**
     * The table used to group cards.
     */
    private static final String TABLE_TAGS = "LoyaltyCardTags";
    /**
     * SQL to create the table used to store card data.
     */
    private static final String DB_CREATE_CARDS = "CREATE TABLE " + TABLE_CARDS
            + " (ID TEXT PRIMARY KEY, Name TEXT NOT NULL UNIQUE);";
    /**
     * SQL to create the table used to group cards.
     */
    private static final String DB_CREATE_TAGS = "CREATE TABLE "
            + TABLE_TAGS
            + " (CardID TEXT NOT NULL, Tag TEXT NOT NULL, FOREIGN KEY (CardID) REFERENCES LoyaltyCards (ID) ON DELETE CASCADE, UNIQUE (CardID, Tag));";

    /**
     * The columns to be returned when searching for cards.
     */
    private static final String[] CARD_COLS = { "ID", "Name" };
    /**
     * The columns to be returned when searching for groups.
     */
    private static final String[] TAG_COLS = new String[] { "Tag", };

    /**
     * Create a new database connection.
     * 
     * @param ctx
     *            context for the database; usually the calling activity
     */
    public DBHelper(final Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase db) {
        if (Log.isLoggable(LOG_TAG, Log.INFO)) {
            Log.i(LOG_TAG, "Creating table '" + DB_CREATE_CARDS + "'");
        }
        db.execSQL(DB_CREATE_CARDS);
        if (Log.isLoggable(LOG_TAG, Log.INFO)) {
            Log.i(LOG_TAG, "Creating table '" + DB_CREATE_TAGS + "'");
        }
        db.execSQL(DB_CREATE_TAGS);
    }

    /**
     * Add a card to the database.
     * 
     * @param newCard
     *            the {@link LoyaltyCard} to be added
     * @return true if the card was added; false otherwise
     */
    public boolean addCard(final LoyaltyCard newCard) {
        if (newCard == null) {
            return false;
        }
        return addCard(newCard.getName(), newCard.getFormat(),
                newCard.getData());
    }

    /**
     * Add a card to the database.
     * 
     * @param name
     *            the card name
     * @param format
     *            the barcode format used by the card
     * @param data
     *            the data stored in the barcode
     * @return true if the card was added; false otherwise
     */
    public boolean addCard(final String name, final String format,
            final String data) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        String id = LoyaltyCard.createID(format, data);
        values.put("ID", id);
        values.put("Name", name);
        if (Log.isLoggable(LOG_TAG, Log.INFO)) {
            Log.i(LOG_TAG, "Creating card: " + id + " (" + name + ")");
        }
        boolean created = db.insert(TABLE_CARDS, null, values) != -1;
        db.close();
        return created;
    }

    /**
     * Remove a card from the database.
     * 
     * @param card
     *            the card to remove
     * @return true if the card was removed, false otherwise (for example, the
     *         card wasn't present)
     */
    public boolean deleteCard(final LoyaltyCard card) {
        if (card == null) {
            return false;
        }
        return deleteCard(card.getFormat(), card.getData());
    }

    /**
     * Remove the specified card from the database.
     * 
     * @param format
     *            the barcode format for the card being specified
     * @param data
     *            the data stored on the card being specified
     * @return true if the card was removed, false otherwise (for example, the
     *         card wasn't present)
     */
    public boolean deleteCard(final String format, final String data) {
        SQLiteDatabase db = getWritableDatabase();
        String id = LoyaltyCard.createID(format, data);
        String[] queryParams = new String[] { id };
        if (Log.isLoggable(LOG_TAG, Log.INFO)) {
            Log.i(LOG_TAG, "Deleting card: " + id);
        }
        boolean deleted = db.delete(TABLE_CARDS, "ID = ?", queryParams) != -1;
        db.close();
        return deleted;
    }

    /**
     * Add the specified card to the specified group; if that is the first card
     * in the group, create the group.
     * 
     * @param card
     *            the card being specified
     * @param tag
     *            the group being specified
     * @return true if the card was added to the group; false otherwise
     */
    public boolean addTag(final LoyaltyCard card, final String tag) {
        return addTag(card.getFormat(), card.getData(), tag);
    }

    /**
     * Add the specified card to the specified group; if that is the first card
     * in the group, create the group.
     * 
     * @param format
     *            the barcode format for the card being specified
     * @param data
     *            the data stored on the card being specified
     * @param tag
     *            the group being specified
     * @return true if the card was added to the group; false otherwise
     */
    public boolean addTag(final String format, final String data,
            final String tag) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("CardID", LoyaltyCard.createID(format, data));
        values.put("Tag", tag);
        boolean created = db.insert(TABLE_TAGS, null, values) != -1;
        db.close();
        return created;
    }

    /**
     * Remove the specified card from the specified group; if that was the last
     * card in the group, remove the group.
     * 
     * @param format
     *            the barcode format for the card being specified
     * @param data
     *            the data stored on the card being specified
     * @param tag
     *            the group being specified
     * @return true if the card was removed from the group; false otherwise
     */
    public boolean removeTag(final String format, final String data,
            final String tag) {
        SQLiteDatabase db = getWritableDatabase();
        String[] queryParams = new String[] {
                LoyaltyCard.createID(format, data), tag };
        boolean deleted = db.delete(TABLE_TAGS, "CardID = ? AND Tag = ?",
                queryParams) != -1;
        db.close();
        return deleted;
    }

    /**
     * Delete the specified group. Cards in that group are unaffected.
     * 
     * @param tag
     *            the group to delete
     * @return true if the group was deleted; false otherwise (ie, group was not
     *         present)
     */
    public boolean deleteTag(final String tag) {
        SQLiteDatabase db = getWritableDatabase();
        String[] queryParams = new String[] { tag };
        if (Log.isLoggable(LOG_TAG, Log.INFO)) {
            Log.i(LOG_TAG, "Deleting tag " + tag);
        }
        boolean deleted = db.delete(TABLE_TAGS, "Tag = ?", queryParams) != -1;
        db.close();
        return deleted;
    }

    /**
     * Get a card by the (user-supplied) name.
     * 
     * @param name
     *            the name to look for
     * @return the card, if found; otherwise null
     */
    public LoyaltyCard getCard(final String name) {
        SQLiteDatabase db = getWritableDatabase();
        String[] queryParams = new String[] { name };
        Cursor c = db.query(TABLE_CARDS, CARD_COLS, "Name = ?", queryParams,
                null, null, null);
        if (!c.moveToFirst()) {
            db.close();
            return null;
        }
        String format = LoyaltyCard.getFormatFromID(c.getString(0));
        String data = LoyaltyCard.getDataFromID(c.getString(0));
        LoyaltyCard result;
        if (data == null || format == null) {
            Log.wtf(LOG_TAG + ":getCard", "Couldn't parse format/data from '"
                    + c.getString(0) + "'");
            result = null;
        } else {
            result = new LoyaltyCard(c.getString(1), format, data);
        }
        db.close();
        return result;
    }

    /**
     * Get all cards currently stored.
     * 
     * @return a list of all cards
     */
    public List<LoyaltyCard> getAllCards() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.query(TABLE_CARDS, CARD_COLS, "1", null, null, null,
                "Name");
        List<LoyaltyCard> result = cursorToList(c);
        db.close();
        return result;
    }

    /**
     * Get all cards in the specified group.
     * 
     * @param tag
     *            the group by which to filter
     * @return a list of all cards in the group
     */
    public List<LoyaltyCard> getCardsByTag(final String tag) {
        if (tag == null || "".equals(tag)) {
            return getAllCards();
        }
        SQLiteDatabase db = getWritableDatabase();
        String[] queryParams = new String[] { tag };
        Cursor c = db.query(TABLE_CARDS + " INNER JOIN " + TABLE_TAGS
                + " ON ID = CardID", CARD_COLS, "Tag = ?", queryParams, null,
                null, "Name");
        List<LoyaltyCard> result = cursorToList(c);
        db.close();
        return result;
    }

    /**
     * Take a Cursor from {@link #getAllCards()} /
     * {@link #getCardsByTag(String)} and turn it into a list of
     * {@link LoyaltyCard}s.
     * 
     * @param c
     *            the database cursor
     * @return a list of LoyaltyCards
     */
    private List<LoyaltyCard> cursorToList(final Cursor c) {
        List<LoyaltyCard> result = new ArrayList<LoyaltyCard>();
        if (!c.moveToFirst()) {
            return result;
        }
        do {
            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG + ":cursorToList",
                        "Parsing card: ID=" + c.getString(0) + "; Name="
                                + c.getString(1));
            }

            String format = LoyaltyCard.getFormatFromID(c.getString(0));
            String data = LoyaltyCard.getDataFromID(c.getString(0));
            if (data == null || format == null) {
                Log.wtf(LOG_TAG + ":getAllCards",
                        "Couldn't parse format/data from '" + c.getString(0)
                                + "'");
            } else {
                result.add(new LoyaltyCard(c.getString(1), format, data));
            }
        } while (c.moveToNext());
        return result;
    }

    /**
     * Get all tags currently in use.
     * 
     * @return a list of all tags in use
     */
    public List<String> getAllGroups() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.query(true, TABLE_TAGS, TAG_COLS, "1", null, null, null,
                "Tag", null);
        List<String> result = new ArrayList<String>();
        if (!c.moveToFirst()) {
            db.close();
            return result;
        }
        do {
            if (Log.isLoggable(LOG_TAG, Log.VERBOSE)) {
                Log.v(LOG_TAG + ":getAllGroups",
                        "Found group: " + c.getString(0));
            }
            result.add(c.getString(0));
        } while (c.moveToNext());
        db.close();
        return result;
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
            final int newVersion) {
        // TODO: Let's not just hose the data
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARDS
                + ";DROP TABLE IF EXISTS " + TABLE_TAGS + ";");

        // Create tables again
        onCreate(db);
    }
}

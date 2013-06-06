package tburke.loyaltykeyring;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.List;

/**
 * Activity to display and manage {@link LoyaltyCard}s.
 * 
 * @author tburke
 */
public final class MainActivity extends FragmentActivity implements
        AdapterView.OnItemClickListener, View.OnClickListener,
        CardNameDialogFragment.Listener, PromptDialogFragment.Listener,
        AdapterView.OnItemSelectedListener {
    /**
     * Request code to use when selecting cards for a grouping.
     */
    private static final int SELECT_CARDS_REQUEST_CODE = 0x9234;
    /**
     * Request code to use when renaming a card.
     */
    private static final int RENAME_CARD_REQUEST_CODE = 0x9235;
    /**
     * Request code to use when creating a new grouping.
     */
    private static final int CREATE_GROUP_REQUEST_CODE = 0x9236;
    /**
     * Request code to use when renaming a group.
     */
    private static final int RENAME_GROUP_REQUEST_CODE = 0x9237;
    /**
     * Provides access to the ZXing barcode scanner/encoder.
     */
    private IntentIntegrator zxing;
    /**
     * Provides access to persistent storage.
     */
    private DBHelper db;
    /**
     * The card currently being renamed.
     */
    private LoyaltyCard cardToBeRenamed = null;
    /**
     * The group currently being renamed.
     */
    private String groupToBeRenamed = null;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DBHelper(this);
        zxing = new IntentIntegrator(this);

        final ListView cardList = (ListView) findViewById(R.id.card_list);
        cardList.setOnItemClickListener(this);
        registerForContextMenu(cardList);

        final Spinner tagSelect = (Spinner) findViewById(R.id.tag_select);
        tagSelect.setOnItemSelectedListener(this);
        registerForContextMenu(tagSelect);

        ((Button) findViewById(R.id.button_add)).setOnClickListener(this);
    }

    /**
     * Refresh the list of {@link LoyaltyCard}s for the current group.
     */
    private void refreshCards() {
        final ListView cardList = (ListView) findViewById(R.id.card_list);
        final Spinner tagList = (Spinner) findViewById(R.id.tag_select);
        String tag = (String) tagList.getSelectedItem();
        Log.i("MainActivity:refreshCards", "Loading cards with tag: " + tag);
        if (getString(R.string.all_cards_label).equals(tag)) {
            tag = null;
        }
        ArrayAdapter<LoyaltyCard> adapter = new ArrayAdapter<LoyaltyCard>(this,
                android.R.layout.simple_list_item_1, db.getCardsByTag(tag));
        cardList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * Refresh the list of available groups.
     */
    private void refreshGroups() {
        refreshGroups(null);
    }

    /**
     * Refresh the list of available groups.
     * 
     * @param preferred
     *            the preferred group to have selected
     */
    private void refreshGroups(final String preferred) {
        final Spinner groupSelect = (Spinner) findViewById(R.id.tag_select);
        String selected = preferred;
        if (selected == null) {
            selected = (String) groupSelect.getSelectedItem();
        }
        if ("All".equals(selected)) {
            selected = null;
        }
        List<String> tagList = db.getAllGroups();
        tagList.add(0, getString(R.string.all_cards_label));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, tagList);
        groupSelect.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        groupSelect.setSelection(tagList.indexOf(selected));
        refreshCards();
    }

    /*
     * @Override protected void onResume() { super.onResume(); refreshGroups();
     * }
     */

    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case SELECT_CARDS_REQUEST_CODE:
            if (resultCode == Activity.RESULT_OK) {
                String[] accounts = data
                        .getStringArrayExtra(AccountSelectActivity.ACCOUNT_LIST);
                String tag = data
                        .getStringExtra(AccountSelectActivity.GROUP_NAME);
                db.deleteTag(tag);
                for (String account : accounts) {
                    if (Log.isLoggable("MainActivity", Log.INFO)) {
                        Log.i("MainActivity", "Adding tag " + tag
                                + " to account " + account);
                    }
                    db.addTag(LoyaltyCard.getFormatFromID(account),
                            LoyaltyCard.getDataFromID(account), tag);
                }
                refreshGroups();
            }
            break;
        default:
            IntentResult res = IntentIntegrator.parseActivityResult(
                    requestCode, resultCode, data);
            if (res != null) {
                Bundle args = new Bundle();
                args.putString(CardNameDialogFragment.BARCODE_FORMAT,
                        res.getFormatName());
                args.putString(CardNameDialogFragment.BARCODE_DATA,
                        res.getContents());
                DialogFragment dialog = new CardNameDialogFragment();
                dialog.setArguments(args);
                dialog.show(getSupportFragmentManager(),
                        "AddCardDialogFragment");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_add_tag:
            Bundle args = new Bundle();
            args.putInt(PromptDialogFragment.DIALOG_TITLE,
                    R.string.new_group_label);
            args.putInt(PromptDialogFragment.DIALOG_PROMPT,
                    R.string.new_group_prompt);
            args.putInt(PromptDialogFragment.DIALOG_REQUEST_CODE,
                    CREATE_GROUP_REQUEST_CODE);
            DialogFragment dialog = new PromptDialogFragment();
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "AddTagDialogFragment");
            return true;
        default:
            return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
            final int position, final long id) {
        LoyaltyCard card = (LoyaltyCard) parent.getAdapter().getItem(position);
        zxing.shareText(card.getFormat(), card.getData());
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.card_list) {
            getMenuInflater().inflate(R.menu.context_menu_card, menu);
        } else if (v.getId() == R.id.tag_select) {
            Spinner groupList = (Spinner) v;
            String group = (String) groupList.getSelectedItem();
            if (!getString(R.string.all_cards_label).equals(group)) {
                getMenuInflater().inflate(R.menu.context_menu_tag, menu);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();

        ListAdapter cardAdapter;
        LoyaltyCard card;
        String group;
        Bundle args;
        DialogFragment dialog;
        switch (item.getItemId()) {
        case R.id.context_card_rename:
            // TODO: is there a way to get View-that-caused-the-context-menu
            // from the MenuItem?
            cardAdapter = ((ListView) findViewById(R.id.card_list))
                    .getAdapter();
            cardToBeRenamed = (LoyaltyCard) cardAdapter.getItem(info.position);

            args = new Bundle();
            args.putInt(PromptDialogFragment.DIALOG_TITLE,
                    R.string.rename_card_label);
            args.putInt(PromptDialogFragment.DIALOG_PROMPT,
                    R.string.new_card_prompt);
            args.putString(PromptDialogFragment.DIALOG_DEFAULT,
                    cardToBeRenamed.getName());
            args.putInt(PromptDialogFragment.DIALOG_REQUEST_CODE,
                    RENAME_CARD_REQUEST_CODE);
            dialog = new PromptDialogFragment();
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "RenameCardDialogFragment");
            return true;
        case R.id.context_card_delete:
            cardAdapter = ((ListView) findViewById(R.id.card_list))
                    .getAdapter();
            card = (LoyaltyCard) cardAdapter.getItem(info.position);
            db.deleteCard(card);
            refreshCards();
            return true;
        case R.id.context_group_edit:
            group = (String) ((Spinner) findViewById(R.id.tag_select))
                    .getSelectedItem();
            editTag(group);
            return true;
        case R.id.context_group_rename:
            groupToBeRenamed = (String) ((Spinner) findViewById(R.id.tag_select))
                    .getSelectedItem();

            args = new Bundle();
            args.putInt(PromptDialogFragment.DIALOG_TITLE,
                    R.string.rename_group_label);
            args.putInt(PromptDialogFragment.DIALOG_PROMPT,
                    R.string.new_group_prompt);
            args.putString(PromptDialogFragment.DIALOG_DEFAULT,
                    groupToBeRenamed);
            args.putInt(PromptDialogFragment.DIALOG_REQUEST_CODE,
                    RENAME_GROUP_REQUEST_CODE);
            dialog = new PromptDialogFragment();
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(),
                    "RenameGroupDialogFragment");
            return true;
        case R.id.context_group_delete:
            group = (String) ((Spinner) findViewById(R.id.tag_select))
                    .getSelectedItem();
            db.deleteTag(group);
            refreshGroups();
            return true;
        default:
            return false;

        }
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
        case R.id.button_add:
            zxing.initiateScan();
            // db.addCard("Card 1", "UPC_A", "789456123");
            // db.addCard("Card 2", "UPC_A", "123456789");
            break;
        default:
            Log.wtf(this.getClass().getSimpleName() + ":onClick",
                    "Unknown view clicked.");
        }
    }

    @Override
    public void onAddCard(final LoyaltyCard newCard) {
        db.addCard(newCard);
        final Spinner tagSelect = (Spinner) findViewById(R.id.tag_select);
        String currentTag = (String) tagSelect.getSelectedItem();
        if (currentTag != null && !"".equals(currentTag)) {
            db.addTag(newCard.getFormat(), newCard.getData(), currentTag);
        }
        cardToBeRenamed = null;
        refreshCards();
    }

    @Override
    public void onAddCardCancel() {
        if (cardToBeRenamed != null) {
            db.addCard(cardToBeRenamed);
            cardToBeRenamed = null;
        }
        refreshCards();
    }

    @Override
    public void onResponse(final int requestCode, final String input) {
        if (input == null || "".equals(input)) {
            return;
        }
        switch (requestCode) {
        case CREATE_GROUP_REQUEST_CODE:
            if (getString(R.string.all_cards_label).equals(input)) {
                displayMessage(getString(R.string.group_name_invalid, input));
            } else {
                editTag(input);
            }
            break;
        case RENAME_CARD_REQUEST_CODE:
            db.deleteCard(cardToBeRenamed);
            db.addCard(input, cardToBeRenamed.getFormat(),
                    cardToBeRenamed.getData());
            refreshCards();
            break;
        case RENAME_GROUP_REQUEST_CODE:
            if (getString(R.string.all_cards_label).equals(input)) {
                displayMessage(getString(R.string.group_name_invalid, input));
            } else {
                List<LoyaltyCard> cards = db.getCardsByTag(groupToBeRenamed);
                db.deleteTag(groupToBeRenamed);
                Log.i("MainActivity", "Adding " + cards.size()
                        + " cards to tag " + input);
                for (LoyaltyCard card : cards) {
                    db.addTag(card, input);
                }
                refreshGroups(input);
            }
            break;
        default:
            Log.wtf(this.getClass().getSimpleName() + ":onResponse",
                    "Unknown request code received.");
        }
    }

    /**
     * Re-do selections for the specified tag.
     * 
     * @param tag
     *            the tag for which we'd like to select {@link LoyaltyCard}s.
     */
    private void editTag(final String tag) {
        Intent intent = new Intent(this, AccountSelectActivity.class);
        intent.putExtra(AccountSelectActivity.GROUP_NAME, tag);
        startActivityForResult(intent, SELECT_CARDS_REQUEST_CODE);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle inState) {
        super.onRestoreInstanceState(inState);

        groupToBeRenamed = inState.getString("groupToBeRenamed");
        cardToBeRenamed = (LoyaltyCard) inState
                .getSerializable("cardToBeRenamed");
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("groupToBeRenamed", groupToBeRenamed);
        outState.putSerializable("cardToBeRenamed", cardToBeRenamed);
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshGroups();
    }

    @Override
    public void onItemSelected(final AdapterView<?> arg0, final View arg1,
            final int arg2, final long arg3) {
        refreshCards();
    }

    @Override
    public void onNothingSelected(final AdapterView<?> arg0) {
        // No-op
    }

    /**
     * Display a message box to the user.
     * 
     * @param msg the message to display
     */
    private void displayMessage(final String msg) {
        new AlertDialog.Builder(this).setMessage(msg)
                .setPositiveButton(android.R.string.ok, null).show();
    }
}

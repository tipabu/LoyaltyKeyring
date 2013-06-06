package tburke.loyaltykeyring;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Android activity to select which {@link LoyaltyCard}s should be associated
 * with a given group.
 * 
 * @author tburke
 * 
 */
public final class AccountSelectActivity extends Activity implements
        View.OnClickListener, AdapterView.OnItemClickListener {
    /**
     * Key to be used with Intent.getStringExtra to get the group being editted.
     */
    public static final String GROUP_NAME = "TAG_NAME";
    /**
     * Key to be used with Intent.getStringArrayExtra to get the
     * {@link LoyaltyCard}s that were selected.
     */
    public static final String ACCOUNT_LIST = "ACCOUNTS";
    /**
     * Helper to get access to the database.
     */
    private DBHelper db;
    /**
     * Adapter to populate the list of {@link LoyaltyCard}s.
     */
    private ArrayAdapter<LoyaltyCard> adapter;
    /**
     * The group being edited.
     */
    private String group;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_accounts);
        db = new DBHelper(this);

        final ListView cardList = (ListView) findViewById(R.id.card_select);
        cardList.setOnItemClickListener(this);
        cardList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        adapter = new ArrayAdapter<LoyaltyCard>(this,
                android.R.layout.simple_list_item_multiple_choice,
                db.getAllCards());
        cardList.setAdapter(adapter);

        group = getIntent().getStringExtra(GROUP_NAME);
        for (LoyaltyCard card : db.getCardsByTag(group)) {
            int pos = adapter.getPosition(card);
            Log.i(this.getClass().getSimpleName(),
                    "Checking card " + card.toString() + " at position " + pos);
            cardList.setItemChecked(pos, true);
        }
        adapter.notifyDataSetChanged();

        ((Button) findViewById(R.id.save_tag)).setOnClickListener(this);
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
        case R.id.save_tag:
            Intent res = new Intent();
            final ListView cardList = (ListView) findViewById(R.id.card_select);
            List<String> accounts = new ArrayList<String>();
            SparseBooleanArray checked = cardList.getCheckedItemPositions();
            if (checked != null) {
                Log.i("AccountSelectActivity:onClick", "Selected items: "
                        + checked.size());

                for (int i = 0; i < checked.size(); ++i) {
                    if (checked.get(checked.keyAt(i))) {
                        Log.i("AccountSelectActivity:onClick", "  Item: "
                                + checked.keyAt(i));
                        accounts.add(adapter.getItem(checked.keyAt(i)).getID());
                    }
                }
            }
            res.putExtra(GROUP_NAME, group);
            res.putExtra(ACCOUNT_LIST, accounts.toArray(new String[] {}));
            setResult(RESULT_OK, res);
            finish();
            break;
        default:
            Log.wtf("AccountSelectDialogFragment:onClick",
                    "Unknown view clicked.");
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
            final int position, final long id) {
        // ((CheckedTextView) view).toggle();
    }

    @Override
    protected void onRestoreInstanceState(final Bundle inState) {
        super.onRestoreInstanceState(inState);
        if (false) {
            final ListView cardList = (ListView) findViewById(R.id.card_select);
            cardList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            boolean[] checked = inState.getBooleanArray("checked");
            for (int i = 0; i < checked.length; ++i) {
                cardList.setItemChecked(i, checked[i]);
                String msg;
                if (checked[i]) {
                    msg = String.format(
                            "Setting item at position %d to checked", i);
                } else {
                    msg = String.format(
                            "Setting item at position %d to not checked", i);
                }
                Log.v("AccountSelectActivity:onRestoreInstanceState", msg);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (false) {
            final ListView cardList = (ListView) findViewById(R.id.card_select);
            SparseBooleanArray sparseChecked = cardList
                    .getCheckedItemPositions();
            boolean[] checked = new boolean[sparseChecked.size()];
            for (int i = 0; i < sparseChecked.size(); ++i) {
                checked[i] = sparseChecked.get(i);
                String msg;
                if (checked[i]) {
                    msg = String.format("Item at position %d is checked", i);
                } else {
                    msg = String
                            .format("Item at position %d is not checked", i);
                }
                Log.v("AccountSelectActivity:onSaveInstanceState", msg);
            }
            outState.putBooleanArray("checked", checked);
        }
    }

}

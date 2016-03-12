package com.prismaqf.callblocker;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.FilterRuleProvider;

import java.util.ArrayList;

/**
 * Base activity for editable list with action bar
 * @author ConteDiMonteCristo
 */
public class EditFilterRules extends ActionBarActivity {

    private class DbOperation extends AsyncTask<SQLiteDatabase, Void ,ArrayList<String>> {


        @Override
        protected ArrayList<String> doInBackground(SQLiteDatabase... dbs) {
            try {
                return FilterRuleProvider.AllRuleNames(dbs[0]);
            }
            finally {
                dbs[0].close();
            }
        }

        @Override
        protected void onPostExecute (ArrayList<String> names) {
            Intent intent = new Intent(EditFilterRules.this, NewEditFilterRule.class);
            intent.putExtra(NewEditActivity.ACTION_KEY,NewEditCalendarRule.ACTION_CREATE);
            intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, names);
            startActivity(intent);
        }

    }

    private final String FRAGMENT = "EditFilterRulesFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.data_bound_edit_activity);


        getFragmentManager().
                beginTransaction().
                setTransition(FragmentTransaction.TRANSIT_ENTER_MASK).
                replace(R.id.list_fragment_holder, new FilterRulesFragment(), FRAGMENT).
                commit();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_list, menu);
        menu.findItem(R.id.action_new_item).setTitle(R.string.mn_new_filter_rule);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_new_item:
                newFilterRule();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void newFilterRule() {
        SQLiteDatabase db = new DbHelper(this).getReadableDatabase();
        (new DbOperation()).execute(db);
    }
}
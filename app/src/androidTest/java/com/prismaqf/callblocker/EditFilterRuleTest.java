package com.prismaqf.callblocker;

import android.Manifest.permission;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.prismaqf.callblocker.rules.FilterRule;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.FilterRuleProvider;
import com.prismaqf.callblocker.utils.DebugDBFileName;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class EditFilterRuleTest {


    private static final String TEST_RULE_NAME = "Test rule";

    @ClassRule
    public static final DebugDBFileName myDebugDB = new DebugDBFileName();

    public final ActivityTestRule<CallBlockerManager> myActivityRule = new ActivityTestRule<>(CallBlockerManager.class);

    public final GrantPermissionRule permissionRule = GrantPermissionRule
          .grant(permission.READ_CONTACTS, permission.CALL_PHONE);

    @Rule
    public final RuleChain chian = RuleChain.outerRule(permissionRule).around(myActivityRule);

    @Before
    public void before() {
        FilterRule fr = new FilterRule(TEST_RULE_NAME, "Rule used for testing");
        fr.addPattern("123");
        fr.addPattern("4*56");
        SQLiteDatabase db = new DbHelper(myActivityRule.getActivity()).getWritableDatabase();
        FilterRuleProvider.InsertRow(db,fr);
        db.close();
    }

    @After
    public void after() {
        SQLiteDatabase db = new DbHelper(myActivityRule.getActivity()).getWritableDatabase();
        FilterRuleProvider.DeleteFilterRule(db, TEST_RULE_NAME);
        db.close();
    }

    @Test
    public void SmokeTest() {
        Intent intent = new Intent(myActivityRule.getActivity(),EditFilterRules.class);
        myActivityRule.getActivity().startActivity(intent);
        //test that the rule is displayed
        onView(ViewMatchers.withId(R.id.text_rule_name)).check(matches(withText(TEST_RULE_NAME)));
        //test that the add action is present
        onView(ViewMatchers.withId(R.id.action_new_item)).check(matches(isDisplayed()));
    }

}

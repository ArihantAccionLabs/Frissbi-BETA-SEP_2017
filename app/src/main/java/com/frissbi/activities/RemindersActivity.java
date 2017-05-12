package com.frissbi.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.adapters.RemindersAdapter;
import com.frissbi.models.FrissbiReminder;

import java.util.List;

public class RemindersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RecyclerView remindersRecyclerView = (RecyclerView) findViewById(R.id.reminders_recyclerView);
        TextView noReminderTv = (TextView) findViewById(R.id.no_reminder_tv);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        remindersRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(remindersRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        remindersRecyclerView.addItemDecoration(dividerItemDecoration);
        List<FrissbiReminder> frissbiReminderList = FrissbiReminder.listAll(FrissbiReminder.class);
        Log.d("FrissbiReminder", "frissbiReminderList" + frissbiReminderList);
        if (frissbiReminderList.size() > 0) {
            noReminderTv.setVisibility(View.GONE);
            RemindersAdapter remindersAdapter = new RemindersAdapter(this, frissbiReminderList);
            remindersRecyclerView.setAdapter(remindersAdapter);
        } else {
            noReminderTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

}

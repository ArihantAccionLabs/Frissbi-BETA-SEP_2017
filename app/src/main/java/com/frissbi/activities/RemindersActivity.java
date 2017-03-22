package com.frissbi.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.frissbi.R;
import com.frissbi.adapters.RemindersAdapter;
import com.frissbi.models.FrissbiReminder;

import java.util.List;

public class RemindersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        RecyclerView remindersRecyclerView = (RecyclerView) findViewById(R.id.reminders_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        remindersRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(remindersRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        remindersRecyclerView.addItemDecoration(dividerItemDecoration);
        List<FrissbiReminder> frissbiReminderList = FrissbiReminder.listAll(FrissbiReminder.class);
        RemindersAdapter remindersAdapter = new RemindersAdapter(this, frissbiReminderList);
        remindersRecyclerView.setAdapter(remindersAdapter);
    }

}

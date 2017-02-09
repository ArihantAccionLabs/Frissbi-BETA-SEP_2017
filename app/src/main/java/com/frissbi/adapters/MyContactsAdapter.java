package com.frissbi.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.frissbi.R;
import com.frissbi.models.MyContacts;

import java.util.List;

/**
 * Created by thrymr on 9/2/17.
 */

public class MyContactsAdapter extends RecyclerView.Adapter<MyContactsAdapter.ViewHolder> {

    private Context mContext;
    private List<MyContacts> mMyContactsList;

    public MyContactsAdapter(Context context, List<MyContacts> myContactsList) {
        mContext = context;
        mMyContactsList = myContactsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.my_contacts_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mMyContactsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private Button inviteButton;
        private TextView myContactsNameTv;
        private TextView myContactsNumTv;

        public ViewHolder(View itemView) {
            super(itemView);
            myContactsNameTv = (TextView) itemView.findViewById(R.id.my_contacts_name);
            myContactsNumTv = (TextView) itemView.findViewById(R.id.my_contacts_num);
            inviteButton = (Button) itemView.findViewById(R.id.invite_button);
        }
    }
}

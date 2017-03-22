package com.frissbi.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.frissbi.Frissbi_img_crop.Util;
import com.frissbi.R;
import com.frissbi.Utility.ConnectionDetector;
import com.frissbi.Utility.CustomProgressDialog;
import com.frissbi.Utility.FLog;
import com.frissbi.Utility.SharedPreferenceHandler;
import com.frissbi.Utility.Utility;
import com.frissbi.activities.GroupDetailsActivity;
import com.frissbi.activities.ProfileActivity;
import com.frissbi.adapters.MeetingLogAdapter;
import com.frissbi.adapters.NotificationLogAdapter;
import com.frissbi.interfaces.NotificationListener;
import com.frissbi.models.Notification;
import com.frissbi.networkhandler.TSNetworkHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class NotificationLogFragment extends Fragment implements NotificationListener {


    private RecyclerView mNotificationLogRecyclerView;
    private List<Notification> mNotificationList;
    private ProgressDialog mProgressDialog;
    private NotificationListener mNotificationListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification_log, container, false);
        mNotificationListener = (NotificationListener) this;
        mProgressDialog = new CustomProgressDialog(getActivity());
        mNotificationList = new ArrayList<>();
        mNotificationLogRecyclerView = (RecyclerView) view.findViewById(R.id.notification_log_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mNotificationLogRecyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mNotificationLogRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mNotificationLogRecyclerView.addItemDecoration(dividerItemDecoration);
        getNoficationsFromServer();
        return view;
    }

    private void getNoficationsFromServer() {
        mProgressDialog.show();
        TSNetworkHandler.getInstance(getActivity()).getResponse(Utility.REST_URI + Utility.NOTIFICATION_LOG + SharedPreferenceHandler.getInstance(getActivity()).getUserId(), new HashMap<String, String>()
                , TSNetworkHandler.TYPE_GET, new TSNetworkHandler.ResponseHandler() {
                    @Override
                    public void handleResponse(TSNetworkHandler.TSResponse response) {
                        if (response != null) {
                            if (response.status == TSNetworkHandler.TSResponse.STATUS_SUCCESS) {
                                try {
                                    mNotificationList.clear();
                                    JSONObject responseJsonObject = new JSONObject(response.response);
                                    JSONArray notificationJsonArray = responseJsonObject.getJSONArray("notificationArray");

                                    for (int i = 0; i < notificationJsonArray.length(); i++) {
                                        JSONObject notificationJsonObject = notificationJsonArray.getJSONObject(i);
                                        Notification notification = new Notification();
                                        notification.setMessage(notificationJsonObject.getString("notificationMessage"));
                                        notification.setType(notificationJsonObject.getString("type"));
                                        if (notificationJsonObject.getString("type").equalsIgnoreCase(Utility.GROUP_NOTIFICATION_TYPE)) {
                                            notification.setGroupId(notificationJsonObject.getLong("groupId"));
                                            notification.setGroupName(notificationJsonObject.getString("groupName"));
                                            notification.setGroupAdmin(notificationJsonObject.getString("adminName"));
                                            notification.setGroupImageId(notificationJsonObject.getString("groupImageId"));
                                        }
                                        if (notificationJsonObject.getString("type").equalsIgnoreCase(Utility.FRIEND_NOTIFICATION_TYPE)) {
                                            notification.setFriendId(notificationJsonObject.getLong("senderUserId"));
                                            notification.setStatus(notificationJsonObject.getString("status"));
                                            notification.setFriendImageId(notificationJsonObject.getString("profileImageId"));
                                            notification.setFriendName(notificationJsonObject.getString("fullName"));
                                        }
                                        mNotificationList.add(notification);
                                    }

                                    NotificationLogAdapter notificationLogAdapter = new NotificationLogAdapter(getActivity(), mNotificationList, mNotificationListener);
                                    mNotificationLogRecyclerView.setAdapter(notificationLogAdapter);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else if (response.status == TSNetworkHandler.TSResponse.STATUS_FAIL) {
                                Toast.makeText(getActivity(), response.message, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                        }
                        mProgressDialog.dismiss();
                    }
                });

    }

    @Override
    public void selectedNotification(Notification notification) {
        FLog.d("NotificationLogFragment", "notification" + notification);
        if (notification.getType().equalsIgnoreCase(Utility.GROUP_NOTIFICATION_TYPE)) {
            Intent intent = new Intent(getActivity(), GroupDetailsActivity.class);
            intent.putExtra("groupId", notification.getGroupId());
            startActivity(intent);
        } else {
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            intent.putExtra("friendUserId", notification.getFriendId());
            intent.putExtra("isFriend", false);
            intent.putExtra("status", notification.getStatus());
            startActivity(intent);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mNotificationList == null) {
                mNotificationList = new ArrayList<>();
            }
            getNoficationsFromServer();
        }
    }


}

package com.frissbi.Notifications;
 import org.json.JSONException;
        import org.json.JSONObject;

        import android.os.Bundle;
        import android.app.Activity;
        import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
        import android.widget.TextView;

 import com.frissbi.GcmIntentService;
 import com.frissbi.R;

public class GetIntent extends Activity {

    TextView name;
    TextView deal;
    TextView valid;
    TextView address;
    JSONObject json;
    GcmIntentService srv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rec);
        Intent intent = getIntent();

        name = (TextView) findViewById(R.id.textView1);
        deal = (TextView) findViewById(R.id.textView);
        valid = (TextView) findViewById(R.id.textView2);
        address = (TextView)findViewById(R.id.textView3);
        String message = intent.getExtras().getString("message");
        String message1 = intent.getExtras().getString("test5");
        String message2 = intent.getExtras().getString("test6");
        String message3 = intent.getExtras().getString("test7");
        String message4 = intent.getExtras().getString("test8");
        name.setText(message+message1);
        deal.setText(message+message2);
        valid.setText(message+message3);
        name.setText(message+message4);

        address.setText(message+message1);


    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        GcmIntentService srv;
        srv=new GcmIntentService();
        srv.cancelNotification(getApplicationContext());

    }


}

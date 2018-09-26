package tool.phonestate.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import tool.phonestate.app2.R;
import tools.android.phonestate.PhoneState;
import tools.android.phonestate.PhoneUsingListener;
import tools.android.phonestate.PhoneWatcher;

public class MainActivity extends Activity {

    Handler mHandler;
    PhoneWatcher mPhoneWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button btn1 = (Button) findViewById(R.id.btn1);
        final TextView btn1ret = (TextView) findViewById(R.id.btn1_ret);
        final Button btn2 = (Button) findViewById(R.id.btn2);
        final TextView btn2ret = (TextView) findViewById(R.id.btn2_ret);

        if (mHandler == null) {
            HandlerThread ht = new HandlerThread("phonestate-single-thread") {
                {
                    start();
                }
            };
            mHandler = new Handler(ht.getLooper());
        }

        mPhoneWatcher = new PhoneWatcher(this, true, new PhoneUsingListener() {
            @Override
            public void onUse(final PhoneState state) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("PPP", "onUse|" + state);
                        btn1ret.setText("onUse|" + state);
                    }
                });
            }

            @Override
            public void onNotUse() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("PPP", "onNotUse|");
                        btn1ret.setText("onNotUse|");
                    }
                });
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (mPhoneWatcher != null) {
//            mPhoneWatcher.resume(this);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (mPhoneWatcher != null) {
//            mPhoneWatcher.pause(this);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPhoneWatcher != null) {
            mPhoneWatcher.release(this);
        }
    }
}
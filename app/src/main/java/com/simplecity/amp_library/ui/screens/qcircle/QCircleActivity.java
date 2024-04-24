package com.simplecity.amp_library.ui.screens.qcircle;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.simplecity.amp_library.R;
import com.simplecity.amp_library.model.Song;
import com.simplecity.amp_library.playback.MediaManager;
import com.simplecity.amp_library.playback.constants.InternalIntents;
import com.simplecity.amp_library.ui.common.BaseActivity;
import com.simplecity.amp_library.ui.screens.main.MainActivity;
import com.simplecity.amp_library.utils.MusicServiceConnectionUtils;
import dagger.android.AndroidInjection;
import javax.inject.Inject;

public class QCircleActivity extends BaseActivity {

    public static final int EXTRA_ACCESSORY_COVER_OPENED = 0;
    public static final int EXTRA_ACCESSORY_COVER_CLOSED = 1;
    public static final String EXTRA_ACCESSORY_COVER_STATE = "com.lge.intent.extra.ACCESSORY_COVER_STATE";
    public static final String ACTION_ACCESSORY_COVER_EVENT = "com.lge.android.intent.action.ACCESSORY_COVER_EVENT";

    public static final String QUICKCOVERSETTINGS_QUICKCOVER_ENABLE = "quick_view_enable";
    static boolean quickCircleEnabled = false;
    int circleWidth = 0;
    int circleHeight = 0;
    int circleXpos = 0;
    int circleYpos = 0;
    int circleDiameter = 0;
    private final boolean DEBUG = true;
    private final String TAG = "QCircleActivity";
    int mQuickCoverState = 0;
    Context mContext;
    private Window win = null;
    private ContentResolver contentResolver = null;

    ImageButton backBtn = null;
    ImageButton skipBtn = null;
    ImageButton prevBtn = null;
    ImageButton pauseBtn = null;

    TextView textOne;
    TextView textTwo;

    @Inject
    MediaManager mediaManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_qcircle);

        final View circlemainView = findViewById(R.id.cover_main_view);*
        mContext = getApplicationContext();

        contentResolver = getContentResolver();

        registerIntentReceiver();

        setQuickCircleWindowParam();

        initializeViewInformationFromDB();

        initButtons();
        initTextViews();
        initializeBackButton();

        setCircleLayoutParam(circlemainView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final IntentFilter filter = new IntentFilter();
        filter.addAction(InternalIntents.PLAY_STATE_CHANGED);
        filter.addAction(InternalIntents.META_CHANGED);
        registerReceiver(mStatusListener, new IntentFilter(filter));
    }

    @Override
    public void onStop() {

        unregisterReceiver(mStatusListener);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext.unregisterReceiver(mIntentReceiver);
    }

    private void registerIntentReceiver() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_ACCESSORY_COVER_EVENT);
        mContext.registerReceiver(mIntentReceiver, filter);
    }

    void setQuickCircleWindowParam() {
        win = getWindow();
        if (win != null) {
            win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    void setCircleLayoutParam(View view) {

        RelativeLayout layout = (RelativeLayout) view;
        RelativeLayout.LayoutParams layoutParam = (RelativeLayout.LayoutParams) layout.getLayoutParams();

        layoutParam.width = circleDiameter;
        layoutParam.height = circleDiameter;

        if (circleXpos < 0) {

            layoutParam.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        } else {
            layoutParam.leftMargin = circleXpos;
        }
        layoutParam.topMargin = circleYpos + (circleHeight - circleDiameter) / 2;
        layout.setLayoutParams(layoutParam);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    void initializeViewInformationFromDB() {

        Log.d(TAG, "initializeViewInformationFromDB");
        if (contentResolver == null) {
            return;
        }

        Log.d(TAG, "initializeViewInformationFromDB");

        quickCircleEnabled = Settings.Global.getInt(contentResolver,
                QUICKCOVERSETTINGS_QUICKCOVER_ENABLE, 0) == 0;
        if (DEBUG) {
            Log.d(TAG, "quickCircleEnabled:" + quickCircleEnabled);
        }

        int id = getResources().getIdentifier("config_circle_window_width", "dimen",
                "com.lge.internal");
        circleWidth = getResources().getDimensionPixelSize(id);
        if (DEBUG) {
            Log.d(TAG, "circleWidth:" + circleWidth);
        }

        id = getResources()
                .getIdentifier("config_cover_window_height", "dimen", "com.lge.internal");
        circleHeight = getResources().getDimensionPixelSize(id);
        if (DEBUG) {
            Log.d(TAG, "circleHeight:" + circleHeight);
        }

        id = getResources()
                .getIdentifier("config_circle_window_x_pos", "dimen", "com.lge.internal");
        circleXpos = getResources().getDimensionPixelSize(id);
        if (DEBUG) {
            Log.d(TAG, "circleXpos:" + circleXpos);
        }

        id = getResources()
                .getIdentifier("config_circle_window_y_pos", "dimen", "com.lge.internal");
        circleYpos = getResources().getDimensionPixelSize(id);
        if (DEBUG) {
            Log.d(TAG, "circleYpos:" + circleYpos);
        }

        id = getResources().getIdentifier("config_circle_diameter", "dimen", "com.lge.internal");
        circleDiameter = getResources().getDimensionPixelSize(id);
        if (DEBUG) {
            Log.d(TAG, "circleDiameter:" + circleDiameter);
        }
    }

    private void initButtons() {

        prevBtn = findViewById(R.id.btn_prev);
        skipBtn = findViewById(R.id.btn_skip);
        pauseBtn = findViewById(R.id.btn_pause);
        setPauseButtonImage();

        prevBtn.setOnClickListener(v -> mediaManager.previous(false));

        skipBtn.setOnClickListener(v -> mediaManager.next());

        pauseBtn.setOnClickListener(v -> {
            mediaManager.togglePlayback();
            setPauseButtonImage();
        });
    }

    public void initTextViews() {
        textOne = findViewById(R.id.text1);
        textTwo = findViewById(R.id.text2);
    }

    public void setPauseButtonImage() {

        if (pauseBtn == null) {
            return;
        }
        if (MusicServiceConnectionUtils.serviceBinder != null && mediaManager.isPlaying()) {

        } else {

        }
    }

    private void initializeBackButton() {
        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(v -> QCircleActivity.this.finish());
    }

    void updateTrackInfo() {
        if (textOne == null || textTwo == null) {
            return;
        }

        Song song = mediaManager.getSong();
        if (song == null) return;

        textOne.setText(song.albumArtistName);
        textTwo.setText(song.name);
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action == null) {
                return;
            }

            if (ACTION_ACCESSORY_COVER_EVENT.equals(action)) {

                if (DEBUG) {
                    Log.d(TAG, "ACTION_ACCESSORY_COVER_EVENT");
                }

                mQuickCoverState = intent.getIntExtra(EXTRA_ACCESSORY_COVER_STATE,
                        EXTRA_ACCESSORY_COVER_OPENED);

                if (DEBUG) {
                    Log.d(TAG, "mQuickCoverState:" + mQuickCoverState);
                }

                if (mQuickCoverState == EXTRA_ACCESSORY_COVER_CLOSED) {
                    setQuickCircleWindowParam();
                } else if (mQuickCoverState == EXTRA_ACCESSORY_COVER_OPENED) {
                    Intent callFullscreen = new Intent(mContext, MainActivity.class);
                    startActivity(callFullscreen);

                    QCircleActivity.this.finish();
                }
            }
        }
    };

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        super.onServiceConnected(componentName, iBinder);
        updateTrackInfo();
    }

    private final BroadcastReceiver mStatusListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            if (action != null) {
                if (action.equals(InternalIntents.META_CHANGED)) {
                    updateTrackInfo();
                    setPauseButtonImage();
                } else if (action.equals(InternalIntents.PLAY_STATE_CHANGED)) {
                    setPauseButtonImage();
                }
            }
        }
    };

    @Override
    protected String screenName() {
        return TAG;
    }
}

package com.test.homekey;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
/**
 * 
 * @author kjer
 *
 */
public class LockService extends Service {

	private static String TAG = "LockService";
	private Intent LockIntent = null;
	private final static String ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON";
	private final static String ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF";

	private KeyguardManager mKeyguardManager = null;
	private KeyguardManager.KeyguardLock mKeyguardLock = null;

	private static View mView;
	private static WindowManager mWindow;
	private static WindowManager.LayoutParams mWindowManagerParams;
	public static final int SHOW_SCREEN_VIEW = 1;
	public static final int REMOVE_SCREEN_VIEW = 2;
	public boolean isCloseView = false;

	public void onCreate() {
		super.onCreate();
		LockIntent = new Intent(this, MainActivity.class);
		LockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		IntentFilter mScreenOnFilter = new IntentFilter(ACTION_SCREEN_ON);
		LockService.this.registerReceiver(mScreenOnReceiver, mScreenOnFilter);
		IntentFilter mScreenOffFilter = new IntentFilter(ACTION_SCREEN_OFF);
		LockService.this.registerReceiver(mScreenOffReceiver, mScreenOffFilter);
		initMyLockScreenView();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void initMyLockScreenView() {

		mView = LayoutInflater.from(this).inflate(
				R.layout.test_lock_screen_layout, null);
		mWindow = (WindowManager) this.getSystemService("window");

		mWindowManagerParams = new WindowManager.LayoutParams();
		// phone, above all apps, below the statebar
		mWindowManagerParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		// mWindowManagerParams.type =
		// WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG;
		// mWindowManagerParams.type = WindowManager.LayoutParams.TYPE_KEYGUARD;

		// mWindowManagerParams.type =
		// WindowManager.LayoutParams.TYPE_SYSTEM_ERROR ;
		mWindowManagerParams.format = -3;
		mWindowManagerParams.width = -1;
		mWindowManagerParams.height = -1;
		mWindowManagerParams.softInputMode = 3;
		mWindowManagerParams.x = 0;
		mWindowManagerParams.y = 0;
		mWindowManagerParams.gravity = 51;
		mWindowManagerParams.alpha = 1.0F;
		int flags = /*
					 * WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
					 */WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
				| WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED

				// |WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG
				// |WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
				// |WindowManager.LayoutParams.TYPE_PRIORITY_PHONE

				| WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER;
		mWindowManagerParams.flags = flags;

		Button button = (Button) mView.findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// view hide
				mWindow.removeView(mView);
				isCloseView = true;
			}
		});
		mWindow.addView(mView, mWindowManagerParams);
		isCloseView = false;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		return Service.START_STICKY;
	}

	public void onDestroy() {
		super.onDestroy();
		LockService.this.unregisterReceiver(mScreenOnReceiver);
		LockService.this.unregisterReceiver(mScreenOffReceiver);
		startService(new Intent(LockService.this, LockService.class));
	}

	private BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, intent.getAction());
			if (intent.getAction().equals(ACTION_SCREEN_ON)) {
				Log.i(TAG, "----on------ android.intent.action.SCREEN_ON------");
				mHandler.sendEmptyMessage(SHOW_SCREEN_VIEW);
			}
		}
	};

	private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i(TAG, intent.toString());
			if (action.equals(ACTION_SCREEN_OFF)) {
				getKeyGuardManagerToLock();
				mHandler.sendEmptyMessage(REMOVE_SCREEN_VIEW);
				Log.i(TAG, "----off-- android.intent.action.SCREEN_Off--");
			}
		}
	};

	private void getKeyGuardManagerToLock() {
		if (mKeyguardLock == null) {
			mKeyguardManager = (KeyguardManager) LockService.this
					.getSystemService(Context.KEYGUARD_SERVICE);
			mKeyguardLock = mKeyguardManager.newKeyguardLock("KeyguardLock");
		} else {
			mKeyguardLock.disableKeyguard();
		}
	}

	// handle the action about screen on or off
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_SCREEN_VIEW:
				if (isCloseView) {
					mWindow.addView(mView, mWindowManagerParams);
					isCloseView = false;
				} else {
					initMyLockScreenView();
				}
				break;
			case REMOVE_SCREEN_VIEW:
				if (!isCloseView) {
					mWindow.removeView(mView);
					isCloseView = true;
				}
				break;
			}
		}
	};

}

package com.example.bluno;

import com.example.bluno.BleCmd;
import com.example.bluno.Fragment.FragmentCustomize;
import com.example.bluno.Fragment.FragmentMain;
import com.example.bluno.Fragment.FragmentShare;
import com.example.bluno.Fragment.FragmentUser;
import com.example.bluno.ProgressWheel;
import com.example.bluno.DeviceControlActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.larswerkman.colorpicker.ColorPicker;
import com.larswerkman.colorpicker.ColorPicker.OnColorChangedListener;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class BLUNOActivity extends BlunoLibrary {

	ImageView mainButton, themeButton, customizeButton, shareButton, userButton;

	FragmentManager fragmentManager;
	FragmentTransaction fragmentTransaction;

	int Current_Fragment_Index = 1;
	int Select_Fragment_Index;

	Fragment Fragment_main, Fragment_share, Fragment_user, Fragment_custom;

	public static Uri profileUri;

	String UserUid;

    public static int delayTime = 300;
	public static boolean isColorChange = false;
	public static boolean isLastSwitchOn = false;
	public static boolean isMusicOn = true;
	public static boolean isSleepOn = false;
	public static boolean isSpeechOn = false;

	/**************************************************************************************/
	public final static String TAG = DeviceControlActivity.class.getSimpleName();

    private connectionStateEnum mConnectionState=connectionStateEnum.isNull;
	private PlainProtocol mPlainProtocol= new PlainProtocol();
	private ProgressWheel progressWheel;
	private Typeface txtotf;
	private ImageView titleImageView;
	private ImageView arduinoinputdispArea = null;


	public static final int LEDMode = 0;
	public static final int RockerMode = 1;
	public static final int Theme = 2;
	public static final int Custom = 3;
	public static final int Sleep = 4;
	public static final int Speech = 5;

    public static byte Modestates = LEDMode;

	private static Handler receivedHandler = new Handler();

    public static boolean print = true;

	private Runnable colorRunnable = new Runnable() {
		
		@Override
		public void run() {
			switch (Modestates) {
				case LEDMode:
				if(FragmentMain.picker.mIsSwitchOn)
				{
					if(isColorChange || (isLastSwitchOn==false))
					{
						serialSend(mPlainProtocol.write(BleCmd.RGBLed,
									((FragmentMain.picker.getColor() & 0x00ff0000)>>16),
									((FragmentMain.picker.getColor() & 0x0000ff00)>>8),
									((FragmentMain.picker.getColor() & 0x000000ff)>>0)
									));
					}
					isColorChange=false;
					isLastSwitchOn=true;
				}else{
					if(isLastSwitchOn)
					{
						serialSend(mPlainProtocol.write(BleCmd.RGBLed,0,0,0));
					}
					isLastSwitchOn=false;
				}
				break;
				case RockerMode:
					Log.e("Modestates","RockerMode");
					if(isMusicOn){
						serialSend(mPlainProtocol.write(BleCmd.Rocker));
					}else{
						serialSend(mPlainProtocol.write(BleCmd.RGBLed, 0, 0, 0));
					}
					break;
				case Theme:
					Log.e("Modestates","ThemeMode");
					serialSend(mPlainProtocol.write(BleCmd.Theme,FragmentMain.selected_theme));
					break;
				case Custom:
                    for(int i = 0 ; i < 170; i++){
                    	int r = Integer.parseInt(FragmentShare.CustomPixel[i].substring(0,3));
                    	int g = Integer.parseInt(FragmentShare.CustomPixel[i].substring(3,6));
                    	int b = Integer.parseInt(FragmentShare.CustomPixel[i].substring(6));

                    	serialSend(mPlainProtocol.write(BleCmd.Custom, i, r , g , b));
					//	Toast.makeText(getApplicationContext(), r + " " +  g + " " + b, Toast.LENGTH_SHORT).show();

						if(i == 169) {
                            delayTime = 0;
                        }
                        //MatrixTime(delayTime);
                    }
					break;
				case Sleep:
					Log.e("Modestates","SleepMode");
					serialSend(mPlainProtocol.write(BleCmd.Sleep));
					break;
				case Speech:
					Log.e("Modestates","SpeechMode");
					serialSend(mPlainProtocol.write(BleCmd.Speech, FragmentMain.SPEECH_ON));
					break;
				default:
					break;
			}

			receivedHandler.postDelayed(colorRunnable, 50);
		}
	};

    public void MatrixTime(int delayTime){
        long saveTime = System.currentTimeMillis();
        long currTime = 0;
        while( currTime - saveTime < delayTime){
            currTime = System.currentTimeMillis();
        }
    }

	public void onSerialReceived(String theString) {
		System.out.println("displayData "+theString);
    	
    	mPlainProtocol.mReceivedframe.append(theString) ;
    	System.out.print("mPlainProtocol.mReceivedframe:");
    	System.out.println(mPlainProtocol.mReceivedframe.toString());

    	while(mPlainProtocol.available())
    	{
        	System.out.print("receivedCommand:");
        	System.out.println(mPlainProtocol.receivedCommand);
    		
    		if(mPlainProtocol.receivedCommand.equals(BleCmd.Rocker))
    		{
    			if(Modestates == RockerMode)
        		{
            		System.out.println("received Rocker");
            	}
    		}

    	}
		
	}

	//configure the font
	private void FontConfig() {
		
		// Font path
		String fontPath = "fonts/yueregular.otf";
        
        txtotf = Typeface.createFromAsset(getAssets(), fontPath);
	}


	//configure the title image which shows the connection state
	void titleImageConfig()
	{
        titleImageView =  (ImageView)findViewById(R.id.title_image_view);
        titleImageView.setImageResource(R.drawable.title_scan);
        titleImageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				buttonScanOnClickProcess();
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("BLUNOActivity onCreate");
		setContentView(R.layout.activity_bluno);

		Fragment_main = new FragmentMain();
		Fragment_share = new FragmentShare();
		Fragment_custom = new FragmentCustomize();
		Fragment_user = new FragmentUser();

		fragmentManager = getSupportFragmentManager();
		fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.replace(R.id.content, Fragment_main);
		fragmentTransaction.commit();


		mainButton = (ImageView)findViewById(R.id.mainButton);
		customizeButton = (ImageView)findViewById(R.id.customizeButton);
		shareButton = (ImageView)findViewById(R.id.shareButton);
		userButton = (ImageView)findViewById(R.id.userButton);

		mainButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Select_Fragment_Index = 1;
				Button_Image_Change(Current_Fragment_Index,Select_Fragment_Index);
				Fragment_Change(Fragment_main);
			}
		});
		customizeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Select_Fragment_Index = 3;
				Button_Image_Change(Current_Fragment_Index,Select_Fragment_Index);
				Fragment_Change(Fragment_custom);
			}
		});
		shareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Select_Fragment_Index = 4;
				Button_Image_Change(Current_Fragment_Index,Select_Fragment_Index);
				Fragment_Change(Fragment_share);
			}
		});
		userButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Select_Fragment_Index = 5;
				Button_Image_Change(Current_Fragment_Index,Select_Fragment_Index);
				Fragment_Change(Fragment_user);
			}
		});

		UserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

		File localFile = null;
		try {
			localFile = File.createTempFile("images", "jpg");
		} catch (IOException e) {
			e.printStackTrace();
		}

		FirebaseDatabase.getInstance().getReference().child("users").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				// 처음 넘어오는 데이터 // ArrayList 값.
				for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
					if (snapshot.child("uid").getValue(String.class).equals(UserUid)) {
						Uri url = Uri.parse(snapshot.child("profileImageUrl").getValue(String.class).toString());
						profileUri = url;
						break;
					}else{
						Log.e("MainActivity","have not data");
					}
					// 아래는 기본 프로필 이미지,
					profileUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/blunolight.appspot.com/o/images%2Ficon01non.png?alt=media&token=cbd92845-3865-4e52-b9b5-675610dd85c4");
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});

		/************************************************************/
		//set the Baudrate of the Serial port
		serialBegin(115200);
		
        onCreateProcess();

		FontConfig();

		titleImageConfig();

		
		
	}

	private void Button_Image_Change(int current_Fragment_Index, int select_Fragment_Index){
		switch (current_Fragment_Index){
			case 1:
				mainButton.setImageResource(R.drawable.homeicon);
				break;
			case 2:
				themeButton.setImageResource(R.drawable.themeicon);
				break;
			case 3:
				customizeButton.setImageResource(R.drawable.customicon);
				break;
			case 4:
				shareButton.setImageResource(R.drawable.shareicon);
				break;
			case 5:
				userButton.setImageResource(R.drawable.usericon);
				break;
		}
		switch (select_Fragment_Index){
			case 1:
				mainButton.setImageResource(R.drawable.homeicon_on);
				break;
			case 2:
				themeButton.setImageResource(R.drawable.themeicon_on);
				break;
			case 3:
				customizeButton.setImageResource(R.drawable.customicon_on);
				break;
			case 4:
				shareButton.setImageResource(R.drawable.shareicon_on);
				break;
			case 5:
				userButton.setImageResource(R.drawable.usericon_on);
				break;
		}
	}

	private void Fragment_Change(Fragment changeFragment){
		fragmentTransaction = fragmentManager.beginTransaction();

		if(Current_Fragment_Index > Select_Fragment_Index){
			fragmentTransaction.setCustomAnimations(R.anim.fromleft, R.anim.toright);
		}else if(Current_Fragment_Index < Select_Fragment_Index){
			fragmentTransaction.setCustomAnimations(R.anim.fromright, R.anim.toleft);
		}
		Current_Fragment_Index = Select_Fragment_Index;
		fragmentTransaction.replace(R.id.content, changeFragment);
		fragmentTransaction.commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		System.out.println("BlUNOActivity onResume");
		onResumeProcess();		
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		onActivityResultProcess(requestCode, resultCode, data);

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		System.out.println("BLUNOActivity onPause");
		//receivedHandler.removeCallbacks(colorRunnable);
        onPauseProcess();
	}
	
	protected void onStop() {
		super.onStop();
		onStopProcess();
		System.out.println("MiUnoActivity onStop");
		

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("MiUnoActivity onDestroy");
        onDestroyProcess();

	}

	@Override
	public void onConectionStateChange(connectionStateEnum theConnectionState) {
	
		mConnectionState=theConnectionState;
		switch (mConnectionState) {
		case isScanning:
	        titleImageView.setImageResource(R.drawable.title_scanning);
			break;

		case isConnected:
	        titleImageView.setImageResource(R.drawable.title_connected);
			switch (Modestates) {
			case LEDMode:
				receivedHandler.post(colorRunnable); 
				break;
			case RockerMode:
				
				break;

			default:
				break;
			}
			
			break;
		case isConnecting:
	        titleImageView.setImageResource(R.drawable.title_connecting);
			break;
		case isToScan:
	        titleImageView.setImageResource(R.drawable.title_scan);
			//receivedHandler.removeCallbacks(colorRunnable);
			break;
		default:
			break;
		}
	}




}

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

    public static int delayTime = 1000;
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

                    for(int i = 0 ; i < 43; i++){
                        serialSend(mPlainProtocol.write(BleCmd.Custom, i, FragmentShare.CustomPixel[i * 4 + 0], FragmentShare.CustomPixel[i * 4 + 1], FragmentShare.CustomPixel[i * 4 + 2], FragmentShare.CustomPixel[i * 4 + 3]));

                        if(i == 42) {
                            delayTime = 0;
                        }
                        MatrixTime(delayTime);
                    }
//                    serialSend(mPlainProtocol.write(BleCmd.Custom, 0, FragmentShare.CustomPixel[0], FragmentShare.CustomPixel[1], FragmentShare.CustomPixel[2], FragmentShare.CustomPixel[3]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 1, FragmentShare.CustomPixel[4], FragmentShare.CustomPixel[5], FragmentShare.CustomPixel[6], FragmentShare.CustomPixel[7]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 2, FragmentShare.CustomPixel[8], FragmentShare.CustomPixel[9], FragmentShare.CustomPixel[10], FragmentShare.CustomPixel[11]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 3, FragmentShare.CustomPixel[12], FragmentShare.CustomPixel[13], FragmentShare.CustomPixel[14], FragmentShare.CustomPixel[15]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 4, FragmentShare.CustomPixel[16], FragmentShare.CustomPixel[17], FragmentShare.CustomPixel[18], FragmentShare.CustomPixel[19]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 5, FragmentShare.CustomPixel[20], FragmentShare.CustomPixel[21], FragmentShare.CustomPixel[22], FragmentShare.CustomPixel[23]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 6, FragmentShare.CustomPixel[24], FragmentShare.CustomPixel[25], FragmentShare.CustomPixel[26], FragmentShare.CustomPixel[27]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 7, FragmentShare.CustomPixel[28], FragmentShare.CustomPixel[29], FragmentShare.CustomPixel[30], FragmentShare.CustomPixel[31]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 8, FragmentShare.CustomPixel[32], FragmentShare.CustomPixel[33], FragmentShare.CustomPixel[34], FragmentShare.CustomPixel[35]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 9, FragmentShare.CustomPixel[36], FragmentShare.CustomPixel[37], FragmentShare.CustomPixel[38], FragmentShare.CustomPixel[39]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 10, FragmentShare.CustomPixel[40], FragmentShare.CustomPixel[41], FragmentShare.CustomPixel[42], FragmentShare.CustomPixel[43]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 11, FragmentShare.CustomPixel[44], FragmentShare.CustomPixel[45], FragmentShare.CustomPixel[46], FragmentShare.CustomPixel[47]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 12, FragmentShare.CustomPixel[48], FragmentShare.CustomPixel[49], FragmentShare.CustomPixel[50], FragmentShare.CustomPixel[51]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 13, FragmentShare.CustomPixel[52], FragmentShare.CustomPixel[53], FragmentShare.CustomPixel[54], FragmentShare.CustomPixel[55]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 14, FragmentShare.CustomPixel[56], FragmentShare.CustomPixel[57], FragmentShare.CustomPixel[58], FragmentShare.CustomPixel[59]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 15, FragmentShare.CustomPixel[60], FragmentShare.CustomPixel[61], FragmentShare.CustomPixel[62], FragmentShare.CustomPixel[63]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 16, FragmentShare.CustomPixel[64], FragmentShare.CustomPixel[65], FragmentShare.CustomPixel[66], FragmentShare.CustomPixel[67]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 17, FragmentShare.CustomPixel[68], FragmentShare.CustomPixel[69], FragmentShare.CustomPixel[70], FragmentShare.CustomPixel[71]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 18, FragmentShare.CustomPixel[72], FragmentShare.CustomPixel[73], FragmentShare.CustomPixel[74], FragmentShare.CustomPixel[75]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 19, FragmentShare.CustomPixel[76], FragmentShare.CustomPixel[77], FragmentShare.CustomPixel[78], FragmentShare.CustomPixel[79]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 20, FragmentShare.CustomPixel[80], FragmentShare.CustomPixel[81], FragmentShare.CustomPixel[82], FragmentShare.CustomPixel[83]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 21, FragmentShare.CustomPixel[84], FragmentShare.CustomPixel[85], FragmentShare.CustomPixel[86], FragmentShare.CustomPixel[87]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 22, FragmentShare.CustomPixel[88], FragmentShare.CustomPixel[89], FragmentShare.CustomPixel[90], FragmentShare.CustomPixel[91]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 23, FragmentShare.CustomPixel[92], FragmentShare.CustomPixel[93], FragmentShare.CustomPixel[94], FragmentShare.CustomPixel[95]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 24, FragmentShare.CustomPixel[96], FragmentShare.CustomPixel[97], FragmentShare.CustomPixel[98], FragmentShare.CustomPixel[99]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 25, FragmentShare.CustomPixel[100], FragmentShare.CustomPixel[101], FragmentShare.CustomPixel[102], FragmentShare.CustomPixel[103]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 26, FragmentShare.CustomPixel[104], FragmentShare.CustomPixel[105], FragmentShare.CustomPixel[106], FragmentShare.CustomPixel[107]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 27, FragmentShare.CustomPixel[108], FragmentShare.CustomPixel[109], FragmentShare.CustomPixel[110], FragmentShare.CustomPixel[111]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 28, FragmentShare.CustomPixel[112], FragmentShare.CustomPixel[113], FragmentShare.CustomPixel[114], FragmentShare.CustomPixel[115]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 29, FragmentShare.CustomPixel[116], FragmentShare.CustomPixel[117], FragmentShare.CustomPixel[118], FragmentShare.CustomPixel[119]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 30, FragmentShare.CustomPixel[120], FragmentShare.CustomPixel[121], FragmentShare.CustomPixel[122], FragmentShare.CustomPixel[123]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 31, FragmentShare.CustomPixel[124], FragmentShare.CustomPixel[125], FragmentShare.CustomPixel[126], FragmentShare.CustomPixel[127]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 32, FragmentShare.CustomPixel[128], FragmentShare.CustomPixel[129], FragmentShare.CustomPixel[130], FragmentShare.CustomPixel[131]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 33, FragmentShare.CustomPixel[132], FragmentShare.CustomPixel[133], FragmentShare.CustomPixel[134], FragmentShare.CustomPixel[135]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 34, FragmentShare.CustomPixel[136], FragmentShare.CustomPixel[137], FragmentShare.CustomPixel[138], FragmentShare.CustomPixel[139]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 35, FragmentShare.CustomPixel[140], FragmentShare.CustomPixel[141], FragmentShare.CustomPixel[142], FragmentShare.CustomPixel[143]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 36, FragmentShare.CustomPixel[144], FragmentShare.CustomPixel[145], FragmentShare.CustomPixel[146], FragmentShare.CustomPixel[147]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 37, FragmentShare.CustomPixel[148], FragmentShare.CustomPixel[149], FragmentShare.CustomPixel[150], FragmentShare.CustomPixel[151]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 38, FragmentShare.CustomPixel[152], FragmentShare.CustomPixel[153], FragmentShare.CustomPixel[154], FragmentShare.CustomPixel[155]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 39, FragmentShare.CustomPixel[156], FragmentShare.CustomPixel[157], FragmentShare.CustomPixel[158], FragmentShare.CustomPixel[159]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 40, FragmentShare.CustomPixel[160], FragmentShare.CustomPixel[161], FragmentShare.CustomPixel[162], FragmentShare.CustomPixel[163]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 41, FragmentShare.CustomPixel[164], FragmentShare.CustomPixel[165], FragmentShare.CustomPixel[166], FragmentShare.CustomPixel[167]));
//					serialSend(mPlainProtocol.write(BleCmd.Custom, 42, FragmentShare.CustomPixel[168], FragmentShare.CustomPixel[169], FragmentShare.CustomPixel[170], FragmentShare.CustomPixel[171]));

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

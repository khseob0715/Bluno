package com.example.bluno.Fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluno.BLUNOActivity;
import com.example.bluno.Dialog.ShareDialog;
import com.example.bluno.R;
import com.example.bluno.views.CanvasView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.larswerkman.colorpicker.ColorPicker;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class FragmentMain extends Fragment implements View.OnClickListener{

    public static ColorPicker picker;

    private CircleImageView CircleImageView_Button01;
    private CircleImageView CircleImageView_Button02;
    private CircleImageView CircleImageView_Button03;

    public static int SPEECH_ON = 0;

    private CircleImageView[] Theme;
    private CircleImageView[] AnimeTheme;

    public static int selected_theme;

    TextToSpeech tts;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.RECORD_AUDIO},5);
        }

        tts=new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.KOREAN);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, null);

        picker = (ColorPicker) view.findViewById(R.id.picker);

        picker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener(){

            @Override
            public void onColorChanged(int color) {
                BLUNOActivity.isColorChange=true;

                CircleImageView_Button01.setImageResource(R.drawable.music_black);
                CircleImageView_Button02.setImageResource(R.drawable.sleep_black);
                CircleImageView_Button03.setImageResource(R.drawable.speeh_black);

                if(picker.mIsSwitchOn){
                    BLUNOActivity.Modestates = 0;
                }
            }
        });

        Theme = new CircleImageView[4];
        AnimeTheme = new CircleImageView[4];

        Theme[0] = (CircleImageView)view.findViewById(R.id.theme01);
        Theme[1] = (CircleImageView)view.findViewById(R.id.theme02);
        Theme[2] = (CircleImageView)view.findViewById(R.id.theme03);
        Theme[3] = (CircleImageView)view.findViewById(R.id.theme04);

        AnimeTheme[0] = (CircleImageView)view.findViewById(R.id.AnimationTheme01);
        AnimeTheme[1] = (CircleImageView)view.findViewById(R.id.AnimationTheme02);
        AnimeTheme[2] = (CircleImageView)view.findViewById(R.id.AnimationTheme03);
        AnimeTheme[3] = (CircleImageView)view.findViewById(R.id.AnimationTheme04);

        for(int i = 0 ; i < 4; i++) {
            Theme[i].setOnClickListener(this);
            AnimeTheme[i].setOnClickListener(this);
        }

        CircleImageView_Button01 = (CircleImageView)view.findViewById(R.id.circlebutton);
        CircleImageView_Button01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BLUNOActivity.isMusicOn = !BLUNOActivity.isMusicOn;
                if(BLUNOActivity.isMusicOn){
                    CircleImageView_Button01.setImageResource(R.drawable.music);
                    BLUNOActivity.Modestates = 1; // RockerMode
                    BLUNOActivity.isLastSwitchOn = false;
                    BLUNOActivity.isSleepOn = false;
                    BLUNOActivity.isSpeechOn = false;
                    CircleImageView_Button02.setImageResource(R.drawable.sleep_black);
                    CircleImageView_Button03.setImageResource(R.drawable.speeh_black);
                }else{
                    CircleImageView_Button01.setImageResource(R.drawable.music_black);
                }
            }
        });

        CircleImageView_Button02 = (CircleImageView)view.findViewById(R.id.circlebutton2);
        CircleImageView_Button02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BLUNOActivity.isSleepOn = !BLUNOActivity.isSleepOn;
                if(BLUNOActivity.isSleepOn){
                    CircleImageView_Button02.setImageResource(R.drawable.sleep);
                    BLUNOActivity.Modestates = 4; // Sleep
                    BLUNOActivity.isLastSwitchOn = false;
                    BLUNOActivity.isMusicOn = false;
                    BLUNOActivity.isSpeechOn = false;
                    CircleImageView_Button01.setImageResource(R.drawable.music_black);
                    CircleImageView_Button03.setImageResource(R.drawable.speeh_black);
                }else{
                    CircleImageView_Button02.setImageResource(R.drawable.sleep_black);
                }


            }
        });

        CircleImageView_Button03 = (CircleImageView)view.findViewById(R.id.circlebutton3);
        CircleImageView_Button03.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BLUNOActivity.isSpeechOn = !BLUNOActivity.isSpeechOn;
                if(BLUNOActivity.isSpeechOn){
                    CircleImageView_Button03.setImageResource(R.drawable.speech);
                    BLUNOActivity.Modestates = 5; // Speech
                    BLUNOActivity.isLastSwitchOn = false;
                    BLUNOActivity.isMusicOn = false;
                    BLUNOActivity.isSleepOn = false;
                    CircleImageView_Button01.setImageResource(R.drawable.music_black);
                    CircleImageView_Button02.setImageResource(R.drawable.sleep_black);
                    inputVoice();
                }else{
                    CircleImageView_Button03.setImageResource(R.drawable.speeh_black);
                }


            }
        });

        return view;
    }


    public void inputVoice() {
        try{
            Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getContext().getPackageName());
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");
            final SpeechRecognizer stt=SpeechRecognizer.createSpeechRecognizer(getContext());
            stt.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {

                }

                @Override
                public void onBeginningOfSpeech() {

                }

                @Override
                public void onRmsChanged(float rmsdB) {

                }

                @Override
                public void onBufferReceived(byte[] buffer) {

                }

                @Override
                public void onEndOfSpeech() {
                    BLUNOActivity.isSpeechOn = !BLUNOActivity.isSpeechOn;
                }

                @Override
                public void onError(int error) {
                    stt.destroy();
                }

                //음성결과 넘어오는부분
                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> result=(ArrayList<String>)results.get(SpeechRecognizer.RESULTS_RECOGNITION);
                    //  txt.append(result.get(0)+"\n");
                    replyAnswer(result.get(0));
                    stt.destroy();
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
            stt.startListening(intent);
        }catch (Exception e){
            Log.e("AI","exception");
        }
    }

    private void replyAnswer(String input){
        if(input.equals("켜줘") || input.equals("켜 줘") || input.equals("켜")){
            SPEECH_ON = 1;
        }
        else if(input.equals("노래")){
            Toast.makeText(getContext(),input.toString(),Toast.LENGTH_SHORT).show();
            SPEECH_ON = 2;
        }else if(input.equals("꺼줘") || input.equals("꺼 줘") || input.equals("꺼")){
            SPEECH_ON = 0;
        }else if(input.equals("수면모드")){
            SPEECH_ON = 3;
        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.theme01:
                //  Toast.makeText(getContext(),"Theme01", Toast.LENGTH_SHORT).show();
                selected_theme = 101;
                break;
            case R.id.theme02:
                //  Toast.makeText(getContext(),"Theme02", Toast.LENGTH_SHORT).show();
                selected_theme = 102;
                break;
            case R.id.theme03:
//                Toast.makeText(getContext(),"Theme03", Toast.LENGTH_SHORT).show();
                selected_theme = 103;
                break;
            case R.id.theme04:
//                Toast.makeText(getContext(),"Theme04", Toast.LENGTH_SHORT).show();
                selected_theme = 104;
                break;
            case R.id.AnimationTheme01:
//                Toast.makeText(getContext(),"AnimationTheme01", Toast.LENGTH_SHORT).show();
                selected_theme = 201;
                break;
            case R.id.AnimationTheme02:
//                Toast.makeText(getContext(),"Theme04", Toast.LENGTH_SHORT).show();
                selected_theme = 202;
                break;
            case R.id.AnimationTheme03:
//                Toast.makeText(getContext(),"Theme04", Toast.LENGTH_SHORT).show();
                selected_theme = 203;
                break;
            case R.id.AnimationTheme04:
//                Toast.makeText(getContext(),"Theme04", Toast.LENGTH_SHORT).show();
                selected_theme = 204;
                break;
        }
        BLUNOActivity.Modestates = 2; // Theme;
    }
}

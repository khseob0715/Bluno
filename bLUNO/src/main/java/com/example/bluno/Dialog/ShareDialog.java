package com.example.bluno.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bluno.R;
import com.example.bluno.model.LightModel;
import com.example.bluno.views.CanvasView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

public class ShareDialog {

    private Context context;

    final Integer[] integers = new Integer[100];
    private DatabaseReference mDatabase;

    public ShareDialog(Context context) {
        this.context = context;
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    // 호출할 다이얼로그 함수를 정의한다.
    public void callFunction(final String fileName, final CanvasView canvasView) {

        // 커스텀 다이얼로그를 정의하기위해 Dialog클래스를 생성한다.
        final Dialog dlg = new Dialog(context);

        // 액티비티의 타이틀바를 숨긴다.
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.setContentView(R.layout.dialog_share);

        // 커스텀 다이얼로그를 노출한다.
        dlg.show();

        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
        final EditText message = (EditText) dlg.findViewById(R.id.mesgase);
        final Button okButton = (Button) dlg.findViewById(R.id.dialog_okButton);
        final Button shareButton = (Button) dlg.findViewById(R.id.dialog_shareButton);
        final Button cancelButton = (Button) dlg.findViewById(R.id.cancelButton);

        String pixelString = canvasView.toString();

        String[] strs = pixelString.split(",");
        int len = strs.length;

        for(int i = 0 ; i < len ; i++){
            integers[i] = Integer.parseInt(strs[i]);
        }

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LightModel lightModel = new LightModel();

                lightModel.ShareUserName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

                if(message.getText().toString().equals("")){
                    lightModel.ShareLightDescription = lightModel.ShareUserName.toString() +"의 조명";
                }else {
                    lightModel.ShareLightDescription = message.getText().toString();
                }



                // 1번 조명   2번 조명  3번 조명 -------->?

                List<Integer> list = Arrays.asList(integers);
                lightModel.SharePixel = list;

                Long tsLong = System.currentTimeMillis();
                Date date = new Date(tsLong);
                String ts = tsLong.toString();
                lightModel.timestamp  = ts;
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                lightModel.ShareDate = dateFormat.format(date);
                lightModel.ShareUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                lightModel.bShare = false;
                lightModel.LigthImageUrl = fileName;

                mDatabase.child("recipe").child(ts).setValue(lightModel); // 데이터 쓰기.

                // 커스텀 다이얼로그를 종료한다.
                dlg.dismiss();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LightModel lightModel = new LightModel();

                lightModel.ShareUserName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

                if(message.getText().toString().equals("")){
                    lightModel.ShareLightDescription = lightModel.ShareUserName.toString() +"의 조명";
                }else {
                    lightModel.ShareLightDescription = message.getText().toString();
                }


                // 1번 조명   2번 조명  3번 조명 -------->?

                List<Integer> list = Arrays.asList(integers);
                lightModel.SharePixel = list;

                Long tsLong = System.currentTimeMillis();
                Date date = new Date(tsLong);
                String ts = tsLong.toString();
                lightModel.timestamp  = ts;
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");

                lightModel.ShareDate = f.format(date);
                lightModel.ShareUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                lightModel.bShare = true;
                lightModel.LigthImageUrl = fileName;

                mDatabase.child("recipe").child(ts).setValue(lightModel); // 데이터 쓰기.
                // FirebaseAuth.getInstance().getCurrentUser().getUid()  userId;

                Toast.makeText(context,"공유",Toast.LENGTH_SHORT).show();
                dlg.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "취소 했습니다.", Toast.LENGTH_SHORT).show();

                // 커스텀 다이얼로그를 종료한다.
                dlg.dismiss();
            }
        });
    }
}
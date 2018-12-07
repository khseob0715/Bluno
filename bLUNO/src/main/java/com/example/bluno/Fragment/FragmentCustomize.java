package com.example.bluno.Fragment;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluno.Dialog.CustomFlag;
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
import com.skydoves.colorpickerview.AlphaTileView;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Date;
import java.util.List;

public class FragmentCustomize extends Fragment {

    private ColorPickerView colorPickerView;
    public String customize_rgbcolor, customize_hexcolor;


    // 플래그 처음에 안뜨게
    private boolean FLAG_PALETTE = false;
    private boolean FLAG_SELECTOR = false;



    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    public static int customize_touch_color = 0;

    private ViewGroup buttonlist;

    private boolean pickingColor = false;
    DrawFragment drawFragment;
    CanvasView canvasView;

    DatabaseReference mDatabase;

    StorageReference mStoragedRef;

    private TextView textView;
    private AlphaTileView alphaTileView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStoragedRef = FirebaseStorage.getInstance().getReference();

        drawFragment = new DrawFragment();

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.customize_framelayout, drawFragment);
        fragmentTransaction.commit();

        canvasView = drawFragment.getCanvas();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_customize,null);



        buttonlist = view.findViewById(R.id.buttonlist);

        for (int i = 0; i < buttonlist.getChildCount(); i++)
            buttonlist.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(-2414079));


        colorPickerView = view.findViewById(R.id.colorPickerView);
        colorPickerView.setFlagView(new CustomFlag(getContext(), R.layout.layout_flag));
        colorPickerView.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                //setLayoutColor(envelope);

                //int[] argb = envelope.getArgb();
                customize_touch_color = envelope.getColor();

                for (int i = 0; i < buttonlist.getChildCount(); i++)
                    buttonlist.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(customize_touch_color));

                Log.e("customize_touch_color",""+customize_touch_color);
                canvasView = drawFragment.getCanvas();
                if(canvasView != null) {
                    canvasView.color = customize_touch_color;
                }

            }
        });



        // fill canvas on button click   // 색 복사?
        view.findViewById(R.id.pickerButton).setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickingColor = true;
                canvasView = drawFragment.getCanvas();
                canvasView.setOnTouchListener(new CanvasView.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if (pickingColor) {
                            int tempBrushSize = canvasView.brushSize;
                            canvasView.brushSize = 1;
                            List<View> pixels = canvasView.findPixels(motionEvent);
                            canvasView.color = ((ColorDrawable) pixels.get(0).getBackground()).getColor();
                            for (int i = 0; i < buttonlist.getChildCount(); i++)
                                buttonlist.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(canvasView.color));

                            canvasView.brushSize = tempBrushSize;
                            pickingColor = false;
                            return true;
                        }
                        return false;
                    }
                });
                Toast.makeText(getActivity(), R.string.colorpickermessage, Toast.LENGTH_SHORT).show();
            }
        });

        // fill canvas on button click
        view.findViewById(R.id.fillButton).setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                canvasView = drawFragment.getCanvas();
                canvasView.fill();
            }
        });

        // reset canvas on button click
        view.findViewById(R.id.clearButton).setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                canvasView = drawFragment.getCanvas();
                canvasView.fill_white(0);
            }
        });

        // save button
        view.findViewById(R.id.saveButton).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                canvasView = drawFragment.getCanvas();

                String path =  Environment.getExternalStorageDirectory().getAbsolutePath();
                Bitmap b = Bitmap.createBitmap(canvasView.getWidth(), canvasView.getHeight(), Bitmap.Config.RGB_565);


                if(b!=null){
                    try {
                        File f = new File(path+"/notes");
                        f.mkdir();
                        Long tsLong = System.currentTimeMillis();
                        Date date = new Date(tsLong);
                        String ts = tsLong.toString();
                        final File f2 = new File(path + "/notes/"+ts+".png");

                        Canvas c = new Canvas( b );
                        canvasView.draw( c );

                        FileOutputStream fos = new FileOutputStream(f2);

                        if ( fos != null )
                        {
                            b.compress(Bitmap.CompressFormat.PNG, 100, fos );
                            fos.close();
                        }

                        StorageReference riversRef = mStoragedRef.child("images/LigthImage" +  "/" + f2);

                        riversRef.putFile(Uri.fromFile(f2)).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot task) {
                                // 저장이 제대로 되었을 경우 다이얼로그 호출.
                                ShareDialog customDialog = new ShareDialog(getContext());
                                // 커스텀 다이얼로그를 호출한다.
                                // 커스텀 다이얼로그의 결과를 출력할 TextView를 매개변수로 같이 넘겨준다.

                                customDialog.callFunction(task.getDownloadUrl().toString(), canvasView);

                            }
                        });
                    } catch( Exception e ){
                        Log.e("testSaveView", "Exception: " + e.toString() );
                    }

                }
            }
        });


        view.findViewById(R.id.brushButton).setOnClickListener(new FloatingActionButton.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setTitle(R.string.brushsize);

                final SeekBar input = new SeekBar(getContext());
                input.setMax(5);
                input.setProgress(drawFragment.getCanvas().brushSize-1);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMarginStart(100);
                input.setLayoutParams(params);
                dialog.setView(input);

                dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        drawFragment.getCanvas().brushSize = input.getProgress()+1;
                    }
                });
                dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });
        return view;
    }

    private int getColor(ImageView selectedImage, int evX, int evY){
        selectedImage.setDrawingCacheEnabled(true);
        Bitmap bitmap=Bitmap.createBitmap(selectedImage.getDrawingCache());
        selectedImage.setDrawingCacheEnabled(false);
        return bitmap.getPixel(evX,evY);
    }

//
//
//    private void setLayoutColor(ColorEnvelope envelope) {

//        textView.setText("#" + envelope.getHexCode());
//        alphaTileView.setPaintColor(envelope.getColor());
//    }

    // 스펙트럼 위에 그림

    public void palette() {
        if (FLAG_PALETTE)
            colorPickerView.setPaletteDrawable(ContextCompat.getDrawable(getContext(), R.drawable.palette));
        else
            colorPickerView.setPaletteDrawable(ContextCompat.getDrawable(getContext(), R.drawable.palettebar));
        FLAG_PALETTE = !FLAG_PALETTE;
    }

    // 슬라이드바 변경
    public void selector() {
        if (FLAG_SELECTOR)
            colorPickerView.setSelectorDrawable(ContextCompat.getDrawable(getContext(), R.drawable.wheel));
        else
            colorPickerView.setSelectorDrawable(ContextCompat.getDrawable(getContext(), R.drawable.wheel_dark));
        FLAG_SELECTOR = !FLAG_SELECTOR;
    }
}

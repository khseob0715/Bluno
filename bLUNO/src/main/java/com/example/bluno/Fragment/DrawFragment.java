package com.example.bluno.Fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluno.R;
import com.example.bluno.model.Save;
import com.example.bluno.views.CanvasView;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;


import java.util.List;

import static android.app.Activity.RESULT_OK;

public class DrawFragment extends Fragment {

    private boolean pickingColor = false;
    private ViewGroup body;
    private ViewGroup buttonlist;
    private ProgressBar progressbar;
    private CanvasView canvas = null;
    private DrawFragmentListener mListener;
    private Bundle tempSavedInstanceState = null;

    private String Tag = "DrawFragmenmt";
    public DrawFragment() {}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        if (canvas != null) {
            savedInstanceState.putInt("color", canvas.getColor());
            savedInstanceState.putInt("rows", canvas.getRows());
            savedInstanceState.putInt("columns", canvas.getColumns());
            savedInstanceState.putIntArray("pixels", canvas.toArray());

        } else if (tempSavedInstanceState != null) {
            // use old instancestate if turning display before canvas is loaded

            savedInstanceState.putInt("color", tempSavedInstanceState.getInt("color"));
            savedInstanceState.putInt("rows", tempSavedInstanceState.getInt("rows"));
            savedInstanceState.putInt("columns", tempSavedInstanceState.getInt("columns"));
            savedInstanceState.putIntArray("pixels", tempSavedInstanceState.getIntArray("pixels"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_draw, container, false);

        body = view.findViewById(R.id.body);
        buttonlist = view.findViewById(R.id.buttonlist);
        progressbar = view.findViewById(R.id.progress);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tempSavedInstanceState = savedInstanceState;
    }


    public void setColor(int color) {
        if (canvas != null)
            canvas.color = color;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState != null) {

            setColor(savedInstanceState.getInt("color"));

            if (savedInstanceState.get("pixels") != null) {
                final int[] pixelColors = savedInstanceState.getIntArray("pixels");
                final int rows = savedInstanceState.getInt("rows");
                final int columns = savedInstanceState.getInt("columns");
                // wait until body has been drawn
                body.post( new Runnable() {
                    @Override
                    public void run() {
                        generate(rows,columns,pixelColors);
                    }
                });
            }
        } else {

            // inform people of secondary tools
            //Toast.makeText(getActivity(), R.string.toolsmessage, Toast.LENGTH_SHORT).show();

            body.post(new Runnable() {
                @Override
                public void run() {
                    generate(10, 17);
                }
            });
        }



    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        // photo taken
        if (requestCode == 2 && resultCode == RESULT_OK) {

            // ask for rows & columns count
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
            dialog.setTitle(R.string.photoheight);
            dialog.setMessage(R.string.row);

            final LinearLayout ll = new LinearLayout(getContext());
            ll.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMarginStart(50);
            params.setMarginEnd(50);
            ll.setLayoutParams(params);

            // add slider
            final SeekBar input = new SeekBar(getContext());
            input.setProgress(20);
            input.setMax(37);
            ll.addView(input);

            // add checkbox
            final CheckBox checkbox = new CheckBox(getContext());

            checkbox.setLayoutParams(params);
            ll.addView(checkbox);

            dialog.setView(ll);

            dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // get camera sensor orientation
                    int orientation = 0;
                    CameraManager manager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
                    try {
                        int length = manager.getCameraIdList().length;
                        String cameraId = manager.getCameraIdList()[0 + (checkbox.isChecked() && length > 1 ? 1 : 0)];
                        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                        orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                    } catch (Exception e){}

                    // get picture
                    Bitmap photo = (Bitmap) data.getExtras().get("data");

                    // rotate photo based on picture orientation
                    Matrix matrix = new Matrix();
                    matrix.postRotate(orientation);
                    Bitmap rotatedPhoto = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);

                    // resize photo
                    int rows = input.getProgress() + 3;
                    double aspectratio = ( ((double) rotatedPhoto.getHeight()) / rotatedPhoto.getWidth()) > 1 ?
                            ( ((double) rotatedPhoto.getHeight()) / rotatedPhoto.getWidth()) :
                            ( ((double) rotatedPhoto.getWidth()) / rotatedPhoto.getHeight());
                    int columns = (int) (rows * aspectratio);
                    Bitmap scaledPhoto = Bitmap.createScaledBitmap(rotatedPhoto, rows, columns, false);

                    // convert to color array
                    int[] pixels = new int[scaledPhoto.getWidth()*scaledPhoto.getHeight()];
                    scaledPhoto.getPixels(pixels, 0, scaledPhoto.getWidth(), 0, 0, scaledPhoto.getWidth(), scaledPhoto.getHeight());
                    generate(scaledPhoto.getHeight(),scaledPhoto.getWidth(), pixels);
                }
            });
            dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            dialog.show();

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void rotate() {
        progressbar.setVisibility(View.VISIBLE);

        // rotate photo 90 degrees to the right
        new Thread(new Runnable() {
            public void run() {
                final TextView[][] rotated = canvas.getRotated();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        canvas.load(rotated);
                        progressbar.setVisibility(View.GONE);
                    }
                });
            }
        }).start();

    }

    public void generate(final int rows, final int columns) {
        generate(rows,columns,null);
    }

    public void generate(final int rows, final int columns, final int[] pixelColors) {
        // show loading spinner
        progressbar.setVisibility(View.VISIBLE);

        // remove old canvas
        if (body != null && canvas != null) {
            body.removeView(canvas);
            setColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(getContext(), R.color.colorAccent), 255));
        }

        if (getActivity() == null) return;

        // make canvas
        final CanvasView canvas = new CanvasView(getActivity());
        canvas.setOrientation(LinearLayout.VERTICAL);
        canvas.setBackgroundColor(Color.WHITE);
        canvas.setElevation(16);
        canvas.setTag(R.id.CANVAS_TAG, true);

        new Thread(new Runnable() {
            public void run() {
                canvas.generate(body,rows,columns,pixelColors);

                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressbar.setVisibility(View.INVISIBLE);
                        body.addView(canvas, 0);

                        // center canvas
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) canvas.getLayoutParams();
                        layoutParams.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
                        layoutParams.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
                        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                        canvas.setLayoutParams(layoutParams);

                        canvas.setOnTouchListener(new CanvasView.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                if (pickingColor) {
                                    int tempBrushSize = canvas.brushSize;
                                    canvas.brushSize = 1;
                                    List<View> pixels = canvas.findPixels(motionEvent);
                                    setColor( ((ColorDrawable) pixels.get(0).getBackground()).getColor() );
                                    canvas.brushSize = tempBrushSize;
                                    pickingColor = false;
                                    return true;
                                }
                                return false;
                            }
                        });

                        registerCanvas(canvas);
                    }
                });

            }
        }).start();

    }

    public CanvasView getCanvas() {
        return canvas;
    }

    private void registerCanvas(final CanvasView canvas) {
        this.canvas = canvas;
        tempSavedInstanceState = null;
    }



    public interface DrawFragmentListener {
        void onCanvasResetted();
        void onCanvasSaved(Save save);
        void openSaves();
    }

}


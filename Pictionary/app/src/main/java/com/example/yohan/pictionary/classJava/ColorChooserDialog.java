package com.example.yohan.pictionary.classJava;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;
import android.widget.ImageButton;

import com.example.yohan.pictionary.R;

import java.util.ArrayList;
import java.util.List;

public class ColorChooserDialog extends Dialog{
    public ColorChooserDialog(Context context) {
        super(context);
    }

    private GridLayout gridView;
    private List<Integer> colors;
    private List<ImageButton> buttons;

    private ColorListener myColorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_picker_dialog);
        gridView=((GridLayout)findViewById(R.id.panelColorView));

        buttons = new ArrayList<>();
        colors = new ArrayList<>();
        Resources res = getContext().getResources();
        int[] panel_color = res.getIntArray(R.array.item_panel);
        int j;
        for (j=0; j<panel_color.length; j++){
            colors.add(panel_color[j]);
            ImageButton imageButton = new ImageButton(getContext());
            buttons.add(imageButton);
            gridView.addView(imageButton);
        }
            Colorize();



        setListeners();

    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(myColorListener != null)
                myColorListener.OnColorClick(v, (int)v.getTag());
            dismiss();
        }
    };

    private void setListeners() {
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setTag(colors.get(i));
            buttons.get(i).setOnClickListener(listener);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void Colorize() {
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setColorFilter(colors.get(i));
            buttons.get(i).setMinimumWidth(58);
            buttons.get(i).setMinimumHeight(58);
            buttons.get(i).setVisibility(View.INVISIBLE);
            buttons.get(i).setBackgroundColor(colors.get(i));
        }
        animate();

    }




    private void animate(){




        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(getContext(),android.R.anim.fade_in);
                animation.setInterpolator(new AccelerateInterpolator());
                int i;
                for (i=0;i<buttons.size();i++){
                    buttons.get(i).setAnimation(animation);
                    buttons.get(i).setVisibility(View.VISIBLE);
                }
                animation.start();
            }
        };
        android.os.Handler handler = new android.os.Handler();
        int counter = 85;
        handler.postDelayed(r1,counter);
    }


    private void animator(final ImageButton imageButton){
        Animation animation = AnimationUtils.loadAnimation(getContext(),R.anim.color_item);
        animation.setInterpolator(new AccelerateInterpolator());
        imageButton.setAnimation(animation);
        imageButton.setVisibility(View.VISIBLE);
        animation.start();
    }




    public void setColorListener(ColorListener listener){
        this.myColorListener = listener;
    }



}

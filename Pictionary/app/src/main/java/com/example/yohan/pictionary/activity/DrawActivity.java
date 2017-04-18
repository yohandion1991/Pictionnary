package com.example.yohan.pictionary.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yohan.pictionary.R;

import com.example.yohan.pictionary.classJava.ActionClearBitmap;
import com.example.yohan.pictionary.classJava.ActionDessineChoix;
import com.example.yohan.pictionary.classJava.ActionMessage;
import com.example.yohan.pictionary.classJava.ActionWordToFound;
import com.example.yohan.pictionary.classJava.ActionPicture;
import com.example.yohan.pictionary.classJava.ActionRegister;
import com.example.yohan.pictionary.classJava.ColorChooserDialog;
import com.example.yohan.pictionary.classJava.ColorListener;
import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


import static android.widget.Toast.LENGTH_SHORT;

public class DrawActivity extends AppCompatActivity {
    private WebSocketClient mWebSocketClient;
    private SharedPreferences sp;
    private ActionRegister registerAction;
    private DrawingView dv;
    private Paint mPaint;
    private ArrayAdapter<String> adapterMessage;
    private ListView listView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_dessin);
        connectWebSocket();
        findViewById(R.id.buttonSendChat).setOnClickListener(sendListener);
        findViewById(R.id.fapColor).setOnClickListener(panelButtonListener);
        findViewById(R.id.clearBitmap).setOnClickListener(clearBitmap);

        //adapter message
        ArrayList<String> mListeMessage = new ArrayList<String>();
        adapterMessage = new ArrayAdapter(this, android.R.layout.simple_list_item_1, mListeMessage);
        listView = (ListView) findViewById(R.id.chat);
        listView.setAdapter(adapterMessage);




        int color = ContextCompat.getColor(this, R.color.colorVert);
        //Drawing
        dv = new DrawingView(this);
        ((FrameLayout) findViewById(R.id.frameDessin)).addView(dv);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebSocketClient.connect();
        //sp define
        sp = getSharedPreferences("nickName", MODE_PRIVATE);
        registerAction = new ActionRegister(sp.getString("nickName", ""));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebSocketClient.close();
        Intent result = new Intent();
        setResult(ConnexionActivity.RESULT_OK, result);
        finish();
    }

    private void connectWebSocket() {
        URI uri;
        String url;
        sp = getSharedPreferences("routeIP", MODE_PRIVATE);
        url = "ws://"+sp.getString("routeIP", "")+":8087";
        Log.i("URL",url);
        Log.i("ROUTE",sp.getString("routeIP", ""));
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                mWebSocketClient.send(new Gson().toJson(registerAction));
                Log.i("Websocket", "Opened");

            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                Gson gson = new Gson();
                JSONObject json = null;

                try {
                    json = new JSONObject(message);
                    switch (json.getString("action")) {
                        case "message":
                            ActionMessage am = gson.fromJson(message, ActionMessage.class);
                            final String mess = am.getMessage();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapterMessage.add("\n" + mess);
                                    listView.setSelection(listView.getScrollBarSize());
                                    listView.setFocusable(true);
                                }
                            });
                            break;
                        case "picture":
                            ActionPicture ap = gson.fromJson(message, ActionPicture.class);
                            final float positionX = ap.getPositionX();
                            final float positionY = ap.getPositionY();
                            final int colorDraw = ap.getColor();
                            final Boolean start = ap.getStart();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    drawByMeassage(start,positionX,positionY,colorDraw);
                                }
                            });
                            break;
                        case "dessin":
                            ActionDessineChoix ad = gson.fromJson(message, ActionDessineChoix.class);
                            final boolean messBolean = ad.getDessin();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (messBolean == false) {
                                        dv.setDessiner(false);
                                        findViewById(R.id.linearLayoutChat).setVisibility(View.VISIBLE);
                                        findViewById(R.id.answerTextView).setVisibility(View.GONE);
                                        findViewById(R.id.clearBitmap).setVisibility(View.GONE);
                                    } else {
                                        //Man Drawing don't have chat
                                        findViewById(R.id.linearLayoutChat).setVisibility(View.GONE);
                                        findViewById(R.id.answerTextView).setVisibility(View.VISIBLE);
                                        findViewById(R.id.clearBitmap).setVisibility(View.VISIBLE);
                                        drawerChoose();

                                    }
                                    dv.clear();
                                }
                            });
                           break;
                        case "clear":
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dv.clear();
                                }
                            });
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.i("JSON", "Mauvais JSON envoyer");
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Intent result = new Intent();
                setResult(ConnexionActivity.RESULT_OK, result);
                finish();
            }

            private void drawByMeassage(Boolean start, float positionX, float positionY, int colorDraw){
                mPaint.setColor(colorDraw);
                if (start == true) {
                    //evite la deformation
                    if (dv.getmBitmap().getWidth()>=dv.getmBitmap().getHeight()){
                        dv.touch_start(positionX * dv.getmBitmap().getHeight(), positionY * dv.getmBitmap().getHeight());
                    }else{
                        dv.touch_start(positionX * dv.getmBitmap().getWidth(), positionY * dv.getmBitmap().getWidth());
                    }
                } else {
                    if (dv.getmBitmap().getWidth()>=dv.getmBitmap().getHeight()){
                        dv.touch_move(positionX * dv.getmBitmap().getHeight(), positionY * dv.getmBitmap().getHeight());
                    }else{
                        dv.touch_move(positionX * dv.getmBitmap().getWidth(), positionY * dv.getmBitmap().getWidth());
                    }
                }
                dv.draw(dv.getmCanvas());
                dv.invalidate();
            }

            private void drawerChoose(){
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(DrawActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.alert_dialog_layout, null);
                final EditText mMot = (EditText) mView.findViewById(R.id.editTextValiderMots);
                final Button mValidate = (Button) mView.findViewById(R.id.validerButton);
                final Timer timer = new Timer();
                mBuilder.setView(mView);
                mBuilder.setCancelable(false);
                final AlertDialog dialog = mBuilder.create();
                timer.schedule(new firstTask(mMot, mValidate), 20000);
                mValidate.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (!mMot.getText().toString().isEmpty()) {
                            ActionWordToFound reponse = new ActionWordToFound(mMot.getText().toString());
                            mWebSocketClient.send(new Gson().toJson(reponse));
                            Toast toast = Toast.makeText(getApplicationContext(), "Faire deviner le mot: " + mMot.getText().toString(), Toast.LENGTH_LONG);
                            ((TextView)findViewById(R.id.answerTextView)).setText("Faire deviner le mot: " + mMot.getText().toString());
                            toast.show();
                            dialog.dismiss();
                            timer.cancel();
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Merci de mettre un mot", LENGTH_SHORT);
                            toast.show();
                        }

                    }
                });
                dialog.show();
                Toast toast = Toast.makeText(getApplicationContext(), "A toi de dessiner", LENGTH_SHORT);
                toast.show();
                dv.setDessiner(true);
            }
        };
    }


    private View.OnClickListener sendListener = new View.OnClickListener() {
        public void onClick(View v) {
            EditText editText = (EditText) findViewById(R.id.message);
            ActionMessage messageAction = new ActionMessage(editText.getText().toString());
            mWebSocketClient.send(new Gson().toJson(messageAction));
            editText.setText("");


        }
    };
    private View.OnClickListener panelButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            ColorChooserDialog dialog1 = new ColorChooserDialog(v.getContext());

            dialog1.setColorListener(new ColorListener() {
                @Override
                public void OnColorClick(View v, int color) {
                    mPaint.setColor(color);
                }
            });
            dialog1.show();

        }
    };

    private View.OnClickListener clearBitmap = new View.OnClickListener() {
        public void onClick(View v) {
            dv.clear();
            ActionClearBitmap messageAction = new ActionClearBitmap();
            mWebSocketClient.send(new Gson().toJson(messageAction));
        }
    };



    class firstTask extends TimerTask {
        private EditText text;
        private Button button;

        public firstTask(EditText text, Button button) {
            this.text = text;
            this.button = button;
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    text.setText("banane");
                    button.performClick();
                }
            });
        }
    }

    public class DrawingView extends View {

        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mBitmapPaint;
        Context context;
        private boolean dessiner = false;
        private boolean changeSize = true;

        public Canvas getmCanvas() {
            return mCanvas;
        }

        public Bitmap getmBitmap() {
            return mBitmap;
        }

        public void setDessiner(Boolean dessiner) {
            this.dessiner = dessiner;
        }


        public DrawingView(Context c) {
            super(c);
            context = c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            if (changeSize == true) {
                super.onSizeChanged(w, h, oldw, oldh);
                mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBitmap);
                changeSize = false;
            }

        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath(mPath, mPaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        protected void touch_start(float x, float y) {

            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
            if (dessiner == true) {
                ActionPicture ap;
                if (dv.getmBitmap().getWidth()>=dv.getmBitmap().getHeight()){
                    ap = new ActionPicture(mPaint.getColor(), x / mBitmap.getWidth(), y / mBitmap.getWidth(), true);
                }else{
                    ap = new ActionPicture(mPaint.getColor(), x / mBitmap.getHeight(), y / mBitmap.getWidth(), true);
                }
                mWebSocketClient.send(new Gson().toJson(ap));
            }


        }

        protected void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
                if (dessiner == true) {
                    ActionPicture ap;
                    if (dv.getmBitmap().getWidth()>=dv.getmBitmap().getHeight()){
                        ap = new ActionPicture(mPaint.getColor(), x / mBitmap.getWidth(), y / mBitmap.getWidth(), false);
                    }else{
                        ap = new ActionPicture(mPaint.getColor(), x / mBitmap.getHeight(), y / mBitmap.getWidth(), false);
                    }
                    mWebSocketClient.send(new Gson().toJson(ap));
                }


            }

        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            // commit the path to our offscreen
            mCanvas.drawPath(mPath, mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (dessiner == true) {
                float x = event.getX();
                float y = event.getY();

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        touch_start(x, y);
                        invalidate();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        touch_move(x, y);
                        invalidate();
                        break;
                    case MotionEvent.ACTION_UP:
                        touch_up();
                        invalidate();
                        break;
                }
            }
            return true;
        }

        public void clear() {
            mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            mCanvas.drawColor(Color.WHITE);
            invalidate();
        }

    }


}

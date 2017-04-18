package com.example.yohan.pictionary.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yohan.pictionary.R;


public class ConnexionActivity extends AppCompatActivity {
    EditText nickName;
    EditText routeIP;
    Button buttonConnexion;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    public final static int REQUEST_B3 = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connexion);


        //lie les boutton et edittext
        buttonConnexion = (Button) findViewById(R.id.buttonConnexion);
        buttonConnexion.setOnClickListener(connexionListener);
        nickName = (EditText) findViewById(R.id.nickNameEditTexte);
        routeIP = (EditText) findViewById(R.id.routeIPEditText);

        sp = getSharedPreferences("routeIP", MODE_PRIVATE);
        routeIP.setText(sp.getString("routeIP", "192.168.1.62"));


        sp = getSharedPreferences("nickName", MODE_PRIVATE);
        nickName.setText(sp.getString("nickName", ""));
        editor = sp.edit();


        connexion();
    }


    private View.OnClickListener connexionListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (nickName != null) {
                sp = getSharedPreferences("routeIP", MODE_PRIVATE);
                editor = sp.edit();
                editor.putString("routeIP", routeIP.getText().toString());
                editor.commit();

                sp = getSharedPreferences("nickName", MODE_PRIVATE);
                editor = sp.edit();
                editor.putString("nickName", nickName.getText().toString());
                editor.commit();

                connexion();
            }
        }
    };

    private void connexion() {
        if (sp.contains("nickName")) {
            Intent iner = new Intent(getApplicationContext(), DrawActivity.class);
            startActivityForResult(iner, REQUEST_B3);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_B3) {
            if (resultCode == DrawActivity.RESULT_OK) {
                NetworkInfo info = ((ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                if (info == null)
                {
                    Toast.makeText(getApplicationContext(), "Please check your internet connexion", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Server done please come back later", Toast.LENGTH_LONG).show();
                }
            }
        }

    }

}

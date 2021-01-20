package com.example.storeit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class splashView extends AppCompatActivity {
    private static byte count = 1;
    private TextView tv;
    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_view);

        tv = findViewById(R.id.tv_app_name);
        iv = findViewById(R.id.image_view_logo);
        View view = findViewById(R.id.view_black);

        Animation viewAnim = AnimationUtils.loadAnimation(splashView.this,R.anim.splash_view);

        Animation animation = AnimationUtils.loadAnimation(splashView.this,R.anim.splash_amin);
        tv.startAnimation(animation);
        iv.startAnimation(animation);
        view.startAnimation(viewAnim);
        if(!isDestroyed() && count < 2) {
            final Thread thread = new Thread() {

                public void run() {
                    try {
                        // 3 sec delay for splash screen
                        sleep(2050);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        Intent intent = new Intent(splashView.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            };
            thread.start();
            count++;
        }else{
            if(count > 1){
                count = 2;
            }
            Intent intent = new Intent(splashView.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
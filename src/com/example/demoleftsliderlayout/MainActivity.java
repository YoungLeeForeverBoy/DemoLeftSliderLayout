package com.example.demoleftsliderlayout;

import com.example.demoleftsliderlayout.LeftSliderLayout.OnLeftSliderLayoutStateListener;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
        implements OnLeftSliderLayoutStateListener, OnClickListener{
    public final static String TAG = "MainActivity";
    private HorizontalScrollView mHorizontalSv;
    private LeftSliderLayout mLeftSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHorizontalSv = (HorizontalScrollView)findViewById(R.id.horizontal_scroll_view);
        mLeftSlider = (LeftSliderLayout)findViewById(R.id.main_slider_layout);
        mLeftSlider.setOnLeftSliderLayoutStateListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.item1_tv: {
                Toast.makeText(this, ((TextView)v).getText().toString(),
                        Toast.LENGTH_SHORT).show();
            }break;

            case R.id.item2_tv: {
                Toast.makeText(this, ((TextView)v).getText().toString(),
                        Toast.LENGTH_SHORT).show();
            }break;

            case R.id.item3_tv: {
                Toast.makeText(this, ((TextView)v).getText().toString(),
                        Toast.LENGTH_SHORT).show();
            }break;

            case R.id.item4_tv: {
                Toast.makeText(this, ((TextView)v).getText().toString(),
                        Toast.LENGTH_SHORT).show();
            }break;

            case R.id.item5_tv: {
                Toast.makeText(this, ((TextView)v).getText().toString(),
                        Toast.LENGTH_SHORT).show();
            }break;

            case R.id.item6_tv: {
                Toast.makeText(this, ((TextView)v).getText().toString(),
                        Toast.LENGTH_SHORT).show();
            }break;

            case R.id.item7_tv: {
                Toast.makeText(this, ((TextView)v).getText().toString(),
                        Toast.LENGTH_SHORT).show();
            }break;

            case R.id.item8_tv: {
                Toast.makeText(this, ((TextView)v).getText().toString(),
                        Toast.LENGTH_SHORT).show();
            }break;

            case R.id.enable_btn: {
                mLeftSlider.enableSlide(true);
            }break;

            case R.id.disable_btn: {
                mLeftSlider.enableSlide(false);
            }break;

            case R.id.open_btn: {
                mLeftSlider.open();
            }break;

            case R.id.close_btn: {
                mLeftSlider.close();
            }break;
        }
    }

    @Override
    public void onLeftSliderLayoutStateChanged(boolean isOpen) {
        if(isOpen) {
            Toast.makeText(this, "LeftSlider is open!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "LeftSlider is close", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onLeftSliderLayoutInterceptTouch(MotionEvent ev) {
        if(isViewIntercept(mHorizontalSv, ev)) {
            return false;
        }

        return true;
    }

    private boolean isViewIntercept(View view, MotionEvent ev) {
        int location[] = new int[2];
        view.getLocationOnScreen(location);

        int viewLeft = location[0];
        int viewTop = location[1];

        if(ev.getRawX() > viewLeft && ev.getRawX() < viewLeft + view.getWidth()
                && ev.getRawY() > viewTop && ev.getRawY() < viewTop + view.getHeight()) {
            return true;
        }

        return false;
    }
}

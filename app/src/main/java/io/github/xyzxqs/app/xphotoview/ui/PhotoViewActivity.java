package io.github.xyzxqs.app.xphotoview.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;

import io.github.xyzxqs.app.xphotoview.R;
import io.github.xyzxqs.libs.xphotoview.XphotoView;

/**
 * @author xyzxqs (xyzxqs@gmail.com)
 */

public class PhotoViewActivity extends AppCompatActivity {
    private static final String TAG = "PhotoViewActivity";
    private XphotoView imageView;
    private View shawBackground;

    private int left, top, width, height;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);

        imageView = (XphotoView) findViewById(R.id.photo_image);
        shawBackground = findViewById(R.id.background_shadow);

        Intent intent = getIntent();
        String url = intent.getStringExtra("photo_url");
        left = intent.getIntExtra("left", 0);
        top = intent.getIntExtra("top", 0);
        width = intent.getIntExtra("width", 0);
        height = intent.getIntExtra("height", 0);

        imageView.setInitArgs(left, top, width, height, new XphotoView.Callback() {
            @Override
            public void onPreviewFinished() {
                finish();
                overridePendingTransition(0, 0);
            }

            @Override
            public void onReqUpdateBgAlpha(float alpha) {
                shawBackground.setAlpha(alpha);
            }
        });

        if (!TextUtils.isEmpty(url)) {
            Glide.with(this)
                    .load(url)
                    .into(imageView);
        }

    }

    @Override
    public void onBackPressed() {
//        finish();
        imageView.finishPhotoPreview();
    }
}

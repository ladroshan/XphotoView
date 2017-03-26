# XphotoView
XphotoView, a Android Custom ImageView Component with Google Photos Gestures

## Features

* Inherit from AppCompatImageView, all animation back support to minSdkVersion 9
* All Animation implemented by one matrix Animator: [ImageMatrixAnimator](xphotoview/src/main/java/io/github/xyzxqs/libs/xphotoview/ImageMatrixAnimator.java)
* Double tap or multi finger to zoom in/out, fling, slide to dismiss the preview, etc.
the gesture behavior more like Google Photos
* Long photo preview supported.

## Screenshots

<img src="/screenshots/xphotoview-demo.gif" alt="screenshot" title="xphotoview" width="270" height="486" />

## Getting started
With gradle:
```groovy
dependencies {
    compile 'io.github.xyzxqs.libs:xphotoview:1.1.0'
}
```

## Usage

As sample activity:
```java
public class PhotoViewActivity extends AppCompatActivity {
    private static final String TAG = "PhotoViewActivity";
    private XphotoView xphotoView;
    private View backgroundView;
 
    private int left, top, width, height;
 
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
 
        xphotoView = (XphotoView) findViewById(R.id.photo_image);
        backgroundView = findViewById(R.id.background_shadow);
 
        Intent intent = getIntent();
        String url = intent.getStringExtra("photo_url");
        left = intent.getIntExtra("left", 0);
        top = intent.getIntExtra("top", 0);
        width = intent.getIntExtra("width", 0);
        height = intent.getIntExtra("height", 0);
 
        //before photo laid out:
        xphotoView.setInitArgs(left, top, width, height, new XphotoView.Callback() {
            @Override
            public void onPreviewDismissed() {
                finish();
                overridePendingTransition(0, 0);
            }
            
            @Override
            public void onReqUpdateBgAlpha(float alpha) {
                backgroundView.setAlpha(alpha);
            }
        });
 
        //then load photo
        if (!TextUtils.isEmpty(url)) {
            Glide.with(this)
                    .load(url)
                    .into(xphotoView);
        }
 
    }
 
    @Override
    public void onBackPressed() {
        //if you want dismiss preview
        xphotoView.dismissPreview();
    }
}

```

start this activity:
```java
Intent i = new Intent(MainActivity.this, PhotoViewActivity.class);
i.putExtra("photo_url", url);
i.putExtra("left", left);
i.putExtra("top", top);
i.putExtra("width", width);
i.putExtra("height", height);
startActivity(i);
overridePendingTransition(0, 0);
```

License
-------
     Copyright 2017 xyzxqs (xyzxqs@gmail.com)

     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.

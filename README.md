# XphotoView
XphotoView, a Android Custom ImageView Component with Google Photos Gestures


* Inherit from AppCompatImageView, all animation back support to minSdkVersion 9
* All Animation implemented by one matrix Animator: [ImageMatrixAnimator](libxphotoview/src/main/java/io/github/xyzxqs/libs/xphotoview/ImageMatrixAnimator.java)
* Double tap or multi finger to zoom in/out, fling, slide to dismiss the preview, etc.
the gesture behavior more like Google Photos
* Long photo preview supported.

with gradle:
```groovy
dependencies {
    compile 'io.github.xyzxqs.libs:libxphotoview:1.0.1'
}
```

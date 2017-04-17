# XphotoView
XphotoView, a Android Custom ImageView Component with Google Photos Gestures

## Features

* Inherit from AppCompatImageView, all animation back support to minSdkVersion 9
* All Animation implemented by one matrix Animator: [ImageMatrixAnimator](xphotoview/src/main/java/io/github/xyzxqs/libs/xphotoview/ImageMatrixAnimator.java)
* Double tap or multi finger to zoom in/out, fling, slide to dismiss the preview, etc.
the gesture behavior more like Google Photos
* Long photo preview supported.

## Screenshots

<img src="/screenshots/xphotoview-1.gif" alt="screenshot" title="xphotoview" width="240" height="400" /> <img src="/screenshots/xphotoview-2.gif" alt="screenshot" title="xphotoview" width="240" height="400" />
<img src="/screenshots/xphotoview-3.gif" alt="screenshot" title="xphotoview" width="240" height="400" /> <img src="/screenshots/xphotoview-4.gif" alt="screenshot" title="xphotoview" width="240" height="400" />
<img src="/screenshots/xphotoview-5.gif" alt="screenshot" title="xphotoview" width="240" height="400" />

## Getting started
With gradle:
```groovy
dependencies {
    compile 'io.github.xyzxqs.libs:xphotoview:1.1.0'
}
```

## Usage

[PhotoViewActivity](/app/src/main/java/io/github/xyzxqs/app/xphotoview/ui/PhotoViewActivity.java)

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

package io.github.xyzxqs.app.xphotoview.ui;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;

import io.github.xyzxqs.app.xphotoview.R;
import io.github.xyzxqs.app.xphotoview.model.Post;
import io.github.xyzxqs.app.xphotoview.xrv.PostProvider;
import io.github.xyzxqs.libs.xrv.Items;

public class MainActivity extends AbsListActivity {

    private static final String TAG = "MainActivity";
    private Items items;

    @Override
    public void onViewCreated(@Nullable Bundle savedInstanceState) {
        items = new Items();
        adapter.register(new PostProvider(new PostProvider.Delegate() {
            @Override
            public void onContentClicked(int index) {
            }

            @Override
            public void onPhotoClicked(int itemIndex, int photoIndex, int left, int top, int width, int height) {
                Object obj = items.get(itemIndex);
                if (obj instanceof Post) {
                    Post post = ((Post) obj);
                    String url = post.photos.get(photoIndex);
                    Intent i = new Intent(MainActivity.this, PhotoViewActivity.class);
                    i.putExtra("photo_url", url);
                    i.putExtra("left", left);
                    i.putExtra("top", top);
                    i.putExtra("width", width);
                    i.putExtra("height", height);
                    startActivity(i);
                    overridePendingTransition(0, 0);
                }
            }

        }));

        initItems();
    }

    private void initItems() {
        Post post = new Post();
        post.photos = new ArrayList<>();
        post.photos.addAll(Arrays.asList(getResources().getStringArray(R.array.photos)));

        post.userName =getString(R.string.my_nickname);
        post.avatarUrl = getString(R.string.my_avatar_url);
        post.commentCount = 233;
        post.readCount = 1024;
        post.createTime = System.currentTimeMillis();
        post.postContent = getString(R.string.post_content);

        int i = 10;
        while (i-- > 0)
            items.add(post);

        adapter.setItems(items);
        adapter.notifyDataSetChanged();
    }
}

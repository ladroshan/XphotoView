package io.github.xyzxqs.app.xphotoview.xrv;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Date;
import java.util.Locale;

import io.github.xyzxqs.app.xphotoview.R;
import io.github.xyzxqs.app.xphotoview.model.Post;
import io.github.xyzxqs.libs.xrv.XrvAdapter;
import io.github.xyzxqs.libs.xrv.XrvProvider;

import static io.github.xyzxqs.app.xphotoview.$.dateFormat;


/**
 * @author xyzxqs (xyzxqs@gmail.com)
 */

public class PostProvider extends XrvProvider<Post, PostProvider.PostViewHolder> {

    private Delegate delegate;

    public PostProvider(Delegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public PostViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.item_post, parent, false);
        PostViewHolder holder = new PostViewHolder(view);
        holder.setDelegate(delegate);
        return holder;
    }


    @Override
    public void onBindViewHolder(PostViewHolder holder, Post itemData) {
        if (!TextUtils.isEmpty(itemData.avatarUrl)) {
            Glide.with(holder.itemView.getContext())
                    .load(itemData.avatarUrl)
                    .into(holder.avatarImage);
        }

        holder.userNameText.setText(itemData.userName);
        holder.createTimeText.setText(dateFormat.format(new Date(itemData.createTime)));
        holder.contentText.setText(itemData.postContent);
        holder.commentCountText.setText(String.format(Locale.getDefault(),
                "评论：%d", itemData.commentCount));
        holder.readCountText.setText(String.format(Locale.getDefault(),
                "关注：%d", itemData.readCount));

        if (itemData.photos != null && itemData.photos.size() > 0) {
            holder.photosContainer.setVisibility(View.VISIBLE);
            holder.adapter.setItems(itemData.photos);
            holder.adapter.notifyDataSetChanged();
        } else {
            holder.photosContainer.setVisibility(View.GONE);
        }

    }

    static class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView avatarImage;
        TextView userNameText;
        TextView createTimeText;
        TextView contentText;
        RecyclerView photosContainer;
        TextView commentCountText;
        TextView readCountText;

        XrvAdapter adapter;

        private Delegate delegate;

        public PostViewHolder(View itemView) {
            super(itemView);

            avatarImage = (ImageView) itemView.findViewById(R.id.user_avatar);
            userNameText = (TextView) itemView.findViewById(R.id.user_name);
            createTimeText = (TextView) itemView.findViewById(R.id.time_text);
            contentText = (TextView) itemView.findViewById(R.id.post_content);
            photosContainer = (RecyclerView) itemView.findViewById(R.id.photos_container);
            commentCountText = (TextView) itemView.findViewById(R.id.comment_count);
            readCountText = (TextView) itemView.findViewById(R.id.read_count);

            adapter = new XrvAdapter();
            PhotoProvider provider = new PhotoProvider();
            provider.setClickListener(new PhotoProvider.OnPhotoClickListener() {
                @Override
                public void onPhotoClick(int index, int left, int top, int width, int height) {
                    delegate.onPhotoClicked(getAdapterPosition(), index, left, top, width, height);
                }
            });
            adapter.register(provider);

            photosContainer.setAdapter(adapter);
            photosContainer.setLayoutManager(new GridLayoutManager(itemView.getContext(), 3));

            contentText.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            delegate.onContentClicked(getAdapterPosition());
        }

        public void setDelegate(Delegate delegate) {
            this.delegate = delegate;
        }
    }

    public static interface Delegate {
        void onContentClicked(int index);

        void onPhotoClicked(int itemIndex, int photoIndex, int left, int top, int width, int height);
    }
}

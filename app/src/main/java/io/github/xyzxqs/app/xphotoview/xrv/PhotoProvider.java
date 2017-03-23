package io.github.xyzxqs.app.xphotoview.xrv;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import io.github.xyzxqs.app.xphotoview.R;
import io.github.xyzxqs.libs.xrv.XrvProvider;

/**
 * @author xyzxqs (xyzxqs@gmail.com)
 */

public class PhotoProvider extends XrvProvider<String, PhotoProvider.PhotoViewHolder> {

    private OnPhotoClickListener clickListener;

    @Override
    public PhotoViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup parent) {
        PhotoViewHolder holder = new PhotoViewHolder(inflater.inflate(R.layout.item_image, parent, false));
        holder.setOnClickListener(clickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final PhotoViewHolder holder, String itemData) {
        if (!TextUtils.isEmpty(itemData)) {
            Glide.with(holder.itemView.getContext())
                    .load(itemData)
                    .into(holder.photo);
        } else {
            holder.itemView.setVisibility(View.GONE);
        }
    }

    public void setClickListener(OnPhotoClickListener clickListener) {
        this.clickListener = clickListener;
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView photo;
        private OnPhotoClickListener listener;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            photo = (ImageView) itemView.findViewById(R.id.photo_image);
            photo.setOnClickListener(this);
        }

        public void setOnClickListener(OnPhotoClickListener listener) {
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                int[] loc = new int[2];
                photo.getLocationOnScreen(loc);
                listener.onPhotoClick(getAdapterPosition(), loc[0], loc[1], photo.getWidth(), photo.getHeight());
            }
        }
    }

    public interface OnPhotoClickListener {
        void onPhotoClick(int index, int left, int top, int width, int height);
    }
}

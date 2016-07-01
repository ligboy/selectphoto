package org.ligboy.selectphoto.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bumptech.glide.Glide;

import org.ligboy.selectphoto.SelectImageActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AppCompatImageView mPhotoImage;
    private AppCompatTextView mTitleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPhotoImage = (AppCompatImageView) findViewById(R.id.photo);
        mTitleText = (AppCompatTextView) findViewById(R.id.title);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        mPhotoImage.getDrawable();
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                SelectImageActivity.builder()
                        .setTitle("Select a photo")
                        .asSquare()
                        .setFixAspectRatio(true)
                        .withMaxSize(1024, 1024)
                        .withCrop(true)
                        .start(MainActivity.this);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SelectImageActivity.REQUEST_CODE_SELECT_IMAGE
                && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Glide.with(this).load(data.getData()).crossFade().into(mPhotoImage);
            updatePalette(data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updatePalette(@NonNull Intent data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outHeight = options.outWidth = getResources().getDisplayMetrics().widthPixels;
        Bitmap bitmap = BitmapFactory.decodeFile(data.getData().getPath(), options);
        if (bitmap != null && !bitmap.isRecycled()) {
            Palette.Builder builder = new Palette.Builder(bitmap);
            builder.maximumColorCount(5);
            builder.generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    if (mTitleText != null) {
                        Palette.Swatch swatch = palette.getMutedSwatch();
                        if (swatch != null) {
                            mTitleText.setTextColor(swatch.getTitleTextColor());
                            mTitleText.setBackgroundColor(swatch.getRgb() & 0xF0FFFFFF);
                        }
                    }
                }
            });
        }
    }
}

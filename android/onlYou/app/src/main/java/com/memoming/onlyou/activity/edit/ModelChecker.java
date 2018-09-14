package com.memoming.onlyou.activity.edit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Camera;
import android.view.WindowManager;

import com.memoming.onlyou.activity.camera.CameraActivity;
import com.memoming.onlyou.activity.model.ModelCameraActivity;
import com.memoming.onlyou.serverUtil.ServerTask;

public class ModelChecker {

    private Context mContext;

    public ModelChecker(Context context) {
        mContext = context;
    }

    public void exec() {
        new com.memoming.onlyou.activity.edit.ModelChecker.CheckModelTask().execute("http://117.16.44.14:8055/post");
    }

    private class CheckModelTask extends ServerTask {

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Checking Server ...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
            ((Activity)mContext).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDialog.dismiss();
            ((Activity)mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            changeIntent(Integer.parseInt(result) != 0);
        }
    }

    private void changeIntent(Boolean hasModel) {
        if( !hasModel ) initDialog();
        else {
            Intent compareIntent = new Intent(mContext, Compare.class);
            mContext.startActivity(compareIntent);
        }
    }

    private void initDialog() {
        AlertDialog.Builder alertNewModel = new AlertDialog.Builder(mContext);
        alertNewModel.setTitle("새 모델 생성");
        alertNewModel
                .setMessage("얼굴 모델이 존재하지 않습니다. \n새로 생성 하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("새 모델생성", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent nextIntent = new Intent(mContext, ModelCameraActivity.class);
                        mContext.startActivity(nextIntent);
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(
                            DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).show();
    }
}


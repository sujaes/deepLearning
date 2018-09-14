package com.memoming.onlyou.activity.model;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.WindowManager;

import com.memoming.onlyou.activity.main.MainActivity;
import com.memoming.onlyou.serverUtil.ServerTask;

public class ModelMaker {

    private Context mContext;
    private ModelCameraActivity mModelCameraActivity = (ModelCameraActivity)ModelCameraActivity.mModelCameraActivity;

    public ModelMaker(Context context) {
        mContext = context;
    }

    public void exec() {
        new ModelMakerTask().execute("http://117.16.44.14:8053/post");
    }

    private class ModelMakerTask extends ServerTask {

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage(" 얼굴 모델을 만드는 중입니다...\n잠시만 기다려 주세요 !");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

            ((Activity) mContext).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDialog.dismiss();
            ((Activity) mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            showMessage();
        }
    }

    private void showMessage() {
        AlertDialog.Builder alertNewModel = new AlertDialog.Builder(mContext);
        alertNewModel.setTitle("OnlyYou");
        alertNewModel
                .setMessage("모델 생성이 완료 되었습니다 !")
                .setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        Intent nextIntent = new Intent(mContext, MainActivity.class);
//                        mContext.startActivity(nextIntent);
                        mModelCameraActivity.finish();
                    }
                }).show();
    }
}

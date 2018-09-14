package com.memoming.onlyou.activity.model;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.WindowManager;

import com.memoming.onlyou.serverUtil.ServerTask;

public class ModelRemover {

    private Context mContext;

    public ModelRemover(Context context) {
        mContext = context;
    }

    public void exec() {
        new ModelRemoverTask().execute("http://117.16.44.14:8056/post");
    }

    private class ModelRemoverTask extends ServerTask {

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage(" Server 초기화 중 입니다...");
            mProgressDialog.show();
            ((Activity)mContext).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDialog.dismiss();
            ((Activity)mContext).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            showMessage();
        }
    }


    private void showMessage() {
        AlertDialog.Builder alertNewModel = new AlertDialog.Builder(mContext);
        alertNewModel.setTitle("OnlyYou");
        alertNewModel
                .setMessage("준비 되었습니다 !\n동영상 촬영을 누르고\n 얼굴을 움직여 앞, 왼쪽, 오른쪽, 위, 아래 등을 30초동안 촬영 해주세요")
                .setCancelable(false)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent nextIntent = new Intent(mContext, ModelCameraActivity.class);
                        mContext.startActivity(nextIntent);
                    }
                }).show();
    }
}

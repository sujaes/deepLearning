package com.memoming.onlyou.activity.model;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.memoming.onlyou.serverUtil.ServerTask;

public class ModelChecker {

    private Context mContext;

    public ModelChecker(Context context) {
        mContext = context;
    }

    public void exec() {
        new CheckModelTask().execute("http://117.16.44.14:8055/post");
    }

    private class CheckModelTask extends ServerTask {

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Checking Server ...");
            mProgressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDialog.dismiss();
            changeIntent(Integer.parseInt(result) != 0);
        }
    }

    private void changeIntent(Boolean hasModel) {
        System.out.println(hasModel);
        if( hasModel ) initDialog();
        else {
            Intent nextIntent = new Intent(mContext, ModelCameraActivity.class);
            mContext.startActivity(nextIntent);
        }
    }

    private void initDialog() {
        AlertDialog.Builder alertNewModel = new AlertDialog.Builder(mContext);
        alertNewModel.setTitle("새 모델 생성");
        alertNewModel
                .setMessage("기존 모델이 존재합니다. \n기존 모델을 지우고 새로 생성 하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("새 모델생성", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new ModelRemover(mContext).exec();
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

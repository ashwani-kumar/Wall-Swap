package com.wallpaper.demo;

import com.chute.android.multiimagepicker.app.MultiImagePickerActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class WallSwapActivity extends Activity
{
  public int mChangeTime = 60;
  private RadioButton mFiveMinRadio;
  private Button mGalleryButton;
  private RadioButton mOneHourRadio;
  private RadioButton mOneMinRadio;
 // private String mSelectedImgPath = "";
  private Button mSetButton;
  private RadioButton mTenMinRadio;
  private RadioButton mThirtyMinRadio;
  private RadioGroup mTimeRadioGroup;
  private Button mUnsetButton;

//  protected void onActivityResult(int requestCode, int resultCode, Intent data){
//  	if (resultCode == RESULT_OK) {
//          mSelectedImgPath = data.getStringExtra("imagePath"); 
//          Log.i("Selected Images", mSelectedImgPath);
//  	}
//  }

  public void onCreate(Bundle paramBundle)
  {
    super.onCreate(paramBundle);
    setContentView(R.layout.main);
    mSetButton = (Button)findViewById(R.id.button1);
    mUnsetButton = (Button)findViewById(R.id.button2);
    mGalleryButton = (Button)findViewById(R.id.button3);
    mOneMinRadio = (RadioButton)findViewById(R.id.radio0);
    mFiveMinRadio = (RadioButton)findViewById(R.id.radio1);
    mTenMinRadio = (RadioButton)findViewById(R.id.radio2);
    mThirtyMinRadio = (RadioButton)findViewById(R.id.radio3);
    mOneHourRadio = (RadioButton)findViewById(R.id.radio4);
    mTimeRadioGroup = (RadioGroup)findViewById(R.id.radioGroup1);

    this.mUnsetButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        Intent localIntent = new Intent(WallSwapActivity.this, WallpaperChangeService.class);
        WallSwapActivity.this.stopService(localIntent);
        WallSwapActivity.this.finish();
      }
    });
        this.mSetButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
    	int i = WallSwapActivity.this.mTimeRadioGroup.getCheckedRadioButtonId();
        if (WallSwapActivity.this.mOneMinRadio.getId() == i)
          WallSwapActivity.this.mChangeTime = 60;
        if (WallSwapActivity.this.mFiveMinRadio.getId() == i)
          WallSwapActivity.this.mChangeTime = 300;
        if (WallSwapActivity.this.mTenMinRadio.getId() == i)
          WallSwapActivity.this.mChangeTime = 600;
        if (WallSwapActivity.this.mThirtyMinRadio.getId() == i)
          WallSwapActivity.this.mChangeTime = 1800;
        if (WallSwapActivity.this.mOneHourRadio.getId() == i)
          WallSwapActivity.this.mChangeTime = 3600;
        Intent localIntent = new Intent(WallSwapActivity.this, WallpaperChangeService.class);
        Bundle localBundle = new Bundle();
        localBundle.putInt("time", WallSwapActivity.this.mChangeTime);
        //localBundle.putString("path", WallSwapActivity.this.mSelectedImgPath);
        localIntent.putExtras(localBundle);
        WallSwapActivity.this.startService(localIntent);
        WallSwapActivity.this.finish();
      }
    });
    this.mGalleryButton.setOnClickListener(new View.OnClickListener()
    {
      public void onClick(View paramAnonymousView)
      {
        Intent localIntent = new Intent(WallSwapActivity.this, MultiImagePickerActivity.class);
        WallSwapActivity.this.startActivityForResult(localIntent, 1370908);
      }
    });
  }

  public void onStart()
  {
    super.onStart();
    LongOperation localLongOperation = new LongOperation(this);
    String[] arrayOfString = new String[1];
    arrayOfString[0] = "";
    localLongOperation.execute(arrayOfString);
  }

  private class LongOperation extends AsyncTask<String, Void, String>
  {
    private Activity activity;
    private Context context;
    private ProgressDialog dialog;

    public LongOperation(Activity arg2)
    {
      this.activity = arg2;
      this.context = arg2;
      this.dialog = new ProgressDialog(this.context);
    }

    protected String doInBackground(String... param)
    {
      sendBroadcast(new Intent("android.intent.action.MEDIA_MOUNTED", Uri.parse("file://" + Environment.getExternalStorageDirectory())));
      return "";
    }

    protected void onPostExecute(String paramString)
    {
      if (this.dialog.isShowing())
        this.dialog.dismiss();
    }

    protected void onPreExecute()
    {
      this.dialog.setMessage("Veriying SDCard");
      this.dialog.show();
    }

    protected void onProgressUpdate(Void... param)
    {
    }
  }
}
package com.wxm.camerajob.ui.acinterface;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.wxm.camerajob.R;
import com.wxm.camerajob.base.data.GlobalDef;
import com.wxm.camerajob.base.utility.ContextUtil;

public class ACHelp extends AppCompatActivity {
    private String HELP_MAIN_FILEPATH = "file:///android_asset/help_main.html";
    private static String TAG = "ACHelp";
    private static final String ENCODING = "utf-8";
    //private static final String MIMETYPE = "text/html; charset=UTF-8";

    private WebView     mWVHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_help);
        ContextUtil.getInstance().addActivity(this);

        mWVHelp = (WebView)findViewById(R.id.ac_help_webvw);
        assert mWVHelp != null;

        WebSettings wSet = mWVHelp.getSettings();
        wSet.setDefaultTextEncodingName(ENCODING);

        // load help html
        Intent it = getIntent();
        String help_type = it.getStringExtra(GlobalDef.STR_HELP_TYPE);
        if(null != help_type)   {
            switch (help_type)  {
                case GlobalDef.STR_HELP_MAIN :  {
                    load_main_help();
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.acm_help_actbar, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.meuitem_help_leave: {
                Intent data = new Intent();
                setResult(GlobalDef.INTRET_NOTCARE, data);
                finish();
            }
            break;

            default:
                return super.onOptionsItemSelected(item);

        }

        return true;
    }



    /**
     * 加载应用主帮助信息
     */
    private void load_main_help()   {
        mWVHelp.loadUrl(HELP_MAIN_FILEPATH);
    }
}

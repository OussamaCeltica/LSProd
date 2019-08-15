package com.leadersoft.celtica.lsprod;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class DeviceConfigScanner extends AppCompatActivity {
    private ZXingScannerView mScannerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_config_scanner);
        if (savedInstanceState != null) {
            //region Revenir a au Accueil ..
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        }else {

            mScannerView = new ZXingScannerView(this); // Programmatically initialize the scanner view

            setContentView(mScannerView);
            mScannerView.startCamera();

            mScannerView.setResultHandler(new ZXingScannerView.ResultHandler() {
                @Override
                public void handleResult(Result result) {
                    // Log.e("code"," "+result.getText());
                    mScannerView.stopCamera();
                    Intent i = new Intent();
                    i.putExtra("code", "" + result.getText());
                    setResult(RESULT_OK, i);
                    finish();

                }
            }); // Register ourselves as a handler for scan results.
        }
    }
}

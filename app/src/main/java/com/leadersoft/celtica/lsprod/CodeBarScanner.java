package com.leadersoft.celtica.lsprod;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.FrameLayout;

import com.google.zxing.Result;
import com.leadersoft.celtica.lsprod.Preparations.FairePrep;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class CodeBarScanner extends AppCompatActivity {


    private ZXingScannerView mScannerView;
    private FrameLayout camera_body;
    private CheckBox check_incr;
    public static REQUEST_SCANNER requestScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState != null) {
            //region Revenir a au Deviceconfig ..
            Intent intent = new Intent(getApplicationContext(), DeviceConfig.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        } else {
            //region check camera permission ..
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(CodeBarScanner.this, new String[]{Manifest.permission.CAMERA}, 8);

                Log.e("ccc","non ");

            }
            //endregion
            else {

                //region afficher le layout de scanner Incre
                if(getIntent().getExtras() != null) {
                    setContentView(R.layout.activity_codebar_scanner);
                    mScannerView=(ZXingScannerView)findViewById(R.id.camera_scanner);
                    camera_body=(FrameLayout)findViewById(R.id.camera_body);
                    check_incr=(CheckBox)findViewById(R.id.camera_scanner_check_incr);

                }
                //endregion

                //region aficher le layout d un seul scan ..
                else {

                    mScannerView = new ZXingScannerView(this); // Programmatically initialize the scanner view
                    setContentView(mScannerView);
                 }
                //endregion

                mScannerView.startCamera();

                //region on scann result ..
                mScannerView.setResultHandler(new ZXingScannerView.ResultHandler() {
                    @Override
                    public void handleResult(Result result) {
                        // Log.e("code"," "+result.getText());
                        mScannerView.resumeCameraPreview(this);

                        //test si scann avec incrementation
                        if(getIntent().getExtras() != null){

                            if (!check_incr.isChecked()){
                                FairePrep.withIncr=false;
                                Intent i = new Intent();
                                i.putExtra("code", "" + result.getText());
                                setResult(RESULT_OK, i);
                                finish();
                            }else {
                                FairePrep.withIncr=true;
                                if (FairePrep.me.checkPrExiste(result.getText())) {
                                    DeviceConfig.session.changeColorOnScan(camera_body, getResources().getColor(R.color.Green), getResources().getColor(R.color.White));
                                    FairePrep.me.addPrToPanier(result.getText(), 1);
                                } else {
                                    DeviceConfig.session.changeColorOnScan(camera_body, getResources().getColor(R.color.Red), getResources().getColor(R.color.White));
                                }
                            }

                        }else {
                            Intent i = new Intent();
                            i.putExtra("code", "" + result.getText());
                            setResult(RESULT_OK, i);
                            finish();
                        }


                    }
                });
                //endregion



            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera(); // Stop camera on pause
    }


    }

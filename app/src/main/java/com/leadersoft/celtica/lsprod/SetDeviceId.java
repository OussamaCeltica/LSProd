package com.leadersoft.celtica.lsprod;

import android.content.Intent;
import android.database.sqlite.SQLiteStatement;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class SetDeviceId extends AppCompatActivity {
    EditText deviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_device_id);
        if (savedInstanceState != null) {
            //region Revenir a au Accueil ..
            Intent intent = new Intent(getApplicationContext(), Accueil.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            //endregion
        }else {
            deviceName=(EditText)findViewById(R.id.deviceName);
            ((TextView)findViewById(R.id.validerDeviceName)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (deviceName.getText().toString().equals("")){

                    }else {
                        Accueil.bd.write2("update admin set device_name=?", new MyBD.SqlPrepState() {
                            @Override
                            public void putValue(SQLiteStatement stmt) {
                                stmt.bindString(1,deviceName.getText().toString()+"");
                                stmt.execute();
                            }
                        });

                        Intent intent = new Intent(getApplicationContext(), Accueil.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }
            });
        }
    }
}

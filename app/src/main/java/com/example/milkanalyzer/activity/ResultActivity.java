package com.example.milkanalyzer.activity;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;
import com.dantsu.escposprinter.textparser.PrinterTextParserImg;
import com.example.milkanalyzer.AppManager;
import com.example.milkanalyzer.firebase.FireBaseHelper;
import com.example.milkanalyzer.firebase.FireBaseMilkHelper;
import com.example.milkanalyzer.object.TakenMilk;
import com.example.milkanalyzer.object.User;
import com.example.milkanalyzer.R;
import com.example.milkanalyzer.databinding.ActivityResultBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.WriterException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class ResultActivity extends AppCompatActivity {

    private ActivityResultBinding activityResultBinding;
    private User user;
    private final static int CONNECTING_STATUS = 1;
    private final static int MESSAGE_READ = 2;
    private int progresCount = 0;
    private boolean isAnaliz = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityResultBinding = DataBindingUtil.setContentView(this, R.layout.activity_result);
        user = AppManager.getUser();
        activityResultBinding.save.setOnClickListener(view -> {
            String takenString = activityResultBinding.takenMilk.getText().toString();
            if (takenString.trim().length() != 0){
                String pattern = "MM.dd.yyyy HH:mm";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String takenDate = simpleDateFormat.format(new Date());
                user.setLastTakenDate(takenDate);
                String takenMilk = String.valueOf(Integer.parseInt(user.getTakenMilk()) + Integer.parseInt(takenString));
                user.setTakenMilk(takenMilk);
                user.setLastTakenMilk(takenString);
                FireBaseHelper fireBaseHelper = new FireBaseHelper();
                fireBaseHelper.addUser(user.getId(), user).addOnSuccessListener(suc ->{
                    FireBaseMilkHelper fireBaseMilkHelper = new FireBaseMilkHelper();
                    fireBaseMilkHelper.addMilkInformation(UUID.randomUUID().toString(), new TakenMilk(user.getId(), takenString, takenDate)).addOnSuccessListener(res ->{
                        Toast.makeText(ResultActivity.this, "İşlem Başarılı.", Toast.LENGTH_LONG).show();

                    }).addOnFailureListener(fail ->{
                        Toast.makeText(ResultActivity.this, "Hata.", Toast.LENGTH_LONG).show();
                    });
                }).addOnFailureListener(fail ->{
                    Toast.makeText(ResultActivity.this, "Hata.", Toast.LENGTH_LONG).show();
                });

            } else {
                activityResultBinding.takenMilk.requestFocus();
                Toast.makeText(ResultActivity.this, "Alınan süt miktarını giriniz.", Toast.LENGTH_LONG).show();
            }
        });
        activityResultBinding.buttonToggle.setOnClickListener(view -> {
            progresCount = 0;
            activityResultBinding.progressBar.setProgress(progresCount);
            activityResultBinding.progressLayout.setVisibility(View.VISIBLE);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (progresCount <= 100) {
                        activityResultBinding.progressText.setText("" + progresCount);
                        activityResultBinding.progressBar.setProgress(progresCount);
                        progresCount = progresCount + 1;
                        handler.postDelayed(this, 95);
                    } else {
                        handler.removeCallbacks(this);
                    }
                }
            }, 95);
            String cmdText = null;
            cmdText = "start\n";
            AppManager.getConnectedThread().write(cmdText);
        });
        activityResultBinding.print.setOnClickListener(view -> {
            if(isAnaliz)
                getPrinter();
            else
                Toast.makeText(ResultActivity.this, "Ölçüm Yapılmamış.", Toast.LENGTH_LONG).show();
        });
        initView();
        AppManager.getConnectedThread().setHandler(new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                break;
                            case -1:
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String readMessage = msg.obj.toString(); // Read message from Arduino
                        getData(readMessage.split(":"));
                        break;
                }
            }
        });
    }

    private void initView(){
        activityResultBinding.textViewName.setText(user.getFullName());
        activityResultBinding.textViewAddress.setText(user.getAddress());
        activityResultBinding.textViewId.setText(user.getId());
        activityResultBinding.textViewDate.setText(user.getLastTakenDate());
        activityResultBinding.targetMilk.setText(user.getTargetMilk());
        activityResultBinding.totalTakenMilk.setText(user.getTakenMilk());
        activityResultBinding.textViewVillage.setText(user.getVillageName() + " - " + user.getVillageNo());
        activityResultBinding.lastTakenMilk.setText(user.getLastTakenMilk());
    }

    private void getPrinter(){
        Bitmap bitmap = null;
        QRGEncoder qrgEncoder = new QRGEncoder(user.getId(), null, QRGContents.Type.TEXT, 256);
        try {
            bitmap = qrgEncoder.encodeAsBitmap();
            //qrCodeIV.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.e("Tag", e.toString());
        }
        int takenMilk = Integer.parseInt(user.getTargetMilk()) - Integer.parseInt(activityResultBinding.takenMilk.getText().toString());
        EscPosPrinter printer = AppManager.getCreatePrinter().getPointer();
        if (printer != null){

            try {
                printer
                        .printFormattedText(
                                "[L]\n" +
                                "[C]<u><font size='big'>Süt Analizi</font></u>\n" +
                                "[L]\n" +
                                "[C]================================\n" +
                                "[R]Toplam Süt Borcu:[R]"+ user.getTargetMilk() + "\n" +
                                "[R]Alinan :[R]" + activityResultBinding.takenMilk.getText().toString() + "\n" +
                                "[R]Kalan Borc :[R]" + takenMilk + "\n" +
                                "[L]\n" +
                                "[C]================================\n" +
                                "[L]\n" +
                                "[L]<font size='tall'>Kisi bilgileri :</font>\n" +
                                "[L]Köy ismi:" + user.getVillageName() +"\n" +
                                "[L]Köy No:" +user.getVillageNo() +"\n" +
                                "[L]Ad Soyad:" + user.getFullName() + "\n" +
                                "[L]Kullanici No:" + user.getId() +"\n" +
                                "[L]\n" +
                                        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, bitmap)+"</img>\n"
                        );
            } catch (EscPosConnectionException e) {
                e.printStackTrace();
            } catch (EscPosParserException e) {
                e.printStackTrace();
            } catch (EscPosEncodingException e) {
                e.printStackTrace();
            } catch (EscPosBarcodeException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(ResultActivity.this, "Yazıcının bağlı olduğundan emin olun.", Toast.LENGTH_LONG).show();
        }
    }

    public void getData(String[] strings) {
        if ("rfid".equals(strings[0])) {
            FireBaseHelper fireBaseHelper = new FireBaseHelper();
            fireBaseHelper.databaseReference.child(user.getId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    AppManager.setUser(user);
                    /*Intent myIntent = new Intent(ResultActivity.this, ResultActivity.class);
                    myIntent.putExtra("userId", "");
                    MainActivity.this.startActivity(myIntent);*/
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        } else if ("avarage".equals(strings[0])) {
            isAnaliz = true;
            activityResultBinding.progressLayout.setVisibility(View.GONE);
            activityResultBinding.avarage.setText(strings[1]);
            activityResultBinding.print.setImageDrawable(getDrawable(R.drawable.printer_online));
            activityResultBinding.buttonToggle.setText("Tekrar Ölçüm Yap");
            activityResultBinding.save.setEnabled(true);
        }
    }

}
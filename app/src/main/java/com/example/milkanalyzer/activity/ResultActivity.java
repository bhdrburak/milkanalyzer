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
import com.example.milkanalyzer.object.ValueModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
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
    private ValueModel mValueModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityResultBinding = DataBindingUtil.setContentView(this, R.layout.activity_result);
        activityResultBinding.save.setOnClickListener(view -> {
            String takenString = activityResultBinding.takenMilk.getText().toString();
            if (takenString.trim().length() != 0) {
                String pattern = "dd.MM.yyyy HH:mm";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
                String takenDate = simpleDateFormat.format(new Date());
                user.setLastTakenDate(takenDate);
                String takenMilk = String.valueOf(Double.parseDouble(user.getTakenMilk()) + Double.parseDouble(takenString));
                user.setTakenMilk(takenMilk);
                user.setLastTakenMilk(takenString);
                FireBaseHelper fireBaseHelper = new FireBaseHelper();
                fireBaseHelper.addUser(user.getId(), user).addOnSuccessListener(suc -> {
                    FireBaseMilkHelper fireBaseMilkHelper = new FireBaseMilkHelper();
                    TakenMilk takMilk = new TakenMilk();
                    takMilk.setUserId(user.getId());
                    takMilk.setTakenMilk(takenString);
                    takMilk.setTakenMilkDate(takenDate);
                    fireBaseMilkHelper.addMilkInformation(UUID.randomUUID().toString(), takMilk).addOnSuccessListener(res -> {
                        Toast.makeText(ResultActivity.this, "İşlem Başarılı.", Toast.LENGTH_LONG).show();

                    }).addOnFailureListener(fail -> {
                        Toast.makeText(ResultActivity.this, "Hata.", Toast.LENGTH_LONG).show();
                    });
                }).addOnFailureListener(fail -> {
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
            activityResultBinding.resultView.setVisibility(View.GONE);
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
                        /*String resultString = "data:{fateRate:\"4.1\", laktozRate:\"2.7\", waterRate:\"1.1\", snfRate:\"3.3\", proteinRate:\"6.6\", saltRate:\"5.1\", weight:\"8.0\"}";
                        getData(resultString);*/
                        handler.removeCallbacks(this);
                    }
                }
            }, 95);
            String cmdText = null;
            cmdText = "start\n";
            AppManager.getConnectedThread().write(cmdText);
        });
        activityResultBinding.print.setOnClickListener(view -> {
            if (isAnaliz)
                getPrinter();
            else
                Toast.makeText(ResultActivity.this, "Ölçüm Yapılmamış.", Toast.LENGTH_LONG).show();
        });
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
                        getSensorData(readMessage);
                        break;
                }
            }
        });
        getData();
    }

    private void getData() {
        FireBaseHelper fireBaseHelper = new FireBaseHelper();
        fireBaseHelper.databaseReference.child(AppManager.getUserId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                initView();

                /*String resultString = "data:{fateRate:\"4.1\", laktozRate:\"2.7\", waterRate:\"1.1\", snfRate:\"3.3\", proteinRate:\"6.6\", saltRate:\"5.1\", weight:\"8.0\"}";
                getData(resultString);*/
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("TAG", "onCancelled: ");
            }
        });
    }

    private void initView() {
        activityResultBinding.save.setEnabled(false);
        activityResultBinding.takenMilk.getText().clear();
        activityResultBinding.textViewName.setText(user.getFullName());
        activityResultBinding.textViewAddress.setText(user.getAddress());
        activityResultBinding.textViewId.setText(user.getId());
        activityResultBinding.textViewDate.setText(user.getLastTakenDate());
        activityResultBinding.targetMilk.setText(user.getTargetMilk() + " Litre");
        activityResultBinding.totalTakenMilk.setText(user.getTakenMilk() + " Litre");
        activityResultBinding.textViewVillage.setText(user.getVillageName() + " - " + user.getVillageNo());
        activityResultBinding.lastTakenMilk.setText(user.getLastTakenMilk() + " Litre");
    }

    private void getPrinter() {
        Bitmap bitmap = null;
        QRGEncoder qrgEncoder = new QRGEncoder(user.getId(), null, QRGContents.Type.TEXT, 256);
        try {
            bitmap = qrgEncoder.encodeAsBitmap();
        } catch (WriterException e) {
            Log.e("Tag", e.toString());
        }
        String takenMilk = String.valueOf(Double.parseDouble(user.getTargetMilk()) - Double.parseDouble(user.getTakenMilk()));

        String targetMilkString = user.getTargetMilk() != null ? user.getTargetMilk() : "";
        String lastTakenMilkString = user.getLastTakenMilk() != null ? user.getLastTakenMilk() : "";
        String villageName = user.getVillageName() != null ? user.getVillageName() : "";
        String villageNo = user.getVillageNo() != null ? user.getVillageNo() : "";
        String fullname = user.getFullName() != null ? user.getFullName() : "";
        String id = user.getId() != null ? user.getId() : "";
        String fateRate = mValueModel.getFateRate() != null ? mValueModel.getFateRate() : "";
        String waterRate = mValueModel.getWaterRate() != null ? mValueModel.getWaterRate() : "";
        String snfRate = mValueModel.getSnfRate() != null ? mValueModel.getSnfRate() : "";
        String laktozRate = mValueModel.getLaktozRate() != null ? mValueModel.getLaktozRate() : "";
        String proteinRate = mValueModel.getProteinRate() != null ? mValueModel.getProteinRate() : "";
        String saltRate = mValueModel.getSaltRate() != null ? mValueModel.getSaltRate() : "";
        EscPosPrinter printer = AppManager.getCreatePrinter().getPointer();
        if (printer != null) {

            try {
                printer.printFormattedText("[L]\n" +
                        "[C]<u><font size='big'>Süt Analizi</font></u>\n" +
                        "[L]\n" +
                        "[C]================================\n" +
                        "[R]Toplam Süt Borcu:[R]" + targetMilkString + "\n" +
                        "[R]Alinan Süt:[R]" + lastTakenMilkString + "\n" +
                        "[R]Kalan Borc :[R]" + takenMilk + "\n" +
                        "[R]Yağ :[R]" + fateRate + "\n" +
                        "[R]Su :[R]" + waterRate + "\n" +
                        "[R]Kuru Madde :[R]" + snfRate + "\n" +
                        "[R]Laktoz :[R]" + laktozRate + "\n" +
                        "[R]Protein :[R]" + proteinRate + "\n" +
                        "[R]Tuz :[R]" + saltRate + "\n" +
                        "[L]\n" +
                        "[C]================================\n" +
                        "[L]\n" +
                        "[L]<font size='tall'>Kisi bilgileri :</font>\n" +
                        "[L]Köy ismi:" + villageName + "\n" +
                        "[L]Köy No:" + villageNo + "\n" +
                        "[L]Ad Soyad:" + fullname + "\n" +
                        "[L]Kullanici No:" + id + "\n" +
                        "[L]\n" +
                        "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, bitmap) + "</img>\n"
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
            AppManager.initPrinter(this, AppManager.usbReceiver);
            Toast.makeText(ResultActivity.this, "Yazıcının bağlı olduğundan emin olun.", Toast.LENGTH_LONG).show();
        }
    }

    public void getSensorData(String strings) {
        if (strings.startsWith("data")) {
            isAnaliz = true;
            String resultString = strings.split("data:")[1];
            //String resultString = "data:{fateRate:\"2.1\", laktozRate:\"2.7\", waterRate:\"1.1\", snfRate:\"3.3\", proteinRate:\"6.6\", saltRate:\"5.1\", weight:\"8\"}";
            Gson gson = new Gson();
            ValueModel valueModel = gson.fromJson(resultString, ValueModel.class);
            mValueModel = valueModel;
            activityResultBinding.progressLayout.setVisibility(View.GONE);
            activityResultBinding.resultView.setVisibility(View.VISIBLE);
            //activityResultBinding.avarage.setText(new DecimalFormat("##.#").format(result));
            activityResultBinding.print.setImageDrawable(getDrawable(R.drawable.printer_online));
            activityResultBinding.buttonToggle.setText("Tekrar Ölçüm Yap");
            initString(valueModel);
            if (Double.parseDouble(valueModel.getFateRate()) < 2.9) {
                Toast.makeText(this, "Yağ Oranı Belirtilen Değerin Altında.", Toast.LENGTH_LONG).show();
                activityResultBinding.fateRate.setTextColor(getResources().getColor(R.color.red));
            } else {
                activityResultBinding.fateRate.setTextColor(getResources().getColor(R.color.black));
                activityResultBinding.save.setEnabled(true);
            }
        }
    }

    private void initString(ValueModel valueModel) {
        activityResultBinding.fateRate.setText(valueModel.getFateRate());
        activityResultBinding.waterRate.setText(valueModel.getWaterRate());
        activityResultBinding.proteinRate.setText(valueModel.getProteinRate());
        activityResultBinding.snfRate.setText(valueModel.getSnfRate());
        activityResultBinding.saltRate.setText(valueModel.getSaltRate());
        activityResultBinding.laktozRate.setText(valueModel.getLaktozRate());
        activityResultBinding.takenMilk.setText(valueModel.getWeight());
    }

}
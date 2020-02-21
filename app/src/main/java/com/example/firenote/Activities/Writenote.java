package com.example.firenote.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firenote.R;
import com.example.firenote.database.AppDatabase;
import com.example.firenote.database.AppExecutors;
import com.example.firenote.model.UserData;
import com.example.firenote.utils.ImageCompressionLikeWhatsapp;
import com.example.firenote.utils.ImagePicker;
import com.gauravk.bubblenavigation.BubbleNavigationLinearView;
import com.gauravk.bubblenavigation.BubbleToggleView;
import com.gauravk.bubblenavigation.listener.BubbleNavigationChangeListener;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class Writenote extends BaseActivity implements View.OnClickListener, RecognitionListener {

    private static final String LOG_TAG = "sanjeev";
    BubbleNavigationLinearView bubbleNavigationLinearView;
    ImageCompressionLikeWhatsapp imageCompressionLikeWhatsapp;
    Dialog dialog;
    int REQUEST_CAMERA = 100;
    int SELECT_IMAGES = 200;
    Uri mediaUri;
    File photoFile;
    String realPth = "";
    Uri imageUri = null;
    String compressed_real_path = "";
    String RandomAudioFileName = "PatientAudio";
    Random random;
    SpeechRecognizer speech;
    Intent recognizerIntent;
    UserData userData;
    private AppDatabase mDb;
    LocationManager locationManager;
    private Location location, location2;
    public double latitude = 0;
    public double longitude = 0;
    String categorytitle = "";
    String uinversalimage = "";

    // media boolean
    boolean audiodata = false;
    boolean imagedata = false;
    boolean updatedata = false;
    boolean hidekeyboard = false;

    int record = 0;
    int navigation = 0;
    int save = 0;
    int camera = 0;
    int micnote = 2;
    int catposition = 2;

    private double startTime = 0;
    private double finalTime = 0;
    public static int oneTimeOnly = 0;
    private TextView txt_time;

    ImageView start_rec;
    ImageView stop_rec;
    ImageView stop_music;
    ImageView play_rec;
    ImageView close_rec;
    LinearLayout submit_rec, reset_recorder;
    SeekBar seekBar;
    LinearLayout ll_audio_recorder, ll_music_player, ll_reset_submit;

    String AudioSavePathInDevice = "";
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    private Chronometer chronometer;
    private Handler myHandler = new Handler();


    @BindView(R.id.img_note)
    ImageView img_note;

    @BindView(R.id.txt_upload_audio_ha)
    TextView txt_upload_audio_ha;

    @BindView(R.id.txt_date)
    TextView txt_date;

    @BindView(R.id.edt_subject)
    EditText edt_subject;

    @BindView(R.id.edt_title)
    EditText edt_title;

    @BindView(R.id.l_item_profile)
    BubbleToggleView l_item_profile;

    @BindView(R.id.l_record_audio)
    BubbleToggleView l_record_audio;

    @BindView(R.id.l_maplocation)
    BubbleToggleView l_maplocation;

    @BindView(R.id.l_micnavigation)
    BubbleToggleView l_micnavigation;

    @BindView(R.id.l_savenote)
    BubbleToggleView l_savenote;

    @BindView(R.id.img_card)
    CardView img_card;

    @BindView(R.id.music_card)
    CardView music_card;

    @BindView(R.id.back_write)
    ImageView back_write;

    @BindView(R.id.but_cancel_rec)
    Button but_cancel_rec;

    @BindView(R.id.but_save_rec)
    Button but_save_rec;

    @BindView(R.id.cross_image)
    ImageView cross_image;

    @BindView(R.id.cross_music)
    ImageView cross_music;

    @BindView(R.id.mic_layout)
    LinearLayout mic_layout;

    @BindView(R.id.ll_keyshow)
    LinearLayout ll_keyshow;

    @BindView(R.id.mic_cross)
    ImageView mic_cross;

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    String[] permissionsRequired = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};
    private SharedPreferences permissionStatus;
    private boolean sentToSettings = false;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Writenote.this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.activity_writenote);
        ButterKnife.bind(this);

        categorytitle = getIntent().getStringExtra("category");
        catposition = getIntent().getIntExtra("categoryposition", 0);
        userData = new UserData();

        Bundle data = getIntent().getExtras();
        userData = (UserData) data.getParcelable("notedata");

        if (!(userData == null || userData.getTitle().isEmpty() || userData.getTitle().equals(""))) {
            edt_title.setText(userData.getTitle());
            edt_subject.setText(userData.getSubtitle());
            categorytitle = userData.getCategory();
            uinversalimage = "" + userData.getImagedata();
            //Toast.makeText(Writenote.this, ""+userData.getTitle(), Toast.LENGTH_LONG).show();

            if (!(userData.getImagedata().isEmpty() || userData.getImagedata() == "")) {
                img_card.setVisibility(View.VISIBLE);

                File imgFile = new File("" + userData.getImagedata());

                if (imgFile.exists()) {

                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    img_note.setImageBitmap(myBitmap);

                }
            }

            if (!(userData.getAudiodata().isEmpty() || userData.getAudiodata() == "")) {
                music_card.setVisibility(View.VISIBLE);
                AudioSavePathInDevice = userData.getAudiodata();
                txt_upload_audio_ha.setText("User.mp3");
            }

            txt_date.setText("" + userData.getDatedata());
            updatedata = true;
        } else {
            updatedata = false;
            userData = new UserData();
            getcurrentdate();
        }
        random = new Random();
        cross_image.setOnClickListener(this);
        music_card.setOnClickListener(this);
        cross_music.setOnClickListener(this);
        back_write.setOnClickListener(this);
        mic_cross.setOnClickListener(this);
        but_save_rec.setOnClickListener(this);
        but_cancel_rec.setOnClickListener(this);
        img_card.setOnClickListener(this);
        ll_keyshow.setOnClickListener(this);

        mDb = AppDatabase.getInstance(getApplicationContext());
        imageCompressionLikeWhatsapp = new ImageCompressionLikeWhatsapp(Writenote.this);
        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            askfor_permissions();
        }
        bubbleNavigationLinearView = findViewById(R.id.bottom_navigation_view_linear);
        bubbleNavigationLinearView.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {

                switch (position) {

                    case 0:
                        camera = 0;
                        navigation = 0;
                        record = 0;
                        save = 0;
                        // Toast.makeText(Writenote.this, "add note", Toast.LENGTH_LONG).show();
                        break;

                    case 1:
                        camera = 0;
                        navigation = 0;
                        save = 0;
                        micnote = 0;


                        break;

                    case 2:
                        camera = 0;
                        record = 0;
                        save = 0;
                        micnote = 0;


                        break;

                    case 3:
                        camera = 0;
                        navigation = 0;
                        record = 0;
                        micnote = 0;


                        break;

                    case 4:
                        navigation = 0;
                        record = 0;
                        save = 0;
                        micnote = 0;

                        break;
                }
            }
        });

        l_record_audio.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    record++;
                    bubbleNavigationLinearView.setCurrentActiveItem(1);
                    if (record > 1) {
                        showaudiodialog();
                    }
                    return true;
                }
                return false;
            }
        });

        l_savenote.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    save++;
                    bubbleNavigationLinearView.setCurrentActiveItem(3);
                    if (save > 1) {
                        savenote();
                    }
                    return true;
                }
                return false;
            }
        });


        l_maplocation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    navigation++;
                    bubbleNavigationLinearView.setCurrentActiveItem(2);
                    if (navigation > 1) {


                        Intent intent = new Intent(Writenote.this, Userlocation.class);
                        intent.putExtra("savedlat", userData.getLat());
                        intent.putExtra("savedlong", userData.getLng());
                        startActivity(intent);


                    }
                    return true;
                }
                return false;
            }
        });


        l_item_profile.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    camera++;
                    bubbleNavigationLinearView.setCurrentActiveItem(4);
                    if (camera > 1) {
                        showchooser();
                    }
                    return true;
                }
                return false;
            }
        });

        l_micnavigation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    micnote++;
                    bubbleNavigationLinearView.setCurrentActiveItem(0);
                    if (micnote > 1) {
                        mic_layout.setVisibility(View.VISIBLE);
                        speech.startListening(recognizerIntent);

                    }
                    return true;
                }
                return false;
            }
        });

        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(Writenote.this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

    }

    private void showGPSDisabledAlertToUser() {

        //dialog intialization
        dialog = new Dialog(Writenote.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.no_gps_layout);
        dialog.setCancelable(false);

        Button settings = (Button) dialog.findViewById(R.id.gps_settings);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                //startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                Writenote.this.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });

        dialog.show();
    }

    public void showchooser() {
        //dialog intialization
        dialog = new Dialog(Writenote.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Writenote.this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.choose);
        dialog.setCancelable(true);

        LinearLayout camera_choose = (LinearLayout) dialog.findViewById(R.id.camera_picker);
        LinearLayout gallery_choose = (LinearLayout) dialog.findViewById(R.id.gallery_picker);


        camera_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("CAMERA >>>>>> ");
                try {
                    launchCameraForImage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });

        gallery_choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryImageIntent();
                dialog.dismiss();
            }
        });


        dialog.show();
    }

    // for high quality image
    public void launchCameraForImage() throws IOException {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            photoFile = getPhotoFileUri();
            mediaUri = FileProvider.getUriForFile(Writenote.this, "com.firenote.provider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mediaUri);

            if (intent.resolveActivity(Writenote.this.getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CAMERA);
            }

        } catch (Exception e) {

            try {
                Log.e("launchCameraForImage: ", e.getMessage());
            } catch (Exception e1) {
                e1.printStackTrace();
            }

        }
    }

    void galleryImageIntent() {
        Intent intent = null;
        try {
            intent = ImagePicker.getPickImageIntent(Writenote.this);
            startActivityForResult(intent, SELECT_IMAGES);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_IMAGES) {
                if (data != null && data.getData() != null) {
                    imageUri = data.getData();
                    realPth = getRealPathFromURI(imageUri);
                    img_note.setImageURI(imageUri);
                    compressed_real_path = imageCompressionLikeWhatsapp.compressImage(realPth);
                    imagedata = true;
                    img_card.setVisibility(View.VISIBLE);
                    uinversalimage = "" + compressed_real_path;

                }
            } else if (requestCode == REQUEST_CAMERA) {
                realPth = photoFile.getPath();
                imageUri = Uri.parse(realPth);
                compressed_real_path = imageCompressionLikeWhatsapp.compressImage(realPth);
                img_note.setImageURI(imageUri);
                imagedata = true;
                img_card.setVisibility(View.VISIBLE);
                uinversalimage = "" + compressed_real_path;
            }
        } else if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(Writenote.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    proceedAfterPermission();
                }
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String path = null;
        String[] proj = {MediaStore.MediaColumns.DATA};
        Cursor cursor = Writenote.this.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;
    }

    public File getPhotoFileUri() {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(Writenote.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "firechat");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            // Log.e(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + System.currentTimeMillis() + ".jpg");

        return file;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            //check if all permissions are granted
            boolean allgranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if (allgranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    proceedAfterPermission();
                }
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(Writenote.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(Writenote.this, permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(Writenote.this, permissionsRequired[2])) {

                askfor_permissions();

            } else {
                Toast.makeText(getBaseContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();

            }
        }
    }

    public String CreateRandomAudioFileName(int string) {
        StringBuilder stringBuilder = new StringBuilder(string);
        int i = 0;
        while (i < string) {
            stringBuilder.append(RandomAudioFileName.
                    charAt(random.nextInt(RandomAudioFileName.length())));

            i++;
        }
        return stringBuilder.toString();
    }

    public void MediaRecorderReady() {
        if (mediaRecorder == null) {
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            mediaRecorder.setOutputFile(AudioSavePathInDevice);
        }

    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {

            try {
                startTime = mediaPlayer.getCurrentPosition();
                seekBar.setProgress((int) startTime);
                myHandler.postDelayed(this, 100);

                if (startTime == finalTime) {
                    stop_music.performClick();
                }

            } catch (Exception e) {

            }


        }
    };


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void proceedAfterPermission() {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getLoc();
            }
        } else {
            showGPSDisabledAlertToUser();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getLoc() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            if (locationManager != null) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                location2 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } else if (location2 != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } else {
                    latitude = 0;
                    longitude = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(Writenote.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    proceedAfterPermission();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void askfor_permissions() {
        if (ActivityCompat.checkSelfPermission(Writenote.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(Writenote.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(Writenote.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Writenote.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(Writenote.this, permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(Writenote.this, permissionsRequired[2])) {

                ActivityCompat.requestPermissions(Writenote.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);

            } else if (permissionStatus.getBoolean(permissionsRequired[0], false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission

                ActivityCompat.requestPermissions(Writenote.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(Writenote.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }

            // txtPermissions.setText("Permissions Required");

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0], true);
            editor.commit();
        } else {
            //You already have the permission, just go ahead.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                proceedAfterPermission();
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.cross_image:
                imagedata = false;
                deletepic();
                break;

            case R.id.ll_keyshow:
               if(hidekeyboard)
               {
                   hideKeyboard(Writenote.this);

                   hidekeyboard = false;
               }
               else
               {
                   hidekeyboard = true;
               }
                break;


            case R.id.mic_cross:
                mic_layout.setVisibility(View.GONE);
                speech.stopListening();
                break;

            case R.id.but_save_rec:
                mic_layout.setVisibility(View.VISIBLE);
                speech.startListening(recognizerIntent);
                break;

            case R.id.but_cancel_rec:
                mic_layout.setVisibility(View.GONE);
                speech.stopListening();
                break;

            case R.id.img_card:
                watchimagefull();
                break;

            case R.id.back_write:

                Intent intent = new Intent(Writenote.this, MainActivity.class);
                startActivity(intent);

                break;

            case R.id.music_card:
                if (checkPermission()) {
                    showaudiodialog();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        askfor_permissions();
                    }
                }
                break;

            case R.id.cross_music:
                showdeleteaudio();
                audiodata = false;
                break;

            case R.id.cross_dialog:

                if (mediaPlayer != null) {

                    try {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    } catch (Exception e) {

                    }

                }

                if (txt_upload_audio_ha.getText().equals("Upload Audio")) {
                    AudioSavePathInDevice = "";
                }

                try {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                } catch (Exception e) {

                }

                audiodata = false;
                dialog.dismiss();
                break;


            case R.id.start_audio:

                if (checkPermission()) {

                    AudioSavePathInDevice = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + CreateRandomAudioFileName(5) + "NoteAudio.mp3";

                    try {

                        MediaRecorderReady();
                        mediaRecorder.prepare();
                        mediaRecorder.start();

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        Toast.makeText(Writenote.this, "Recording started" + e, Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                    Toast.makeText(Writenote.this, "Recording started", Toast.LENGTH_LONG).show();

                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();

                    start_rec.setVisibility(View.GONE);
                    stop_rec.setVisibility(View.VISIBLE);

                } else {
                    askfor_permissions();
                }

                break;

            case R.id.stop_audio:


                if (mediaRecorder != null) {

                    try {
                        mediaRecorder.stop();
                    } catch (Exception e) {
                        System.out.println("?????????" + e);
                    }

                    // clear recorder configuration
                    mediaRecorder.reset();
                    // release the recorder object
                    mediaRecorder.release();
                    mediaRecorder = null;

                    System.out.println(">>>>>>>>>" + AudioSavePathInDevice);
                }


                stop_rec.setVisibility(View.GONE);
                play_rec.setVisibility(View.VISIBLE);
                chronometer.stop();

                ll_audio_recorder.setVisibility(View.GONE);
                ll_music_player.setVisibility(View.VISIBLE);
                ll_reset_submit.setVisibility(View.VISIBLE);

                Toast.makeText(Writenote.this, "Recording Completed", Toast.LENGTH_LONG).show();

                //setting audio duration time on text view
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePathInDevice);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                finalTime = mediaPlayer.getDuration();
                startTime = mediaPlayer.getCurrentPosition();

                if (oneTimeOnly == 0) {
                    seekBar.setMax((int) finalTime);
                    oneTimeOnly = 1;
                }

                txt_time.setText(String.format("%d min : %d sec", TimeUnit.MILLISECONDS.toMinutes((long) finalTime), TimeUnit.MILLISECONDS.toSeconds((long) finalTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) finalTime))));

                seekBar.setProgress((int) startTime);

                break;

            case R.id.but_reset:

                AudioSavePathInDevice = "";
                audiodata = false;
                ll_audio_recorder.setVisibility(View.VISIBLE);
                ll_music_player.setVisibility(View.GONE);
                ll_reset_submit.setVisibility(View.GONE);

                play_rec.setVisibility(View.VISIBLE);
                stop_music.setVisibility(View.GONE);

                start_rec.setVisibility(View.VISIBLE);
                stop_rec.setVisibility(View.GONE);

                chronometer.setBase(SystemClock.elapsedRealtime());

                if (mediaPlayer != null) {

                    try {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    } catch (Exception e) {

                    }
                }

                break;

            case R.id.play_audio:

                play_rec.setVisibility(View.GONE);
                stop_music.setVisibility(View.VISIBLE);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePathInDevice);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finalTime = mediaPlayer.getDuration();
                startTime = mediaPlayer.getCurrentPosition();
                seekBar.setMax((int) finalTime);
                seekBar.setProgress((int) startTime);


                mediaPlayer.start();
                Toast.makeText(Writenote.this, "Recording Playing", Toast.LENGTH_LONG).show();
                myHandler.postDelayed(UpdateSongTime, 100);

                break;


            case R.id.stop_music:

                try {
                    play_rec.setVisibility(View.VISIBLE);
                    stop_music.setVisibility(View.GONE);

                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    }
                } catch (Exception e) {

                }
                break;


            case R.id.but_submit_audio:

                if (mediaPlayer != null) {

                    try {
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    } catch (Exception e) {

                    }
                }

                String uploadpiv = "Upload Audio";
                String empty = "";


                if (AudioSavePathInDevice == null || AudioSavePathInDevice.equals(uploadpiv) || AudioSavePathInDevice.equals(empty)) {

                    txt_upload_audio_ha.setText("Upload Audio");
                } else {
                    music_card.setVisibility(View.VISIBLE);
                    txt_upload_audio_ha.setText("User.mp3");
                    // img_delete_audio_ha.setVisibility(View.VISIBLE);
                    stop_music.performClick();
                    audiodata = true;
                }

                dialog.dismiss();
                //Toast.makeText(getActivity(), ""+AudioSavePathInDevice, Toast.LENGTH_LONG).show();

                break;

        }

    }

    public void showdeleteaudio() {
        dialog = new Dialog(Writenote.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Writenote.this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.delete_sure);
        dialog.setCancelable(false);

        LinearLayout ll_no_delete = dialog.findViewById(R.id.ll_no_delete);
        LinearLayout ll_yes_delete = dialog.findViewById(R.id.ll_yes_delete);

        ll_no_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        ll_yes_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                music_card.setVisibility(View.GONE);
                txt_upload_audio_ha.setText("Upload Audio");
                AudioSavePathInDevice = "";
                dialog.dismiss();

            }
        });

        dialog.show();
    }


    public void deletepic() {
        dialog = new Dialog(Writenote.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Writenote.this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.delete_sure);
        dialog.setCancelable(false);

        TextView textView = dialog.findViewById(R.id.txt_delete_option);

        LinearLayout ll_no_delete = dialog.findViewById(R.id.ll_no_delete);
        LinearLayout ll_yes_delete = dialog.findViewById(R.id.ll_yes_delete);

        ll_no_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        ll_yes_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();

                img_card.setVisibility(View.GONE);
            }
        });

        dialog.show();
    }

    public void showaudiodialog() {
        dialog = new Dialog(Writenote.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Writenote.this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.record_audio);
        dialog.setCancelable(false);

        start_rec = dialog.findViewById(R.id.start_audio);
        stop_rec = dialog.findViewById(R.id.stop_audio);
        play_rec = dialog.findViewById(R.id.play_audio);
        close_rec = dialog.findViewById(R.id.cross_dialog);
        submit_rec = dialog.findViewById(R.id.but_submit_audio);
        chronometer = dialog.findViewById(R.id.chronometer);
        txt_time = dialog.findViewById(R.id.txt_time);
        seekBar = dialog.findViewById(R.id.seekBar);
        stop_music = dialog.findViewById(R.id.stop_music);
        ll_music_player = dialog.findViewById(R.id.ll_music_player);
        ll_audio_recorder = dialog.findViewById(R.id.ll_audio_recorder);
        reset_recorder = dialog.findViewById(R.id.but_reset);
        ll_reset_submit = dialog.findViewById(R.id.ll_reset_submit);

        seekBar.setClickable(false);

        start_rec.setOnClickListener(this);
        stop_rec.setOnClickListener(this);
        play_rec.setOnClickListener(this);
        close_rec.setOnClickListener(this);
        submit_rec.setOnClickListener(this);
        stop_music.setOnClickListener(this);
        reset_recorder.setOnClickListener(this);

        if (txt_upload_audio_ha.getText().equals("Upload Audio")) {

        } else {
            //setting audio duration time on text view

            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(AudioSavePathInDevice);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            finalTime = mediaPlayer.getDuration();
            startTime = mediaPlayer.getCurrentPosition();

            if (oneTimeOnly == 0) {
                seekBar.setMax((int) finalTime);
                oneTimeOnly = 1;
            }

            txt_time.setText(String.format("%d min : %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    finalTime)))
            );

            seekBar.setMax((int) finalTime);
            seekBar.setProgress((int) startTime);


            ll_reset_submit.setVisibility(View.GONE);
            ll_audio_recorder.setVisibility(View.GONE);
            ll_music_player.setVisibility(View.VISIBLE);
        }

        dialog.show();
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(Writenote.this, WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(Writenote.this, RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
            Toast.makeText(Writenote.this, "destroy", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        // progressBar.setIndeterminate(false);
        // progressBar.setMax(10);
        Toast.makeText(Writenote.this, "onBeginningOfSpeech", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
        Toast.makeText(Writenote.this, "onBufferReceived", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        Toast.makeText(Writenote.this, "onEndOfSpeech", Toast.LENGTH_LONG).show();
        // progressBar.setIndeterminate(true);
        // toggleButton.setChecked(false);
        mic_layout.setVisibility(View.GONE);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        // txt_subject.setText(errorMessage);
        Toast.makeText(Writenote.this, "" + errorMessage, Toast.LENGTH_LONG).show();
        //toggleButton.setChecked(false);
        mic_layout.setVisibility(View.GONE);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
//        for (String result : matches)
//        {
//            text += result + "\n";
//        }
//
        text = edt_subject.getText().toString();

        text += matches.get(0);
        edt_subject.setText(" " + text);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
//        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
//        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }


    public void savenote() {
        if (edt_title.getText().toString().isEmpty() || edt_subject.getText().toString().isEmpty() || edt_title.getText().toString().equals("") || edt_subject.getText().toString().equals("")) {
            displayAlert(Writenote.this, "Please input Text and Title");
        } else {

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        userData.setTitle(edt_title.getText().toString());
                        userData.setSubtitle(edt_subject.getText().toString());
                        userData.setAudiodata(AudioSavePathInDevice);
                        userData.setImagedata(compressed_real_path);
                        userData.setLat(latitude);
                        userData.setLng(longitude);
                        userData.setCategory("" + categorytitle);
                        userData.setDatedata("" + txt_date.getText());

                        if (updatedata) {
                            mDb.noteDao().updatePerson(userData);
                        }
                        else
                        {
                            mDb.noteDao().insertPerson(userData);
                        }

                        intent = new Intent(Writenote.this, MainActivity.class);
                        startActivity(intent);
                        // retrieveTasks();
                    }
                });

            //hideKeyboard(Writenote.this);
        }
    }

    private void retrieveTasks() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<UserData> user = mDb.noteDao().loadAllPersons();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String text = "";
                        for (int i = 0; i < user.size(); i++) {
                            if (user.get(i).getTitle().equals("") || user.get(i).getTitle().isEmpty()) {

                            } else {
                                text += user.get(i).getTitle() + "\n";
                            }

                        }

                        displayAlert(Writenote.this, "" + text);
                    }
                });
            }
        });
    }

    public void getcurrentdate() {
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);
        SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy");
        String formattedDate = df.format(c);
        System.out.println(">>>>>>" + formattedDate);

        txt_date.setText("" + formattedDate);


    }

    public void watchimagefull() {
        dialog = new Dialog(Writenote.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Writenote.this.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.show_fullsize_image);
        dialog.setCancelable(false);

        ImageView myZoomageView = dialog.findViewById(R.id.myZoomageView);
        ImageView imgcross = dialog.findViewById(R.id.img_full_cross);

        File imgFile = new File(uinversalimage);

        if (imgFile.exists()) {

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            myZoomageView.setImageBitmap(myBitmap);

        }

        imgcross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });


        dialog.show();

    }
}

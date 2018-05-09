package ca.nait.rresuena1.dungeonsdragonsapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Resources:
 * http://tekeye.uk/android/examples/android-dice-code (for Dice Roller Startup Kit) (Last Accessed April 12, 2018)
 * https://stackoverflow.com/questions/2317428/android-i-want-to-shake-it (for Shake Event) (Last Accessed April 12, 2018)
 * http://abhiandroid.com/ui/seekbar (for SeekBar sensitivity) (Last Accessed April 15, 2018)
 */

public class DiceRollerActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnRoll, btn4D, btn6D, btn8D, btn10D, btn12D, btn20D;
    TextView textViewNumber, naturalMsg;

    int selectedDie = 6;

    ImageView dice_picture;     //reference to dice picture
    Random rng = new Random();    //generate random numbers
    SoundPool dice_sound;       //For dice sound playing
    int sound_id;               //Used to control sound stream return by SoundPool
    Handler handler;            //Post message to start roll
    Timer timer = new Timer();    //Used to implement feedback to user
    boolean rolling = false;      //Is die rolling?

    SeekBar seekBar;
    int progressChangedValue = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dice_roller);

        //Our function to initialise sound playing
        InitSound();
        //Get a reference to image widget
        dice_picture = (ImageView) findViewById(R.id.image_view_die);
        dice_picture.setOnClickListener(this);

        //TextView
        textViewNumber = (TextView) findViewById(R.id.text_view_number);
        textViewNumber.setText(""); // blank text
        naturalMsg = (TextView) findViewById(R.id.text_view_natural_msg);

        //Buttons
        btnRoll = (Button) findViewById(R.id.button_roll);
        btnRoll.setOnClickListener(this);
        btn4D = (Button) findViewById(R.id.button_4D);
        btn4D.setOnClickListener(this);
        btn6D = (Button) findViewById(R.id.button_6D);
        btn6D.setOnClickListener(this);
        btn8D = (Button) findViewById(R.id.button_8D);
        btn8D.setOnClickListener(this);
        btn10D = (Button) findViewById(R.id.button_10D);
        btn10D.setOnClickListener(this);
        btn12D = (Button) findViewById(R.id.button_12D);
        btn12D.setOnClickListener(this);
        btn20D = (Button) findViewById(R.id.button_20D);
        btn20D.setOnClickListener(this);

        //link handler to callback
        handler = new Handler(callback);

        //Shake Event
        /* do this in onCreate */
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        // SeekBar
        seekBar = (SeekBar) findViewById(R.id.seekbar_sensitivity);
        seekBar.setProgress(8);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChangedValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (progressChangedValue <= 5)
                    Toast.makeText(DiceRollerActivity.this, "Very Sensitive", Toast.LENGTH_SHORT).show();
                else if (progressChangedValue >= 10)
                    Toast.makeText(DiceRollerActivity.this, "Barely Sensitive", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(DiceRollerActivity.this, "Mediocre Sensitive", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_roll:
                naturalMsg.setVisibility(View.INVISIBLE);
                RollDie();
                break;
            case R.id.image_view_die:
                naturalMsg.setVisibility(View.INVISIBLE);
                RollDie();
                break;
            case R.id.button_4D:
                naturalMsg.setVisibility(View.INVISIBLE);
                textViewNumber.setText("");
                Toast.makeText(DiceRollerActivity.this, "Not yet implemented.", Toast.LENGTH_LONG).show();
                break;
            case R.id.button_6D:
                naturalMsg.setVisibility(View.INVISIBLE);
                textViewNumber.setText("");
                dice_picture.setImageResource(R.drawable.dice3droll);
                selectedDie = 6;
                break;
            case R.id.button_8D:
                naturalMsg.setVisibility(View.INVISIBLE);
                textViewNumber.setText("");
                Toast.makeText(DiceRollerActivity.this, "Not yet implemented.", Toast.LENGTH_LONG).show();
                break;
            case R.id.button_10D:
                naturalMsg.setVisibility(View.INVISIBLE);
                textViewNumber.setText("");
                Toast.makeText(DiceRollerActivity.this, "Not yet implemented.", Toast.LENGTH_LONG).show();
                break;
            case R.id.button_12D:
                naturalMsg.setVisibility(View.INVISIBLE);
                textViewNumber.setText("");
                Toast.makeText(DiceRollerActivity.this, "Not yet implemented.", Toast.LENGTH_LONG).show();
                break;
            case R.id.button_20D:
                naturalMsg.setVisibility(View.INVISIBLE);
                dice_picture.setImageResource(R.drawable.d20_blank);
                selectedDie = 20;
                break;
        }
    }

    //New code to initialise sound playback
    void InitSound() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //Use the newer SoundPool.Builder
            //Set the audio attributes, SONIFICATION is for interaction events
            //uses builder pattern
            AudioAttributes aa = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            //default max streams is 1
            //also uses builder pattern
            dice_sound = new SoundPool.Builder().setAudioAttributes(aa).build();

        } else {
            // Running on device earlier than Lollipop
            //Use the older SoundPool constructor
            dice_sound = PreLollipopSoundPool.NewSoundPool();
        }
        //Load the dice sound
        sound_id = dice_sound.load(this, R.raw.shake_dice, 1);
    }

    //When pause completed message sent to callback
    class Roll extends TimerTask {
        public void run() {
            handler.sendEmptyMessage(0);
        }
    }

    //Receives message from timer to start dice roll
    Handler.Callback callback = new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            //Get roll result
            switch (selectedDie) {
                case 4:
                    break;
                case 6:
                    Die_6D();
                    break;
                case 8:
                    break;
                case 10:
                    break;
                case 12:
                    break;
                case 20:
                    Die_20D();
                    break;
            }
            rolling = false;    //user can press again
            return true;
        }
    };

    protected void RollDie() {
        if (!rolling) {
            rolling = true;
            //Show rolling image
            switch (selectedDie) {
                case 4:
                    break;
                case 6:
                    dice_picture.setImageResource(R.drawable.dice3droll);
                    break;
                case 8:
                    break;
                case 10:
                    break;
                case 12:
                    break;
                case 20:
                    textViewNumber.setText("");
                    naturalMsg.setVisibility(View.INVISIBLE);
                    dice_picture.setImageResource(R.drawable.d20_blank);
                    break;
            }
            //Start rolling sound
            dice_sound.play(sound_id, 1.0f, 1.0f, 0, 0, 1.0f);
            //Vibrate Phone
            VibratePhone(300);
            //Pause to allow image to update
            timer.schedule(new Roll(), 600);
        }
    }

    protected void Die_6D() {
        //Remember nextInt returns 0 to 5 for argument of 6
        //hence + 1
        switch (rng.nextInt(6) + 1) {
            case 1:
                dice_picture.setImageResource(R.drawable.one);
                break;
            case 2:
                dice_picture.setImageResource(R.drawable.two);
                break;
            case 3:
                dice_picture.setImageResource(R.drawable.three);
                break;
            case 4:
                dice_picture.setImageResource(R.drawable.four);
                break;
            case 5:
                dice_picture.setImageResource(R.drawable.five);
                break;
            case 6:
                dice_picture.setImageResource(R.drawable.six);
                break;
            default:
        }
    }

    protected void Die_20D() {
        switch (rng.nextInt(20) + 1) {
            case 1:
                textViewNumber.setText("1");
                break;
            case 2:
                textViewNumber.setText("2");
                break;
            case 3:
                textViewNumber.setText("3");
                break;
            case 4:
                textViewNumber.setText("4");
                break;
            case 5:
                textViewNumber.setText("5");
                break;
            case 6:
                textViewNumber.setText("6");
                break;
            case 7:
                textViewNumber.setText("7");
                break;
            case 8:
                textViewNumber.setText("8");
                break;
            case 9:
                textViewNumber.setText("9");
                break;
            case 10:
                textViewNumber.setText("10");
                break;
            case 11:
                textViewNumber.setText("11");
                break;
            case 12:
                textViewNumber.setText("12");
                break;
            case 13:
                textViewNumber.setText("13");
                break;
            case 14:
                textViewNumber.setText("14");
                break;
            case 15:
                textViewNumber.setText("15");
                break;
            case 16:
                textViewNumber.setText("16");
                break;
            case 17:
                textViewNumber.setText("17");
                break;
            case 18:
                textViewNumber.setText("18");
                break;
            case 19:
                textViewNumber.setText("19");
                break;
            case 20:
                naturalMsg.setVisibility(View.VISIBLE);
                textViewNumber.setText("20");
                break;
            default:
        }
    }

    //Clean up
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener); //Shake Event
        super.onPause();
        dice_sound.pause(sound_id);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL); //Shake Event
    }

    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    // Shake Event
    /* put this into your activity class */
    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity

    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta; // perform low-cut filter

            if (mAccel > progressChangedValue) {
                RollDie();
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // do nothing
        }
    };

    // Vibrate
    public void VibratePhone(long milliseconds) {
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(milliseconds);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.about_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_about:
                Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra("url", "https://easyrollerdice.com/blogs/rpg/dd-dice");
                startActivity(intent);
                break;
        }
        return true;
    }
}

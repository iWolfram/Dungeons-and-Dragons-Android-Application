package ca.nait.rresuena1.dungeonsdragonsapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    Button buttonDiceRoller, buttonCreateCharacter;
    ImageView imageViewInfoIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        buttonDiceRoller = (Button) findViewById(R.id.button_dice_roller);
        buttonCreateCharacter = (Button) findViewById(R.id.button_create_character);
        imageViewInfoIcon = (ImageView) findViewById(R.id.image_view_dnd_info);

        buttonDiceRoller.setOnClickListener(this);
        buttonCreateCharacter.setOnClickListener(this);
        imageViewInfoIcon.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_dice_roller: {
                startActivity(new Intent(this, DiceRollerActivity.class));
                break;
            }
            case R.id.button_create_character: {
                Toast.makeText(this, "Not yet implemented.", Toast.LENGTH_LONG).show();
                break;
            }
            case R.id.image_view_dnd_info: {
                Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra("url", "http://dnd.wizards.com/dungeons-and-dragons/what-is-dd");
                startActivity(intent);
                break;
            }
        }
    }
}

package gilbomi.com;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

public class MediInfoActivity extends AppCompatActivity {
    RelativeLayout layout;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediinfo);

        layout = (RelativeLayout)findViewById(R.id.layout);

        Intent intent = getIntent();

        String options[] = intent.getExtras().getStringArray("options");

        for(String option : options) {
            Button btn = new Button(mContext);
            btn.setText(option);
            layout.addView(btn);
        }

//        String add_options = "";
//        for(String option : options) {
//            add_options += option + ",";
//        }
//        tv.setText(add_options);
    }

}

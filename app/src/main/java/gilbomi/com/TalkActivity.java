package gilbomi.com;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TalkActivity extends AppCompatActivity {
    TextView resultTextView, inputTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);

        resultTextView = (TextView)findViewById(R.id.result_text_view);
        inputTextView = (TextView)findViewById(R.id.input_text_view);

    }
}

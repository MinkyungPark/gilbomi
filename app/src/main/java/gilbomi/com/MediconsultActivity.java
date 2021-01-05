package gilbomi.com;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import gilbomi.com.Classification.Result;

public class MediconsultActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    private static final String TAG = "질병분류";
    private String symptomName = "";
    private Classification client;

    private TextView resultTextView;
    private TextView inputTextView;
    private Handler handler;

    Context mContext;
    Intent sttIntent;
    SpeechRecognizer mRecognizer;
    TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediconsult);
        mContext = this;

        client = new Classification(getApplicationContext());
        handler = new Handler();
        tts = new TextToSpeech(mContext, this);

        resultTextView = findViewById(R.id.result_text_view);
        inputTextView = findViewById(R.id.input_text);
        //scrollView = findViewById(R.id.scroll_view);
        Button classifyButton = findViewById(R.id.button);

        classifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.stop();
                speechStart();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");
        handler.post(
                () -> {
                    client.load();
                    // speechInit();
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop");
        handler.post(
                () -> {
                    client.unload();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.post(
                () -> {
                    ttsDestroy();
                    sttDestroy();
                });
    }

    private void sttDestroy() {
        if(mRecognizer!=null){
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer=null;
        }
    }

    private void ttsDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.KOREAN);
            tts.setPitch(1);
        } else {
            Log.e("TTS", "초기화 실패");
        }
    }


    public void speechStart() {
        handler.post(
                () -> {
                    sttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    sttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
                    sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");

                    mRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext); // 음성인식 객체
                    mRecognizer.setRecognitionListener(listener); // 음성인식 리스너 등록
                    mRecognizer.startListening(sttIntent);
                });
    }


    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float rmsdB) {
        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int error) {
            String message;

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }
            String guideStr = "에러가 발생하였습니다.";
            Toast.makeText(getApplicationContext(), guideStr + message, Toast.LENGTH_LONG).show();
            funcVoiceOut(guideStr);
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            String resultStr = "";

            for (int i = 0; i < matches.size(); i++) {
                inputTextView.setText(matches.get(i));
                resultStr += matches.get(i);
            }

            if(resultStr.indexOf("네") > -1 || resultStr.indexOf("응") > -1) {
                Toast.makeText(getApplicationContext(), "YES", Toast.LENGTH_LONG).show();
                sayYes();
            } else if(resultStr.indexOf("아니") > -1 || resultStr.indexOf("괜찮아") > -1) {
                Toast.makeText(getApplicationContext(), "NO", Toast.LENGTH_LONG).show();
                sayNo();
            } else {
                classify(resultStr);
            }

        }

        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    };

    public void funcVoiceOut(String OutMsg){
        handler.post(
                () -> {
                    if(OutMsg.length()<1) return;
                    if(!tts.isSpeaking()) {
                        tts.speak(OutMsg, TextToSpeech.QUEUE_FLUSH, null);
                    }
                });
    }

    private void classify(final String text) {
        handler.post(
                () -> {
                    List<Result> results = client.classify(text);
                    showResult(results);
                });
    }

    private void showResult(final List<Result> results) {
        runOnUiThread(
                () -> {
                    Result result = results.get(0);
                    symptomName = result.getTitle();
                    symptomName = symptomName.replace(" ", "");
                    String resultToVoice = String.format("예상되는 증상은 %s 입니다. 예상되는 증상에 대한 질병 정보를 알려드릴까요?", symptomName);
                    funcVoiceOut(resultToVoice);
                    sttDestroy();
                    //scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sttDestroy();
                            tts.stop();
                            speechStart();
                        }
                    }, 6000);
                });
    }

    private void sayYes() {
        handler.post(
                () -> {
                    String diseaseName = getDiseaseName(symptomName);
                    inputTextView.setText(symptomName);
                    String resultToVoice = String.format("예상되는 질병은 %s 입니다. 이 중 어떤 질병에 대해 알려드릴까요?", diseaseName);
                    funcVoiceOut(resultToVoice);

                    String[] options = diseaseName.split(" ");
                    Intent intent = new Intent(getApplicationContext(), MediInfoActivity.class);
                    intent.putExtra("options", options);
                    startActivity(intent);

                    // String diseaseInfo = parse.getDiseaseInfo(diseaseName);

                });
    }

    private void sayNo() {
        sttDestroy();
        ttsDestroy();
        symptomName = "";
    }

    private String getDiseaseName(String user_symptom) {
        String resultNames = "";
        try {
            InputStream is = getAssets().open("질환DB.json");
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);

            StringBuffer buffer = new StringBuffer();
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line+"\n");
                line = reader.readLine();
            }
            String jsonData = buffer.toString();

            try {
                JSONArray jsonArray = new JSONArray(jsonData);
                ArrayList<Integer> result_disease = new ArrayList<Integer>();

                for(int i=0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String tmp = jsonObject.getString("symptom");
                    if(tmp.indexOf(user_symptom) > -1) {
                        result_disease.add(i);
                    }
                }
                for(int result : result_disease) {
                    JSONObject jsonObject = jsonArray.getJSONObject(result);
                    String name = jsonObject.getString("name");
                    resultNames += name + " ";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return resultNames;
    }

    // public String getDiseaseInfo(String disease_name) {}

}


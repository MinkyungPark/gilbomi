package gilbomi.com;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    Button btn1, btn2, btn3, btn4, btn5, btn6;
    public static Context mContext;

    Intent sttIntent;
    SpeechRecognizer mRecognizer;
    TextToSpeech tts;
    final int PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);
        mContext = this;

        btn1 = (Button)findViewById(R.id.btn_healthcare); // 건강관리
        btn2 = (Button)findViewById(R.id.btn_mediconsult); // 의료상담
        btn3 = (Button)findViewById(R.id.btn_talk); // 대화하기
        btn4 = (Button)findViewById(R.id.btn_photo); // 사진찍기
        btn5 = (Button)findViewById(R.id.btn_game); // 게임하기
        btn6 = (Button)findViewById(R.id.btn_speech); // 음성인식시작

        // 오디오, 카메라 권한설정
        if ( Build.VERSION.SDK_INT >= 23 ){
            // 퍼미션 체크
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO},PERMISSION);
        }

        // STT, TTS 로드
        speechInit();

        // Button Click Event 설정
        btn1.setOnClickListener(new View.OnClickListener() { //건강관리
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), HealthcareActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_in);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() { //의료상담
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), MediconsultActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_in);
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() { //대화하기
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), TalkActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_in);
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() { //사진찍기
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), PhotoActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_in);
            }
        });

        btn5.setOnClickListener(new View.OnClickListener() { //게임하기
            @Override
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_in);
            }
        });

        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speechStart();
            }
        });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if(mRecognizer!=null){
            mRecognizer.destroy();
            // mRecognizer.cancel();
            mRecognizer=null;
        }
    }

    private void speechInit() {
        // stt 객체 생성, 초기화
        sttIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        sttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        sttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");

        // tts 객체 생성, 초기화
        tts = new TextToSpeech(MainActivity.this, this);
    }


    public void speechStart() {
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext); // 음성인식 객체
        mRecognizer.setRecognitionListener(listener); // 음성인식 리스너 등록
        mRecognizer.startListening(sttIntent);
    }


    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(), "음성인식을 시작합니다.", Toast.LENGTH_LONG).show();
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
                resultStr += matches.get(i);
            }

            Toast.makeText(getApplicationContext(), resultStr, Toast.LENGTH_LONG).show();

            if(resultStr.length() < 1) return;
            resultStr = resultStr.replace(" ", "");

            moveActivity(resultStr);
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }
    };

    public void moveActivity(String resultStr) {
        if(resultStr.indexOf("건강관리") > -1) {
            String guideStr = "건강관리를 실행합니다.";
            Toast.makeText(getApplicationContext(), guideStr, Toast.LENGTH_LONG).show();
            funcVoiceOut(guideStr);
            btn1.callOnClick();
            finish();
        }
        if(resultStr.indexOf("의료상담") > -1) {
            String guideStr = "의료상담을 실행합니다.";
            Toast.makeText(getApplicationContext(), guideStr, Toast.LENGTH_LONG).show();
            funcVoiceOut(guideStr);
            btn2.callOnClick();
            finish();
        }
        if(resultStr.indexOf("대화하기") > -1) {
            String guideStr = "대화하기를 실행합니다.";
            Toast.makeText(getApplicationContext(), guideStr, Toast.LENGTH_LONG).show();
            funcVoiceOut(guideStr);
            btn3.callOnClick();
            finish();
        }
        if(resultStr.indexOf("사진찍기") > -1) {
            String guideStr = "사진찍기를 실행합니다.";
            Toast.makeText(getApplicationContext(), guideStr, Toast.LENGTH_LONG).show();
            funcVoiceOut(guideStr);
            btn4.callOnClick();
            finish();
        }
        if(resultStr.indexOf("게임하기") > -1) {
            String guideStr = "게임하기를 실행합니다.";
            Toast.makeText(getApplicationContext(), guideStr, Toast.LENGTH_LONG).show();
            funcVoiceOut(guideStr);
            btn5.callOnClick();
            finish();
        }
    }

    public void funcVoiceOut(String OutMsg){
        if(OutMsg.length()<1)return;
        if(!tts.isSpeaking()) {
            tts.speak(OutMsg, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}

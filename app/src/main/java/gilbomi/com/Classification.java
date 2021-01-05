package gilbomi.com;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import androidx.annotation.WorkerThread;

import org.snu.ids.kkma.constants.POSTag;
import org.snu.ids.kkma.index.Keyword;
import org.snu.ids.kkma.index.KeywordExtractor;
import org.snu.ids.kkma.index.KeywordList;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Classification {
    private static final String TAG = "ClassificationDemo";
    private static final String MODEL_PATH = "converted_model.tflite";
    private static final String DIC_PATH = "vocab.txt";
    private static final String LABEL_PATH = "labels.txt";

    private static final int SENTENCE_LEN = 512;
    private static final String UNKNOWN = "OOV";
    private static final int MAX_RESULTS = 36;

    private final Context context;
    private final Map<String, Integer> dic = new HashMap<>();
    private final List<String> labels = new ArrayList<>();
    private Interpreter tflite;

    public static class Result {
        private final String id;
        private final String title;
        private final Float confidence;

        public Result(final String id, final String title, final Float confidence) {
            this.id = id;
            this.title = title;
            this.confidence = confidence;
        }
        public String getId() {
            return id;
        }
        public String getTitle() {
            return title;
        }
        public Float getConfidence() {
            return confidence;
        }

        @Override
        public String toString() {
            String resultString = "";
            if (id != null) {
                resultString += "[" + id + "] ";
            }
            if (title != null) {
                resultString += title + " ";
            }
            if (confidence != null) {
                resultString += String.format("(%.1f%%) ", confidence * 100.0f);
            }
            return resultString.trim();
        }
    };

    public Classification(Context context) {
        this.context = context;
    }

    /* load model */
    @WorkerThread
    public void load(){
        loadModel();
        loadDictionary();
        loadLabels();
    }

    @WorkerThread
    private synchronized void loadModel() {
        try {
            ByteBuffer buffer = loadModelFile(this.context.getAssets());
            tflite = new Interpreter(buffer);
            Log.v(TAG, "TFLite model loaded.");
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    /* Load words dictionary. */
    @WorkerThread
    private synchronized void loadDictionary() {
        try {
            loadDictionaryFile(this.context.getAssets());
            Log.v(TAG, "Dictionary loaded.");
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    /* Load labels. */
    @WorkerThread
    private synchronized void loadLabels() {
        try {
            loadLabelFile(this.context.getAssets());
            Log.v(TAG, "Labels loaded.");
        } catch (IOException ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    @WorkerThread
    public synchronized void unload() {
        tflite.close();
        dic.clear();
        labels.clear();
    }

    @WorkerThread
    public synchronized List<Result> classify(String text) {
        // 전처리
        String pos_text = posTag(text);
        float[][] input = tokenizeInputText(pos_text);

        // inference 실행
        Log.v(TAG, "Classifying text with TF Lite...");
        float[][] output = new float[1][labels.size()];
        tflite.run(input, output);

        // best 분류 결과 찾기
        PriorityQueue<Result> pq = new PriorityQueue<>(MAX_RESULTS, (lhs, rhs) -> Float.compare(rhs.getConfidence(), lhs.getConfidence()));
        for (int i = 0; i < labels.size(); i++) {
            pq.add(new Result("" + i, labels.get(i), output[0][i]));
        }
        final ArrayList<Result> results = new ArrayList<>();
        while (!pq.isEmpty()) {
            results.add(pq.poll());
        }

        // Return the probability of each class
        return results;
    }

    /* Load Tf Lite Model */
    private static MappedByteBuffer loadModelFile(AssetManager assetManager) throws IOException {
        try (AssetFileDescriptor fileDescriptor = assetManager.openFd(MODEL_PATH);
             FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor())) {
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    /* Load dictionary from assets. */
    private void loadLabelFile(AssetManager assetManager) throws IOException {
        try (InputStream ins = assetManager.open(LABEL_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(ins))) {
            // Each line in the label file is a label.
            while (reader.ready()) {
                labels.add(reader.readLine());
            }
        }
    }

    /* Load labels from assets. */
    private void loadDictionaryFile(AssetManager assetManager) throws IOException {
        try (InputStream ins = assetManager.open(DIC_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(ins))) {
            // dic의 컬럼1 단어, 컬럼1 인덱스
            while (reader.ready()) {
                List<String> line = Arrays.asList(reader.readLine().split(" "));
                if (line.size() < 2) {
                    continue;
                }
                dic.put(line.get(0), Integer.parseInt(line.get(1)));
            }
        }
    }

    /* Pre-prosessing: 토큰화 및 입력 단어 사전의 숫자로 매핑 */
    float[][] tokenizeInputText(String text) {
        float[] tmp = new float[SENTENCE_LEN];
        List<String> array = Arrays.asList(text.split(" "));

        int index = 0;

        for (String word : array) {
            if (index >= SENTENCE_LEN) {
                break;
            }
            tmp[SENTENCE_LEN - array.size() + index++] = dic.containsKey(word) ? dic.get(word) : (int) dic.get(UNKNOWN);
        } // tf모델에서 앞에서부터 0으로 패딩했기 때문에 똑같이

        float[][] ans = {tmp};
        return ans;
    }

    public synchronized String posTag(String text) {
        String result = "";
        KeywordExtractor ke = new KeywordExtractor();
        KeywordList kl = ke.extractKeyword(text, false);
        for( int i = 0; i < kl.size(); i++ ){
            Keyword kwrd = kl.get(i);
            if( kwrd.isTagOf(POSTag.NNG) ){
                result += kwrd.getString() + " ";
            }
            else if( kwrd.isTagOf(POSTag.VV) || kwrd.isTagOf(POSTag.VA) ) {
                result += kwrd.getString() + "다 ";
            }
        }
        return result;
    }


    Map<String, Integer> getDic() {
        return this.dic;
    }

    Interpreter getTflite() {
        return this.tflite;
    }

    List<String> getLabels() {
        return this.labels;
    }

}

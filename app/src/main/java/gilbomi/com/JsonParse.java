package gilbomi.com;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class JsonParse extends AppCompatActivity {
    public static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_healthcare);
    }

    public JsonParse(Context mContext) { this.mContext = mContext; }

    public String getDiseaseName(String user_symptom) {
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

//                String s = "증상 " + user_symptom + "과 관련된 질환은";
//                tv.append(s);
//                tv.append("\n");

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

    //public String getDiseaseInfo(String disease_name) {}
}

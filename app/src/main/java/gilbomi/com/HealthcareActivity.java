package gilbomi.com;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class HealthcareActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_healthcare);
    }

//    private void getJson(int index) {
//        try {
//            InputStream is = getAssets().open("질환DB.json");
//            InputStreamReader isr = new InputStreamReader(is);
//            BufferedReader reader = new BufferedReader(isr);
//
//            StringBuffer buffer = new StringBuffer();
//            String line = reader.readLine();
//            while (line != null) {
//                buffer.append(line+"\n");
//                line = reader.readLine();
//            }
//
//            String jsonData = buffer.toString();
//
//            try {
//                String s = "";
//
//                JSONArray jsonArray = new JSONArray(jsonData);
//                JSONObject jsonObject = jsonArray.getJSONObject(index);
//
//                String name = jsonObject.getString("name");
//                String definition = jsonObject.getString("definition");
//
//                s += "증상명 : " + name + "\n" + "정의 : " + definition + "\n";
//                tv.setText(s);
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}

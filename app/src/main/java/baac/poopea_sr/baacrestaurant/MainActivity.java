package baac.poopea_sr.baacrestaurant;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    //Explicit
    private UserTABLE objUserTABLE;
    private FoodTABLE objFoodTABLE;
    private EditText userEditText, passwordEditText;
    private String userString, passwordString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Bind Widget

        bindWidget();
                
        //Create & Connect Database
        createAndConnected();

        //Tester Add New Value
        //testerAdd();

        //Delete All SQLite
        deleteAllSQLite();

        //Synchronize JSON to SQLite
        synJSONtoSQLite();


    }   //Main Method

    private void bindWidget() {
        userEditText = (EditText) findViewById(R.id.editText);
        passwordEditText = (EditText) findViewById(R.id.editText2);
    }

    public void clickLogin(View view) {

        userString = userEditText.getText().toString().trim();
        passwordString = passwordEditText.getText().toString().trim();

        if (userString.equals("") || passwordString.equals("") ) {
            //Have Space
            errorDialog("Have Space","Please Fill All Every Blank");
        } else {
            //No Space
            checkUser();
        }

    }

    private void checkUser() {
        try {

            String[] strMyresult = objUserTABLE.searchUser(userString);
            if (passwordString.equals(strMyresult[2])) {
                Toast.makeText(MainActivity.this,"Welcome " + strMyresult[3],Toast.LENGTH_LONG).show();
            } else {
                errorDialog("Password False","Please Try again Password False");
            }
        } catch (Exception e) {
            errorDialog("No This User","No " + userString + e.toString() + " on my Database");
        }

    }

    private void errorDialog(String strTitle, String strMessage) {
        AlertDialog.Builder objBuilder = new AlertDialog.Builder(this);
        objBuilder.setIcon(R.drawable.danger);
        objBuilder.setTitle(strTitle);
        objBuilder.setMessage(strMessage);
        objBuilder.setCancelable(false);
        objBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        objBuilder.show();
    }

    private void synJSONtoSQLite() {
        //0 Change Policy
        StrictMode.ThreadPolicy myPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(myPolicy);

        int intTimes = 0;
        while (intTimes <= 1) {
            InputStream objInputStream = null;
            String strJSON = null;
            String strUserURL = "http://swiftcodingthai.com/baac/php_get_data_master.php";
            String strFoodURL = "http://swiftcodingthai.com/baac/php_get_food.php";
            HttpPost objHttpPost;

            //1 create InputStream
            try {

                HttpClient objHttpClient = new DefaultHttpClient();
                switch (intTimes) {
                    case 0:
                        objHttpPost = new HttpPost(strUserURL);
                         break;
                    case 1:
                        objHttpPost = new HttpPost(strFoodURL);
                         break;
                    default:
                        objHttpPost = new HttpPost(strUserURL);
                        break;
                    }   //Switch

                HttpResponse objHttpResponse = objHttpClient.execute(objHttpPost);
                HttpEntity objHttpEntity = objHttpResponse.getEntity();
                objInputStream = objHttpEntity.getContent();

            } catch (Exception e) {
                Log.d("baac", "InputStream ==> " + e.toString());
            }
            //2 Create JSON String
            try {
                BufferedReader objBufferedReader = new BufferedReader(new InputStreamReader(objInputStream, "UTF-8"));
                StringBuilder objStringBuilder = new StringBuilder();
                String strLine = null;

                while ((strLine = objBufferedReader.readLine()) !=null) {

                    objStringBuilder.append(strLine);


                }   //while

                    objInputStream.close();
                    strJSON = objStringBuilder.toString();
            } catch (Exception e) {
                Log.d("baac", "strJSON ==> " + e.toString());
            }

            //3 Update SQLite
            try {
                JSONArray objJsonArray = new JSONArray(strJSON);

                for (int i = 0; i < objJsonArray.length(); i++) {
                    JSONObject object = objJsonArray.getJSONObject(i);
                    switch (intTimes) {
                        case 0:
                            // for userTABLE
                            String strUser = object.getString("User");
                            String strPassword = object.getString("Password");
                            String strName = object.getString("Name");
                            objUserTABLE.addNewUser(strUser, strPassword, strName);
                            break;
                        case 1:
                            // for foodTABLE
                            String strFood = object.getString("Food");
                            String strSoruce = object.getString("Source");
                            String strPrice = object.getString("Price");
                            objFoodTABLE.addNewFood(strFood, strSoruce, strPrice);

                            break;
                    }
                }

            } catch (Exception e) {
                Log.d("baac", "Update ==> " + e.toString());
            }
            intTimes += 1;
        }
    }   //synJSONtoSQLite

    private void deleteAllSQLite() {

        SQLiteDatabase objSqLiteDatabase = openOrCreateDatabase("BAAC.db",MODE_PRIVATE,null);
        objSqLiteDatabase.delete("userTABLE", null, null);
        objSqLiteDatabase.delete("foodTABLE", null, null);

    }

    private void testerAdd() {
        objUserTABLE.addNewUser("testUser", "testPassword", "ทดสอบชื่อภาษาไทย");
        objFoodTABLE.addNewFood("ชื่ออาหาร", "testSource", "123");

    }

    private void createAndConnected() {

        objUserTABLE = new UserTABLE(this);
        objFoodTABLE = new FoodTABLE(this);

    }

}   //Main Class

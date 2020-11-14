package com.example.mlcurrency;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerLocalModel;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
{
    Double INDtoEURO = 0.0;
    Double AmountEuros = 0.0;
    final static String JSON_URL = "http://data.fixer.io/api/latest?access_key=a68cd10e999f5a22a7838bf91f240ce3&format=1";
    ArrayList<Double> rates;
    ArrayList<String> countryCode;
    ArrayList<Double> convertedValues;
    private Adaptor adaptor;
    TextView textView;
    TextView textPro;
    ProgressBar pro;
    ImageView resultTV;
    ImageLabeler labeler;
    RecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rates = new ArrayList<>();
        countryCode = new ArrayList<>();
        convertedValues = new ArrayList<>();

        pro = findViewById(R.id.progress);
        pro.setVisibility(View.INVISIBLE);

        Toolbar mTool = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mTool);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Pict-Currency");

        resultTV = findViewById(R.id.image);

        recyclerView = findViewById(R.id.currencyDetails);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        textView = findViewById(R.id.detectedCost);
        textView.setText("No Pic Selected");
        textView.setTextColor(Color.RED);

        textPro = findViewById(R.id.introduction);


        FloatingActionButton picAdd = findViewById(R.id.floatingActionButton);



        LinearLayout btmSheetLayout = findViewById(R.id.bottomSheet);
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(btmSheetLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        bottomSheetBehavior.setPeekHeight(300);
        bottomSheetBehavior.setHideable(false);




        textPro.setText("Fetching Realtime Currency");
        pro.setVisibility(View.VISIBLE);

        fetchData();



        AutoMLImageLabelerLocalModel localModel =
                new AutoMLImageLabelerLocalModel.Builder()
                        .setAssetFilePath("model/manifest.json")
                        // or .setAbsoluteFilePath(absolute file path to manifest file)
                        .build();

        AutoMLImageLabelerOptions autoMLImageLabelerOptions =
                new AutoMLImageLabelerOptions.Builder(localModel)
                        .setConfidenceThreshold(0.5f)  // Evaluate your model in the Firebase console
                        // to determine an appropriate value.
                        .build();
        labeler = ImageLabeling.getClient(autoMLImageLabelerOptions);





        picAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent OpenGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(OpenGallery,121);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 121)
        {
            if(data != null)
            {
                resultTV.setImageURI(data.getData());

                InputImage image;
                try {
                    image = InputImage.fromFilePath(getApplicationContext(), Objects.requireNonNull(data.getData()));
                    labeler.process(image)
                            .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                                @Override
                                public void onSuccess(List<ImageLabel> labels) {
                                    // Task completed successfully
                                    // ...
                                    for (ImageLabel label : labels) {
                                        String text = label.getText();
                                        float confidence = label.getConfidence();
                                        int index = label.getIndex();
                                        if(confidence < 0.7)
                                        {
                                            textPro.setText("Image Is not Distinguishable");
                                        }
                                        else
                                        {
                                            textPro.setText("Image Detected, Please Wait for Currency Conversion");
                                            pro.setVisibility(View.VISIBLE);
                                            Double indianValue = indianValue = Double.valueOf(text);
                                            textView.setText(text + " Indian Rs");
                                            textView.setTextColor(Color.parseColor("#4CAF50"));

                                            EuroConverter(indianValue);

                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Task failed with an exception
                                    // ...
                                    textPro.setText("Error in Model :( Retry");
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                textPro.setText("Image Couldn't Add :( Retry!");
            }
        }
    }

    public void fetchData()
    {
        StringRequest request = new StringRequest(Request.Method.GET, JSON_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                rates.clear();
                countryCode.clear();

                try
                {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONObject jsonRates = jsonObject.getJSONObject("rates");
                    Iterator<String> it = jsonRates.keys();
                    while(it.hasNext())
                    {
                        String key = it.next();
                        if(key.equals("INR"))
                        {
                            INDtoEURO = Double.parseDouble(jsonRates.getString(key));
                        }
                        rates.add(Double.parseDouble(jsonRates.getString(key)));
                        countryCode.add(key);
                    }

                }catch (JSONException e)
                {
                    e.printStackTrace();
                }

                Log.i("IND RAtIO,.,.,.,.,.,.<>",String.valueOf(rates.size()));


                textPro.setText("Ready With Model , Add Picture");

                pro.setVisibility(View.INVISIBLE);
                //Double indianValue = 10.0;
                //textView.setText(String.valueOf(indianValue));
                //EuroConverter(indianValue);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.i("fetchData Function .,.,.,.,", Objects.requireNonNull(error.getMessage()));
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    public void EuroConverter(Double indianValue)
    {
        convertedValues.clear();
        AmountEuros = indianValue/INDtoEURO;
        for(int i = 0 ; i < rates.size() ; i++)
        {
            convertedValues.add(rates.get(i) * AmountEuros);
        }
        Log.i("<><><<>><>",Integer.toString(rates.size()));
        convertedValues.add(indianValue);
        adaptor = new Adaptor(MainActivity.this,countryCode,rates,convertedValues);
        recyclerView.setAdapter(adaptor);
        textPro.setText("Conversion Sucessful");
        pro.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.searchmenu,menu);
        MenuItem searchViewItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView)searchViewItem.getActionView();
        final SearchView searchView1 = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();

        searchView1.setBackgroundColor(Color.TRANSPARENT);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchView1.setBackgroundColor(Color.TRANSPARENT);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                if(s.isEmpty()) {
                    searchView1.setBackgroundColor(Color.TRANSPARENT);
                }
                else{
                    searchView1.setBackgroundColor(Color.rgb(31,31,31));
                }

                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
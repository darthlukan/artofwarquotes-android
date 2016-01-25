package com.brianctomlinson.darthlukan.artofwarquotes;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Art of War Quotes";
    private TextView quote_view;
    private String quote_text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        quote_view = (TextView)findViewById(R.id.quote_view);
        getQuote();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            showAbout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        getQuote();
    }

    public void showAbout() {
        DialogFragment aboutFragment = new AboutDialogFragment();
        aboutFragment.show(getSupportFragmentManager(), TAG);
    }

    static String streamToString(InputStream stream) throws IOException {
        return streamToString(stream, 1024);
    }

    static String streamToString(InputStream stream, int block_size) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] pBuffer = new byte[block_size];

        try {
            for (;;) {
                int res = stream.read(pBuffer);
                if (res == -1) {
                    break;
                }
                if (res > 0) {
                    out.write(pBuffer, 0, res);
                }
            }
            out.close();
            stream.close();

            return out.toString();
        } catch (IOException why) {
            return why.toString();
        }
    }

    protected String quoteRequest() {

        InputStream in;
        HttpsURLConnection conn;
        String quote_url = "https://www.brianctomlinson.com/quotes";
        Uri.Builder uri_builder = Uri.parse(quote_url).buildUpon();

        try {
            conn = (HttpsURLConnection) new URL(uri_builder.toString()).openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();

            int response_code = conn.getResponseCode();

            if (response_code != 200) {
                return null;
            }

            in = conn.getInputStream();
            String str_response = streamToString(in);

            JSONObject json_response = new JSONObject(str_response);
            return json_response.getString("data").replace("\n", "");
        } catch (JSONException | IOException why) {
            why.printStackTrace();
        }
        return null;
    }

    protected void getQuote() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    quote_text = quoteRequest();
                    quote_view.post(new Runnable() {
                        @Override
                        public void run() {
                            quote_view.setText(quote_text);
                        }
                    });
                } catch (Exception why) {
                    why.printStackTrace();
                }
            }
        }).start();
    }

    public void buttonHandler(View view) {
        getQuote();
    }
}

package pl.edu.agh.mobilne.ultrasound.android.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import pl.edu.agh.mobilne.ultrasound.android.lib.ReceiverAndroid;
import pl.edu.agh.mobilne.ultrasound.core.DataReader;


public class MainActivity extends Activity {

    private ReceiverAndroid receiver;

    private DataReader dataReader;

    //private SenderAndroid sender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            final PipedOutputStream os = new PipedOutputStream();
            final PipedInputStream is;
            is = new PipedInputStream(os);
            receiver = new ReceiverAndroid(os);
            //sender = new SenderAndroid(new byte[] {0});
            new Thread(receiver).start();

            dataReader = new DataReader(is);
            new Thread(dataReader).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}


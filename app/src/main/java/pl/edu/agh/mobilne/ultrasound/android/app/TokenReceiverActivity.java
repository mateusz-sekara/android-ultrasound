package pl.edu.agh.mobilne.ultrasound.android.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class TokenReceiverActivity extends ActionBarActivity {

    private boolean isStarted = false;

    private Button startReceivingButton;
    private Button stopReceivingButton;
    private TextView tokenValueTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_receiver);

        startReceivingButton = (Button) findViewById(R.id.startReceiveButton);
        stopReceivingButton = (Button) findViewById(R.id.stopReceiveButton);
        tokenValueTextView = (TextView) findViewById(R.id.tokenValueTextView);

        updateButtons();
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

    public void startReceivingToken(View view) {
        isStarted = true;

        updateButtons();
    }

    public void stopReceivingToken(View view) {
        isStarted = false;

        updateButtons();
    }

    private void updateButtons() {
        startReceivingButton.setEnabled(!isStarted);
        stopReceivingButton.setEnabled(isStarted);
    }
}

package pl.edu.agh.mobilne.ultrasound.android.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import pl.edu.agh.mobilne.ultrasound.android.app.model.Token;
import pl.edu.agh.mobilne.ultrasound.android.lib.receive.ReceiverService;


public class TokenReceiverActivity extends ActionBarActivity {

    private boolean isStarted = false;
    private Token token;

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

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(serviceBroadcastReceiver, new IntentFilter(ReceiverService.NOTIFICATION_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(serviceBroadcastReceiver);
    }

    public void startReceivingToken(View view) {
        isStarted = true;

        updateButtons();
        startReceiverService();
    }

    public void stopReceivingToken(View view) {
        isStarted = false;

        updateButtons();
        stopReceiverService();
    }

    private void updateButtons() {
        startReceivingButton.setEnabled(!isStarted);
        stopReceivingButton.setEnabled(isStarted);
    }

    private void startReceiverService() {
        Intent intent = new Intent(this, ReceiverService.class);
        startService(intent);
    }

    private void stopReceiverService() {
        Intent intent = new Intent(this, ReceiverService.class);
        stopService(intent);
    }

    private void updateToken(Token receiveToken) {
        if (token == null || !receiveToken.equals(token)) {
            tokenValueTextView.setText(receiveToken.toString());
            token = receiveToken;
        }
    }

    private BroadcastReceiver serviceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                byte[] tokenByteArray = bundle.getByteArray(ReceiverService.BYTE_BUFFER_KEY);
                Token receivedToken = new Token(tokenByteArray);
                updateToken(receivedToken);
            }
        }
    };
}

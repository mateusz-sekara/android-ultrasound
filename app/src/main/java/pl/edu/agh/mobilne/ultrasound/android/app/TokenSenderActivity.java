package pl.edu.agh.mobilne.ultrasound.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import pl.edu.agh.mobilne.ultrasound.android.lib.send.SenderService;
import pl.edu.agh.mobilne.ultrasound.core.TokenGenerator;


public class TokenSenderActivity extends ActionBarActivity {

    private boolean isStarted = false;

    private Button startSendingButton;
    private Button stopSendingButton;
    private Button generateTokenButton;
    private TextView tokenEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_sender);

        startSendingButton = (Button) findViewById(R.id.startSendButton);
        stopSendingButton = (Button) findViewById(R.id.stopSendButton);
        generateTokenButton = (Button) findViewById(R.id.generateTokenButton);
        tokenEditText = (TextView) findViewById(R.id.tokenEditText);

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

    public void startSendingToken(View view) {
        isStarted = true;

        updateButtons();
        startSenderService(tokenEditText.getText().toString());
    }

    public void stopSendingToken(View view) {
        isStarted = false;

        updateButtons();
        stopSenderService();
    }

    public void generateToken(View view) {
        String generatedToken = TokenGenerator.getStringToken();

        tokenEditText.setText(generatedToken);
    }

    private void startSenderService(String tokenString) {
        byte[] tokenData = TokenGenerator.convertFromString(tokenString);

        Intent intent = new Intent(this, SenderService.class);
        intent.putExtra(SenderService.BYTE_BUFFER_KEY, tokenData);
        startService(intent);
    }

    private void stopSenderService() {
        Intent intent = new Intent(this, SenderService.class);
        stopService(intent);
    }

    private void updateButtons() {
        startSendingButton.setEnabled(!isStarted);
        stopSendingButton.setEnabled(isStarted);
        generateTokenButton.setEnabled(!isStarted);
    }
}

package network.manning.msi.com;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Android direct to Socket example.
 * <p>
 * For this to work you need a server listening on the IP address and port specified. See the
 * NetworkSocketServer project for an example.
 *
 * @author charliecollins
 */
public class SimpleSocket extends Activity {

    private static final String CLASSTAG = SimpleSocket.class.getSimpleName();
    private static final String SERVER_IP = "192.168.1.2";
    private Handler h;
    private EditText ipAddress;
    private EditText port;
    private EditText socketInput;
    private TextView socketOutput;


    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        this.setContentView(R.layout.simple_socket);

        Button socketMessageButton;
        Button socketRunnableButton;

        this.ipAddress = (EditText) findViewById(R.id.socket_ip);
        this.port = (EditText) findViewById(R.id.socket_port);
        this.socketInput = (EditText) findViewById(R.id.socket_input);
        this.socketOutput = (TextView) findViewById(R.id.socket_output);
        socketMessageButton = (Button) findViewById(R.id.socket_message_button);
        socketRunnableButton = (Button) findViewById(R.id.socket_runnable_button);

//        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());

        h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        socketOutput.append("Message -> " + msg.obj + '\n');
                        break;
                }
                super.handleMessage(msg);
            }
        };
        socketMessageButton.setOnClickListener(new OnClickListener() {

            public void onClick(final View v) {
//                socketOutput.setText("");
                try {
                    InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
//                    InetAddress serverAddr = InetAddress.getByName(ipAddress.getText().toString());
//                    String output = callSocket(serverAddr, port.getText().toString(), socketInput.getText().toString());
                    callSocket(serverAddr, port.getText().toString(), socketInput.getText().toString(), Type.MESSAGE);
//                    socketOutput.setText(output);
                } catch (java.net.UnknownHostException e1) {
                    e1.printStackTrace();
                }
            }
        });

        socketRunnableButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                socketOutput.setText("");
                try {
                    InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
//                    InetAddress serverAddr = InetAddress.getByName(ipAddress.getText().toString());
//                    String output = callSocket(serverAddr, port.getText().toString(), socketInput.getText().toString());
                    callSocket(serverAddr, port.getText().toString(), socketInput.getText().toString(), Type.RUNNABLE);
//                    socketOutput.setText(output);
                } catch (java.net.UnknownHostException e1) {
                    e1.printStackTrace();
                }
            }
        });


        socketMessageButton.callOnClick();
    }

    private void callSocket(final InetAddress ad, final String port, final String socketData, final Type type) {


        new Thread() {
            @Override
            public void run() {
                Socket socket = null;
                BufferedWriter writer = null;
                BufferedReader reader = null;
                String output;
                try {
                    socket = new Socket(ad, Integer.parseInt(port));
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    // send input terminated with \n
                    String input = socketData;
                    writer.write(input + "\n", 0, input.length() + 1);
                    writer.flush();
                    // read back output
                    output = reader.readLine();
                    switch (type) {
                        case MESSAGE:
                            Message message = new Message();
                            message.what = 0;
                            message.obj = output;
                            h.sendMessage(message);
                            break;
                        case RUNNABLE:
                            h.post(new updateUIThread(output));
                            break;
                    }
                    Log.d(getText(R.string.log_tag).toString(), " " + SimpleSocket.CLASSTAG + " output - " + output);
                    // send EXIT and close
                    writer.write("EXIT\n", 0, 5);
                    writer.flush();
                } catch (IOException e) {
                    Log.e(getText(R.string.log_tag).toString(), " " + SimpleSocket.CLASSTAG + " IOException calling socket", e);
                } finally {
                    try {
                        if (writer != null)
                            writer.close();
                    } catch (IOException e) { // swallow
                    }
                    try {
                        if (reader != null)
                            reader.close();
                    } catch (IOException e) { // swallow
                    }
                    try {
                        if (socket != null)
                            socket.close();
                    } catch (IOException e) { // swallow
                    }
                }
//        return output;
            }
        }.start();
    }

    private enum Type {
        MESSAGE,
        RUNNABLE
    }

    private class updateUIThread implements Runnable {
        private final String output;

        public updateUIThread(String output) {
            this.output = output;
        }

        @Override
        public void run() {
            socketOutput.append("Runnable -> " + output + '\n');
        }
    }
}

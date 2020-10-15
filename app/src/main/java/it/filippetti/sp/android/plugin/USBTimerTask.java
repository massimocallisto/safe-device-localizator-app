package it.filippetti.sp.android.plugin;

import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialPort;

import java.util.TimerTask;

import it.smartspace.common.util.ConversionUtility;
import it.smartspace.jz.communication.ZFrame;
import it.smartspace.jz.communication.io.SSTokenizer;

public class USBTimerTask extends TimerTask {
    private String TAG = "AAA_" + USBTimerTask.class.getSimpleName();
    private static final int TIMEOUT_READ = 1000;
    private static final int TIMEOUT_WRITE = 1000;

    private UsbSerialPort usbSerialPort;
    private byte inputBuffer[] = new byte[USBConnector.DEFAULT_BAUD_RATE / 8];
    private SSTokenizer inputTokenizer = new SSTokenizer();
    private SSTokenizer outputTokenizer = new SSTokenizer();
    private byte sequenceNumber = 0;

    public USBTimerTask(UsbSerialPort port) {
        this.usbSerialPort = port;


    }

    public SSTokenizer getTokenizerInput() {
        return inputTokenizer;
    }

    public SSTokenizer getTokenizerOutput() {
        return outputTokenizer;
    }


    @Override
    public void run() {
        try {
            //Log.d(TAG, "reading from " + port + "...");

            int numBytesRead = usbSerialPort.read(inputBuffer, TIMEOUT_READ);
//            Log.d(TAG, "... read " + numBytesRead + " bytes from " + usbSerialPort);

            if (numBytesRead > 0) {
                Log.d(TAG, "... read " + ConversionUtility.toHexString(inputBuffer, 0 , numBytesRead, "") + " bytes from " + usbSerialPort);
                inputTokenizer.process(inputBuffer, 0, numBytesRead);
            }
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean write(ZFrame frame) throws Exception {
        frame.getZPacket().setSequenceNumber(sequenceNumber++);
        return write(frame.serialize().array());
    }


    public boolean write(byte[] token) throws Exception {
        usbSerialPort.write(token, TIMEOUT_WRITE);

        SSTokenizer.SSTokenizerState tokenizerState = outputTokenizer.process(token);
        return SSTokenizer.SSTokenizerState.SOP_STATE.equals(tokenizerState);

    }

}

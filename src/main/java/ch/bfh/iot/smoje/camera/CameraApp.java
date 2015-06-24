package ch.bfh.iot.smoje.camera;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class CameraApp {

    private static final String BROKER_URL = "tcp://smoje.ch:1883";
    private static final String TOPIC = "selfie/klatsch";

     private static final String PATH_PICS = "/home/pi/smoje/cam/";
//    private static final String PATH_PICS = "/home/adrian/pi/";


    MqttClient mqttClient;
    
    Logger log = Logger.getLogger(CameraApp.class.getName());

    public static void main(String[] args) throws MqttException {

        new CameraApp().start();
    }

    private void start() throws MqttException {
        mqttClient = new MqttClient(BROKER_URL, "SelfieSmojeCam");
        mqttClient.connect();
        log.log(Level.INFO, "Connected");
        mqttClient.setCallback(new MqttCallback() {

            public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
                log.log(Level.INFO, "Msg arrived " + arg0 + "  " + new String(arg1.getPayload()));

                File photo = takePhoto();

                if (photo != null) {
                    httpPostPhoto(photo);
                }
            }

            public void deliveryComplete(IMqttDeliveryToken arg0) {
                // TODO Auto-generated method stub
            }

            public void connectionLost(Throwable arg0) {
                // TODO Auto-generated method stub
            }
        });

        mqttClient.subscribe(TOPIC);

    }

    private File takePhoto() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String filename = df.format(new Date()) + ".jpg";

        String cmd = "fswebcam -r 1920x1080 --no-banner " + PATH_PICS + filename;
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        log.log(Level.INFO, "Photo taken: " + PATH_PICS + filename);
        return new File(PATH_PICS + filename);
    }
    
    private void httpPostPhoto(File photo) {
        String cmd = "curl -i -F name=test -F file[]=@" + photo.getAbsolutePath() + " http://siot.ch/upload.php";
        System.out.println(cmd);
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        log.log(Level.INFO, "Photo uploaded");
    }
    




}

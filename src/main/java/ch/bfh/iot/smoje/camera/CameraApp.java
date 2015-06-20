package ch.bfh.iot.smoje.camera;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gdata.client.photos.PicasawebService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.media.MediaFileSource;
import com.google.gdata.data.photos.PhotoEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class CameraApp {

    public static void main(String[] args) throws MqttException {

        new CameraApp().start();
    }

    private void start() throws MqttException {
        MqttClient mqttClient = new MqttClient("tcp://smoje.ch:1883", "SelfieSmojeCam");
        mqttClient.connect();
        mqttClient.setCallback(new MqttCallback() {

            public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
                System.out.println("msg arrived " + arg0 + "  " + new String(arg1.getPayload()));
                
                File photo = takePhoto();

                if(photo != null) {
                    uploadPhoto(photo);
                }
            }

            public void deliveryComplete(IMqttDeliveryToken arg0) {
                // TODO Auto-generated method stub
            }

            public void connectionLost(Throwable arg0) {
                // TODO Auto-generated method stub
            }
        });

        mqttClient.subscribe("selfie/klatsch");

    }

    private File takePhoto() {
//        String pathname = "/home/pi/smoje/cam/";
        String pathname = "/home/adrian/pi/";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String filename = df.format(new Date()) + ".jpg";

        String cmd = "fswebcam -r 1920x1080 --no-banner " + pathname + filename;
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return new File(pathname + filename);
    }

    private void uploadPhoto(File photo) {
        PicasawebService service = new PicasawebService("bfh-SelfieSmoje");
        try {
            service.setUserCredentials("baertschi.adrian@gmail.com", "rthrdgrlimkgluah");
        } catch (AuthenticationException e) {
            e.printStackTrace();
        }
        
        URL albumPostUrl = null;
        try {
            albumPostUrl = new URL("https://picasaweb.google.com/data/feed/api/user/109802042458700177651/albumid/6162378741581285969");
            // old:                 https://picasaweb.google.com/data/feed/api/user/109802042458700177651/albumid/6130961662863629409
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        PhotoEntry myPhoto = new PhotoEntry();
        myPhoto.setTitle(new PlainTextConstruct(new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date())));
        myPhoto.setDescription(new PlainTextConstruct("BFH Selfie Smoje asut Seminar 2015"));
        myPhoto.setClient("smoje");

        MediaFileSource myMedia = new MediaFileSource(photo, "image/jpeg");
        myPhoto.setMediaSource(myMedia);

        try {
            PhotoEntry returnedPhoto = service.insert(albumPostUrl, myPhoto);
        } catch (IOException | ServiceException e) {
            e.printStackTrace();
        }
    }

}

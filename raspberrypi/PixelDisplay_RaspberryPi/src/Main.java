import com.pi4j.io.gpio.*;
import communication.BluetoothCommunication;
import communication.WifiConnectionChecker;

import java.io.File;

public class Main {
    private static File image;

    public static void main(String[] args) {
        Camera camera = Camera.getInstance();

        GpioController gpio = GpioFactory.getInstance();
        GpioPinDigitalOutput ir_led_pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "PinLED", PinState.LOW);
        ir_led_pin.setShutdownOptions(true, PinState.LOW);

        DistanceSensor distanceSensor = DistanceSensor.getInstance(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07), gpio.provisionDigitalInputPin(RaspiPin.GPIO_00));
        distanceSensor.start();

        BluetoothCommunication communication = BluetoothCommunication.getInstance();
        communication.setOnMessageReceivedListener(message -> {
            switch (message) {
                case "capture":
                    ir_led_pin.high();
                    image = camera.takeStill();
                    ir_led_pin.low();
                    communication.sendImage(image, distanceSensor.getDistance());
                    break;
                case "cancel":
                    communication.setSendingImage(false);
                    break;
            }
        });
        communication.start();

        WifiConnectionChecker wifiConnectionChecker = WifiConnectionChecker.getInstance();
        wifiConnectionChecker.start();

        try {
            communication.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            wifiConnectionChecker.close();
            communication.close();
            distanceSensor.close();
            gpio.shutdown();
            camera.close();
        }
    }
}
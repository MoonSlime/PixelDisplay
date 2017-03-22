import com.pi4j.io.gpio.*;

class DistanceSensor extends Thread {

    private final static float SOUND_SPEED = 340.29f;  // speed of sound in m/s
    private final static int TRIG_DURATION_IN_MICROS = 10; // trigger duration of 10 micro s
    private final static int WAIT_DURATION_IN_MILLIS = 60; // wait 60 milli s
    private final static int TIMEOUT = 2100;

    private GpioPinDigitalInput echoPin;
    private GpioPinDigitalOutput trigPin;

    private float distance = 0.0f;

    private static DistanceSensor instance;

    private DistanceSensor(GpioPinDigitalOutput trigPin, GpioPinDigitalInput echoPin) {
        this.trigPin = trigPin;
        this.echoPin = echoPin;

        trigPin.setShutdownOptions(true, PinState.LOW);
        echoPin.setShutdownOptions(true, PinState.LOW);
        trigPin.low();
    }

    static DistanceSensor getInstance(GpioPinDigitalOutput trigPin, GpioPinDigitalInput echoPin) {
        if (instance == null) {
            instance = new DistanceSensor(trigPin, echoPin);
        }

        return instance;
    }

    @Override
    public void run() {
        super.run();

        while (!isInterrupted()) {
            updateDistance();

            try {
                Thread.sleep(300);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    float getDistance() {
        return distance;
    }

    private void updateDistance() {
        float distance = 0.0f;
        try {
            Thread.sleep(WAIT_DURATION_IN_MILLIS);

            triggerSensor();
            waitForSignal();
            long duration = measureDuration();

            distance = duration * SOUND_SPEED / (2 * 1000000); // m
        } catch (Exception e) {
            if (!(e instanceof TimeoutException)) {
                System.out.println(e.getClass().getSimpleName());
                e.printStackTrace();
            }
        }

        if (distance != 0.0f) {
            this.distance = distance;
        }
    }

    private void triggerSensor() {
        try {
            trigPin.high();
            Thread.sleep(0, TRIG_DURATION_IN_MICROS * 1000);
            trigPin.low();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    private void waitForSignal() throws TimeoutException {
        int countdown = TIMEOUT;

        while (this.echoPin.isLow() && countdown > 0) {
            countdown--;
        }

        if (countdown <= 0) {
            throw new TimeoutException("Timeout waiting for signal start");
        }
    }

    private long measureDuration() throws TimeoutException {
        int countdown = TIMEOUT;
        long start = System.nanoTime();
        while (this.echoPin.isHigh() && countdown > 0) {
            countdown--;
        }
        long end = System.nanoTime();

        if (countdown <= 0) {
            throw new TimeoutException("Timeout waiting for signal end");
        }

        return (long) Math.ceil((end - start) / 1000.0);  // Return micro seconds
    }

    private static class TimeoutException extends Exception {

        private final String reason;

        TimeoutException(String reason) {
            this.reason = reason;
        }

        @Override
        public String toString() {
            return this.reason;
        }
    }

    public void close() {
        if (instance != null) {
            trigPin.low();
            instance = null;
        }
    }
}
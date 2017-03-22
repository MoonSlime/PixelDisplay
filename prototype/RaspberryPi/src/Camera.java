import com.hopding.jrpicam.enums.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

class Camera {
    static final String picturePath = "/home/pi/Desktop/face.jpg";
    static final Size maxSize = new Size(2592, 1944);
    static final Size selectedSize = new Size(1944, 1458);
    private HashMap<String, String[]> options;
    private static Camera instance;

    private Camera() {
        options = new HashMap<>();
        this.setISO(800)
                .setShutter(200000)
                .setTimeout(1)
                .setQuality(75)
                .setEncoding(Encoding.JPG)
                .turnOffPreview()
                .setFullPreviewOff()
                .turnOffThumbnail();
    }

    static Camera getInstance() {
        if (instance == null) {
            instance = new Camera();
        }
        return instance;
    }

    File takeStill() {
        try {
            ArrayList<String> command = makeCommand(selectedSize, picturePath);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();
            process.waitFor();
        } catch (Exception e) {
            System.out.println(e.getClass().getSimpleName());
            e.printStackTrace();
        }

        return new File(picturePath);
    }

    BufferedImage takeBufferedStill() {
        BufferedImage bufferedImage = null;
        try {
            ArrayList<String> command = makeCommand(new Size(1296, 972));
            System.out.println(command.toString());

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            bufferedImage = ImageIO.read(process.getInputStream());
            process.getInputStream().close();
        } catch (Exception e) {
            System.out.println(e.getClass().getSimpleName());
            e.printStackTrace();
        }

        return bufferedImage;
    }

    private ArrayList<String> makeCommand(Size size, String path) {
        ArrayList<String> command = new ArrayList<>();
        command.add("raspistill");
        command.add("-o");
        if (path != null) {
            command.add(path);
        } else {
            command.add("-v");
        }
        command.add("-w");
        command.add("" + size.width);
        command.add("-h");
        command.add("" + size.height);

        for (Map.Entry entry : options.entrySet()) {
            if (entry.getValue() != null) {
                Collections.addAll(command, (String[]) entry.getValue());
            }
        }

        return command;
    }

    private ArrayList<String> makeCommand(Size size) {
        return makeCommand(size, null);
    }

    public static class Size {
        final int width;
        final int height;

        Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public String toString() {
            return "Size(" + width + ", " + height + ")";
        }
    }

    void close() {
        if (instance != null) {
            instance = null;
        }
    }


    Camera turnOffPreview() {
        options.put("preview", new String[]{"-n"});
        return this;
    }

    Camera setFullPreviewOff() {
        options.put("fullpreview", null);
        return this;
    }

    Camera setSharpness(int sharpness) {
        if (sharpness > 100) {
            sharpness = 100;
        } else if (sharpness < -100) {
            sharpness = -100;
        }

        options.put("sharpness", new String[]{"-sh", "" + sharpness});
        return this;
    }

    Camera setContrast(int contrast) {
        if (contrast > 100) {
            contrast = 100;
        } else if (contrast < -100) {
            contrast = -100;
        }

        options.put("constast", new String[]{"-co", "" + contrast});
        return this;
    }

    Camera setExposure(Exposure exposure) {
        options.put("exposure", new String[]{"-ex", exposure.toString()});
        return this;
    }

    Camera setAWB(AWB awb) {
        options.put("awb", new String[]{"-awb", awb.toString()});
        return this;
    }

    Camera setDRC(DRC drc) {
        options.put("drc", new String[]{"-drc", drc.toString()});
        return this;
    }

    Camera setQuality(int quality) {
        if (quality > 100) {
            quality = 100;
        } else if (quality < 0) {
            quality = 0;
        }

        options.put("quality", new String[]{"-q", "" + quality});
        return this;
    }

    Camera setTimeout(int time) {
        options.put("timeout", new String[]{"-t", "" + time});
        return this;
    }

    Camera turnOffThumbnail() {
        options.put("thumb", new String[]{"-th", "none"});
        return this;
    }

    Camera setEncoding(Encoding encoding) {
        options.put("encoding", new String[]{"-e", encoding.toString()});
        return this;
    }

    Camera setBrightness(int brightness) {
        if (brightness > 100) {
            brightness = 100;
        } else if (brightness < 0) {
            brightness = 0;
        }

        options.put("brightness", new String[]{"-br", "" + brightness});
        return this;
    }

    Camera setSaturation(int saturation) {
        if (saturation > 100) {
            saturation = 100;
        } else if (saturation < -100) {
            saturation = -100;
        }

        options.put("saturation", new String[]{"-sa", "" + saturation});
        return this;
    }

    Camera setISO(int iso) {
        options.put("ISO", new String[]{"-ISO", "" + iso});
        return this;
    }

    Camera setMeteringMode(MeteringMode meteringMode) {
        // 화각
        options.put("metering", new String[]{"-mm", meteringMode.toString()});
        return this;
    }

    Camera setShutter(int speed) {
        if (speed > 6000000) {
            speed = 6000000;
        }

        if (speed < 0) {
            speed = 0;
        }

        options.put("shutter", new String[]{"-ss", "" + speed});
        return this;
    }
}

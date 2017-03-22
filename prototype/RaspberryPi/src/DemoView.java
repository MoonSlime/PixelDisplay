import com.hopding.jrpicam.enums.Encoding;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

class DemoView {

    private JFrame frame;
    private JButton button_take;
    private JTextField textFiled_timeout;
    private JTextField textFiled_shutter;
    private JSlider slider_iso;
    private BufferedImage bufferedImage;
    private Camera camera;

    /*
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    DemoView window = new DemoView(camera);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    */

    public DemoView(Camera camera) {
        this.camera = camera;
        initialize();
        frame.setVisible(true);
    }

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, Camera.selectedSize.width + 100, Camera.selectedSize.height);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        final JLabel image = new JLabel();
        image.setBounds(100, 0, Camera.selectedSize.width, Camera.selectedSize.height);
        frame.getContentPane().add(image);

        button_take = new JButton("Take");
        button_take.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    camera.setISO(slider_iso.getValue())
                            .setShutter(Integer.parseInt(textFiled_shutter.getText()))
                            .setTimeout(1)
                            .setQuality(75)
                            .setEncoding(Encoding.JPG)
                            .turnOffPreview()
                            .setFullPreviewOff()
                            .turnOffThumbnail();
                    //setAWB(AWB.valueOf(((String) awbComboBox.getSelectedItem()).toUpperCase()))

                    bufferedImage = camera.takeBufferedStill();
                    ImageIcon icon = new ImageIcon(bufferedImage);
                    image.setIcon(icon);

                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(null, "Please Enter a Value for Timeout.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        button_take.setBounds(0, 0, 100, 20);
        frame.getContentPane().add(button_take);

        ////////////////////////////////////////////////
        JLabel label_timeout = new JLabel("timeout :");
        label_timeout.setFont(new Font("Tahoma", Font.BOLD, 10));
        label_timeout.setBounds(0, 40, 100, 20);
        frame.getContentPane().add(label_timeout);

        textFiled_timeout = new JTextField();
        textFiled_timeout.setText("1");
        textFiled_timeout.setBounds(0, 60, 100, 20);
        textFiled_timeout.setColumns(10);
        frame.getContentPane().add(textFiled_timeout);
        ////////////////////////////////////////////////
        JLabel label_shutter = new JLabel("Shutter:");
        label_shutter.setFont(new Font("Tahoma", Font.BOLD, 10));
        label_shutter.setBounds(0, 100, 100, 20);
        frame.getContentPane().add(label_shutter);

        textFiled_shutter = new JTextField();
        textFiled_shutter.setText("200000");
        textFiled_shutter.setBounds(0, 120, 100, 20);
        textFiled_shutter.setColumns(10);
        frame.getContentPane().add(textFiled_shutter);
        ////////////////////////////////////////////////
        JLabel label_iso = new JLabel("ISO:");
        label_iso.setFont(new Font("Tahoma", Font.BOLD, 10));
        label_iso.setBounds(0, 160, 100, 20);
        frame.getContentPane().add(label_iso);

        slider_iso = new JSlider();
        slider_iso.setMinimum(100);
        slider_iso.setMaximum(800);
        slider_iso.setValue(800);
        slider_iso.setBounds(0, 180, 100, 20);
        frame.getContentPane().add(slider_iso);

        /*
        JLabel label_drc = new JLabel("DRC:");
        label_drc.setFont(new Font("Tahoma", Font.BOLD, 10));
        label_drc.setBounds(0, 220, 100, 20);
        frame.getContentPane().add(label_drc);

        JComboBox<String> comboBox_drc = new JComboBox<>(new String[]{"Off", "High", "Medium", "Low"});
        comboBox_drc.setBounds(0, 240, 100, 20);
        frame.getContentPane().add(comboBox_drc);
         */
    }
}

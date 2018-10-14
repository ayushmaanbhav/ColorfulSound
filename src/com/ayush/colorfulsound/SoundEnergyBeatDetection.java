package com.ayush.colorfulsound;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ddf.minim.AudioInput;
import ddf.minim.AudioListener;
import ddf.minim.Minim;
import ddf.minim.analysis.BeatDetect;

public class SoundEnergyBeatDetection {

    Minim minim;
    AudioInput song;
    BeatDetect beat;
    BeatDetect freqBeat;
    BeatListener bl;
    OutputStream output;
    BlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>(10);

    volatile Color defaultColor = Color.RED;
    volatile boolean exit = false;
    volatile boolean threshold1 = false;
    volatile boolean threshold2 = false;
    volatile boolean enabled = true;
    volatile boolean colorEnabled = true;
    volatile float eRadius;
    volatile float r = 0;
    volatile float b = 0;
    volatile float g = 0;
    List<Color> rainbow = new ArrayList<>();
    float dynamicRangeStart = -1;
    float dynamicRangeEnd = -1;

    class BeatListener implements AudioListener {
        private AudioInput source;

        BeatListener(AudioInput source) {
            this.source = source;
            this.source.addListener(this);
        }

        public void samples(float[] samps) {
            draw();
        }

        public void samples(float[] sampsL, float[] sampsR) {
            draw();
        }
    }

    public void setup() {
        rainbow.add(Color.RED);
        rainbow.add(Color.ORANGE);
        rainbow.add(Color.YELLOW);
        rainbow.add(Color.GREEN);
        rainbow.add(Color.BLUE);
        rainbow.add(new Color(75, 0, 130)); //indigo
        rainbow.add(new Color(148, 0, 211)); // violet
        
        this.setupUI();

        ArduinoConnector main = new ArduinoConnector();
        main.initialize();
        output = main.getOutputStream();

        minim = new Minim(this);
        song = minim.getLineIn(Minim.STEREO, 2048);//
        // song =
        // minim.loadFile("F:/Thriller-Michael_Jackson[www.MastJatt.Com].mp3",
        // 2048);
        // song.play();

        // a beat detection object song SOUND_ENERGY mode with a sensitivity of
        // 10 milliseconds
        beat = new BeatDetect();
        beat.setSensitivity(100);

        freqBeat = new BeatDetect(song.bufferSize(), song.sampleRate());
        freqBeat.setSensitivity(100);

        bl = new BeatListener(song);

        eRadius = 500;

        new Thread() {
            public void run() {
                while (true) {
                    try {
                        blockingQueue.take().run();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }.start();

        new Thread() {
            public void run() {
                while (true) {
                    eRadius *= 0.95f;
                    if (eRadius < 50)
                        eRadius = 50;

                    if (enabled) {
                        if (colorEnabled) {
                            sendToArduino((int)r, (int)g, (int)b, eRadius);
                        } else {
                            sendToArduino(defaultColor.getRed(), defaultColor.getGreen(), defaultColor.getBlue(), eRadius);
                        }
                    } else {
                        sendToArduino(defaultColor.getRed(), defaultColor.getGreen(), defaultColor.getBlue(), 500 * ((float)defaultColor.getAlpha()) / 255);
                    }

                    try {
                        Thread.sleep(15);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }.start();
    }

    public void draw() {
        beat.detect(song.mix);
        freqBeat.detect(song.mix);

        /*System.out.println("Size: " + freqBeat.detectSize());
        for (int i = 0; i < freqBeat.detectSize(); i++) {
            System.out.println("Center freq: " + i + " = " + freqBeat.getDetectCenterFrequency(i));
        }
        for (int i = 0; i < freqBeat.detectSize(); i++) {
            System.out.println("Is a Beat: " + i + " = " + freqBeat.isRange(i, i, 1));
        }*/

        /*int red = 0;
        int blue = 0;
        int green = 0;
        if (freqBeat.isKick())
            red = (int) 255;
        if (freqBeat.isSnare())
            green = (int) 255;
        if (freqBeat.isHat())
            blue = (int) 255;

        if (!(red == 0 && blue == 0 && green == 0)) {
            r = red;
            g = green;
            b = blue;
        }*/
        
        int dRangeStart = freqBeat.detectSize() - 1;
        int dRangeEnd = 0;
        for (int i = 0; i < freqBeat.detectSize(); i++) {
            if (freqBeat.isRange(i, i, 1)) {
                if (dRangeStart > i) {
                    dRangeStart = i;
                }
                if (dRangeEnd < i) {
                    dRangeEnd = i;
                }
            }
        }
        if (dRangeStart <= dRangeEnd) {
            if (dynamicRangeEnd == -1) {
                dynamicRangeStart = dRangeStart;
                dynamicRangeEnd = dRangeEnd;
            } else {
                if (dRangeStart < dynamicRangeStart) {
                    dynamicRangeStart = dRangeStart;
                } else {
                    dynamicRangeStart = (dynamicRangeStart * 0.97f + dRangeStart * 0.03f);
                }
                if (dRangeEnd > dynamicRangeEnd) {
                    dynamicRangeEnd = dRangeEnd;
                } else {
                    dynamicRangeEnd = (dynamicRangeEnd * 0.97f + dRangeEnd * 0.03f);
                }
            }
            //System.out.println("Range1: " + dRangeStart + " = " + dRangeEnd);
            //System.out.println("Range2: " + dynamicRangeStart + " = " + dynamicRangeEnd);
            int totaldiff = (int)dynamicRangeEnd - (int)dynamicRangeStart + 1;
            int diff = totaldiff / rainbow.size();
            int rem = totaldiff % rainbow.size();
            Color c = new Color((int)r, (int)g, (int)b);
            List<Color> cs = new ArrayList<>();
            cs.add(c);
            for (int i = (int)dynamicRangeEnd, j = rainbow.size() - 1; i >= (int)dynamicRangeStart; j--) {
                int extra = 0;
                if (rem > 0) {
                    extra = 1;
                    rem--;
                }
                //System.out.println("Data: " + totaldiff + " = " + diff + " = " + rem + " = " + i + " = " + extra + " = " + j);
                int incr = diff + extra;
                int start = (i - incr + 1) > (int)dynamicRangeStart ? (i - incr + 1) : (int)dynamicRangeStart;
                int range = i - start + 1;
                int threshold = range >= 3 && !threshold1 ? (int)Math.ceil(range / 2f) : 1;
                threshold = threshold2 ? range : threshold;
                //System.out.println("Range: " + start + " = " + i + " = " + threshold);
                if (freqBeat.isRange(start, i, threshold)) {
                    cs.add(rainbow.get(j));
                }
                i -= incr;
            }
            if (cs.size() > 0) {
                Color res = blend(cs);
                r = res.getRed();
                g = res.getGreen();
                b = res.getBlue();
            }
        }

        if (beat.isOnset())
            eRadius = 500;

        if (enabled) {
            if (colorEnabled) {
                sendToArduino((int)r, (int)g, (int)b, eRadius);
            } else {
                sendToArduino(defaultColor.getRed(), defaultColor.getGreen(), defaultColor.getBlue(), eRadius);
            }
        } else {
            sendToArduino(defaultColor.getRed(), defaultColor.getGreen(), defaultColor.getBlue(), 500 * ((float)defaultColor.getAlpha()) / 255);
        }
    }

    private synchronized void sendToArduino(int r, int g, int b, float a) {
        if (exit) return;
        try {
            blockingQueue.add(new Runnable() {
                public void run() {
                    // normalize the values
                    int r1 = (int) (r * a / 500);
                    int g1 = (int) (g * a / 500);
                    int b1 = (int) (b * a / 500);
                    try {
                        output.write(r1);
                        output.write(g1);
                        output.write(b1);
                        output.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                    if (exit && r1 == 0 && g1 == 0 && b1 == 0) {
                        try {
                            output.write(r1);
                            output.write(g1);
                            output.write(b1);
                            output.flush();
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.exit(0);
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    public void setupUI() {
        // Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        final TrayIcon trayIcon = new TrayIcon(createImage("icon.png", "Colorful Sound"), "Colorful Sound");
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a pop-up menu components
        MenuItem aboutItem = new MenuItem("Set Default Color");
        MenuItem shuffle = new MenuItem("Shuffle");
        CheckboxMenuItem cb1 = new CheckboxMenuItem("Color Change Enabled");
        CheckboxMenuItem cb2 = new CheckboxMenuItem("Enabled");
        CheckboxMenuItem cb3 = new CheckboxMenuItem("High Sensitivity");
        CheckboxMenuItem cb4 = new CheckboxMenuItem("Low Sensitivity");
        MenuItem exitItem = new MenuItem("Exit");

        cb1.setState(colorEnabled);
        cb2.setState(enabled);
        cb3.setState(threshold1);
        cb4.setState(threshold2);
        cb1.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int cb1Id = e.getStateChange();
                if (cb1Id == ItemEvent.SELECTED) {
                    colorEnabled = true;
                } else {
                    colorEnabled = false;
                }
            }
        });
        cb2.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int cb1Id = e.getStateChange();
                if (cb1Id == ItemEvent.SELECTED) {
                    enabled = true;
                } else {
                    enabled = false;
                }
            }
        });
        cb3.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int cb1Id = e.getStateChange();
                if (cb1Id == ItemEvent.SELECTED) {
                    threshold1 = true;
                    threshold2 = false;
                    cb4.setState(threshold2);
                } else {
                    threshold1 = false;
                }
            }
        });
        cb4.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                int cb1Id = e.getStateChange();
                if (cb1Id == ItemEvent.SELECTED) {
                    threshold2 = true;
                    threshold1 = false;
                    cb3.setState(threshold1);
                } else {
                    threshold2 = false;
                }
            }
        });

        ActionListener listener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                MenuItem item = (MenuItem) e.getSource();
                if ("Set Default Color".equals(item.getLabel())) {
                    JFrame frame = new JFrame("ColorfulSound: Choose Default Color");
                    frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                    
                    JColorChooser tcc = new JColorChooser();
                    tcc.getSelectionModel().addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            defaultColor = tcc.getColor();
                        }
                    });
                    frame.setContentPane(tcc);
                    
                    frame.pack();
                    frame.setVisible(true);
                } else if ("Shuffle".equals(item.getLabel())) {
                    Collections.shuffle(rainbow);
                } else if ("Exit".equals(item.getLabel())) {
                    sendToArduino(0, 0, 0, 0);
                    exit = true;
                }
            }
        };
        aboutItem.addActionListener(listener);
        exitItem.addActionListener(listener);
        shuffle.addActionListener(listener);

        // Add components to pop-up menu
        popup.add(cb2);
        popup.add(cb1);
        popup.addSeparator();
        popup.add(aboutItem);
        popup.add(shuffle);
        popup.add(cb3);
        popup.add(cb4);
        popup.addSeparator();
        popup.add(exitItem);

        trayIcon.setImageAutoSize(true);
        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }
    
    public static Color blend(List<Color> c) {
        if (c == null || c.size() <= 0) {
            return null;
        }
        float ratio = 1f / ((float) c.size());

        int a = 0;
        int r = 0;
        int g = 0;
        int b = 0;

        for (int i = 0; i < c.size(); i++) {
            int rgb = c.get(i).getRGB();
            int a1 = (rgb >> 24 & 0xff);
            int r1 = ((rgb & 0xff0000) >> 16);
            int g1 = ((rgb & 0xff00) >> 8);
            int b1 = (rgb & 0xff);
            a += ((int) a1 * ratio);
            r += ((int) r1 * ratio);
            g += ((int) g1 * ratio);
            b += ((int) b1 * ratio);
        }

        return new Color(a << 24 | r << 16 | g << 8 | b);
    }

    static public void main(String[] passedArgs) {
        SoundEnergyBeatDetection beatDetection = new SoundEnergyBeatDetection();
        beatDetection.setup();
    }

    // Obtain the image URL
    protected static Image createImage(String path, String description) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL imageURL = loader.getResource(path);

        if (imageURL == null) {
            System.err.println("Resource not found: " + path);
            return null;
        } else {
            return (new ImageIcon(imageURL, description)).getImage();
        }
    }
}

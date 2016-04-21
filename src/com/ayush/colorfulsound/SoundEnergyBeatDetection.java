package com.ayush.colorfulsound;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

	volatile float eRadius;
	volatile int r = 0;
	volatile int b = 0;
	volatile int g = 0;

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

		ArduinoConnector main = new ArduinoConnector();
		main.initialize();
		output = main.getOutputStream();
		
		minim = new Minim(this);
		song = minim.getLineIn(Minim.STEREO, 2048);//
		//song = minim.loadFile("F:/Thriller-Michael_Jackson[www.MastJatt.Com].mp3", 2048);
		//song.play();

		// a beat detection object song SOUND_ENERGY mode with a sensitivity of
		// 10 milliseconds
		beat = new BeatDetect();
		beat.setSensitivity(300);

		freqBeat = new BeatDetect(song.bufferSize(), song.sampleRate());
		freqBeat.setSensitivity(500);

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
					
					sendToArduino(r, g, b, eRadius);
					
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

		int red = 0;
		int blue = 0;
		int green = 0;
		if (freqBeat.isKick())
			red = (int) (128 * Math.random()) + 128;
		if (freqBeat.isSnare())
			green = (int) (128 * Math.random()) + 128;
		if (freqBeat.isHat())
			blue = (int) (128 * Math.random()) + 128;

		if (!(red == 0 && blue == 0 && green == 0)) {
			r = red;
			g = green;
			b = blue;
		}

		if (beat.isOnset())
			eRadius = 500;

		sendToArduino(r, g, b, eRadius);
	}

	private synchronized void sendToArduino(int r, int g, int b, float a) {
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
				}
			});
		} catch (Exception e) {
		}
	}

	static public void main(String[] passedArgs) {
		SoundEnergyBeatDetection beatDetection = new SoundEnergyBeatDetection();
		beatDetection.setup();
	}
}

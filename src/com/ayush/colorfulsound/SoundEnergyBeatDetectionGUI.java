package com.ayush.colorfulsound;

import processing.core.*;

import java.io.IOException;
import java.io.OutputStream;

import ddf.minim.*;
import ddf.minim.analysis.*;

public class SoundEnergyBeatDetectionGUI extends PApplet {

	Minim minim;
	AudioInput song;
	BeatDetect beat;
	BeatDetect freqBeat;
	BeatListener bl;
	OutputStream output;

	float eRadius;

	float kickSize, snareSize, hatSize;

	int r = 0;
	int b = 0;
	int g = 0;

	class BeatListener implements AudioListener {
		private BeatDetect beat;
		private AudioInput source;

		BeatListener(BeatDetect beat, AudioInput source) {
			this.source = source;
			this.source.addListener(this);
			this.beat = beat;
		}

		public void samples(float[] samps) {
			beat.detect(source.mix);
		}

		public void samples(float[] sampsL, float[] sampsR) {
			beat.detect(source.mix);
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
		beat.setSensitivity(100);

		freqBeat = new BeatDetect(song.bufferSize(), song.sampleRate());
		freqBeat.setSensitivity(500);

		bl = new BeatListener(freqBeat, song);

		ellipseMode(RADIUS);
		eRadius = 20;
	}

	public void draw() {
		background(0);
		beat.detect(song.mix);
		float a = map(eRadius, 20, 80, 60, 255);

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

		fill(r, g, b, a);

		if (beat.isOnset())
			eRadius = 500;

		ellipse(width / 2, height / 2, eRadius, eRadius);
		sendToArduino(r, g, b, eRadius);
		
		eRadius *= 0.95f;
		if (eRadius < 50)
			eRadius = 50;
	}

	private void sendToArduino(int r, int g, int b, float a) {
		// normalize the values
		r = (int) (r * a / 500);
		g = (int) (g * a / 500);
		b = (int) (b * a / 500);
		try {
			output.write(r);
			output.write(g);
			output.write(b);
			output.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void settings() {
		size(1900, 1000, P3D);
	}

	static public void main(String[] passedArgs) {
		String[] appletArgs = new String[] { "com.ayush.colorfulsound.SoundEnergyBeatDetection" };
		if (passedArgs != null) {
			PApplet.main(concat(appletArgs, passedArgs));
		} else {
			PApplet.main(appletArgs);
		}
	}
}

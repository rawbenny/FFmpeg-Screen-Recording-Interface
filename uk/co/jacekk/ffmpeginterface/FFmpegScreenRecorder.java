package uk.co.jacekk.ffmpeginterface;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;

public class FFmpegScreenRecorder {
	
	public LinkedHashMap<String, String> params;
	
	private Process proc;
	private boolean recording;
	
	public FFmpegScreenRecorder(){
		this.params = new LinkedHashMap<String, String>();
		
		this.params.put("X", "0");
		this.params.put("Y", "0");
		this.params.put("Width", "640");
		this.params.put("Height", "480");
		this.params.put("Frame Rate", "30");
		
		this.recording = false;
	}
	
	public String getParam(String key){
		return this.params.get(key);
	}
	
	public void setParam(String key, String value){
		if (this.params.containsKey(key)){
			this.params.put(key, value);
		}
	}
	
	public void start() throws IOException, FFmpegException {
		ProcessBuilder builder = new ProcessBuilder(Arrays.asList(
			"ffmpeg",
			"-y",
			"-v 0",
			"-f alsa",
			"-ac 2",
			"-i pulse",
			"-f x11grab",
			"-r " + this.getParam("Frame Rate"),
			"-s " + this.getParam("Width") + "x" + this.getParam("Height"),
			"-i :0.0+" + this.getParam("X") + "," + this.getParam("Y"),
			"-acodec libmp3lame",
			"-vcodec libx264",
			"-vpre lossless_ultrafast",
			"-threads 0",
			System.getProperty("user.home") + "temp.mkv"
		));
		
		this.proc = builder.start();
		
		if (this.proc == null){
			throw new FFmpegException("Failed to execute ffmpeg");
		}
		
		this.recording = true;
	}
	
	public void stop() throws FFmpegException {
		try{
			Field field = this.proc.getClass().getDeclaredField("pid");
			field.setAccessible(true);
			
			Runtime.getRuntime().exec("kill -2 " + (Integer) field.get(this.proc));
		}catch (Exception e){
			this.proc.destroy();
			throw new FFmpegException("Can't cleanly kill ffmpeg, using a bad way instead");
		}
		
		this.recording = false;
	}
	
	public boolean isRecording(){
		return this.recording;
	}
	
}
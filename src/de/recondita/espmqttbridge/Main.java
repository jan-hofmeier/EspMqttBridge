package de.recondita.espmqttbridge;

import java.io.IOException;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttException;

import de.recondita.espmqttbridge.EspReceiver.ReceiverCallBack;

public class Main {
	private final static Logger LOGGER = Logger.getLogger(Main.class.getName());
	
	private static EspReceiver espReceiver;
	private static MqttPublisher mqttPublisher;

	public static void main(String[] args) throws MqttException, IOException {
		String configDir = args.length == 0 ? "/etc/espreceiver" : args[0];
		
		Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));
		
		mqttPublisher = new MqttPublisher(configDir);
		espReceiver = new EspReceiver(new ReceiverCallBack() {

			@Override
			public void updateTemp(String room, float temp) {
				try {
					mqttPublisher.publishTemprature(room, temp);
				} catch (MqttException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void updateHumidity(String room, float humidity) {
				try {
					mqttPublisher.publishHumidity(room, humidity);
				} catch (MqttException e) {
					e.printStackTrace();
				}
			}
		});
		
		LOGGER.info("started");
		espReceiver.acceptLoop();
	}

	public static void shutdown() {
		if (espReceiver != null)
			try {
				espReceiver.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		if (mqttPublisher != null)
			mqttPublisher.close();
	}

}

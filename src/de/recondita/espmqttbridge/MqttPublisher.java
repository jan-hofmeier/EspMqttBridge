package de.recondita.espmqttbridge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttPublisher implements AutoCloseable {

	private final static Logger LOGGER = Logger.getLogger(MqttClient.class.getName());
	private MqttClient mqttClient;
	private String appartment;

	
	public MqttPublisher(String configDir) throws MqttException, IOException {
		this(loadMQTTConfig(configDir));
	}
	
	public MqttPublisher(String[] config) throws MqttException {
		appartment = config[3];
		mqttClient = new MqttClient(config[0], config[1], new MemoryPersistence());
		mqttClient.setCallback(new MqttCallbackExtended() {

			@Override
			public void messageArrived(String topic, MqttMessage message) throws Exception {
				LOGGER.log(Level.INFO, "MQTT: " + topic + " " + message.toString());
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {

			}

			@Override
			public void connectionLost(Throwable cause) {
				LOGGER.log(Level.SEVERE, "MQTT: connection lost");
			}

			@Override
			public void connectComplete(boolean reconnect, String serverURI) {
				LOGGER.log(Level.SEVERE, "MQTT: connection complete");
			}
		});
		MqttConnectOptions conOpt = new MqttConnectOptions();
		conOpt.setAutomaticReconnect(true);
		conOpt.setCleanSession(true);
		if (config[2] != null) {
			conOpt.setUserName(config[1]);
			conOpt.setPassword(config[2].toCharArray());
		}
		mqttClient.connect(conOpt);
	}
	
	
	private void publishClimate(String room, String type, float value) throws MqttPersistenceException, MqttException {
		String topic = "climate/" + appartment + "/" + room.toLowerCase() + "/" + type;
		mqttClient.publish(topic, String.valueOf(value).getBytes(), 0, true);
	}
	
	public void publishTemprature(String sensor, float temprature) throws MqttPersistenceException, MqttException {
		publishClimate(sensor, "temp", temprature);
	}
	
	public void publishHumidity(String sensor, float humidity) throws MqttPersistenceException, MqttException {
		publishClimate(sensor, "hum", humidity);
	}

	
	public static String[] loadMQTTConfig(String configDir) throws IOException {
		String[] ret = null; // default config
		File mqttfile = new File(configDir + File.separator + "mqtt-config.txt");
		LOGGER.info("Try to use MQTT config: " + mqttfile);
		if (mqttfile.exists()) {
			LOGGER.info("Found MQTT config: " + mqttfile);
			try (BufferedReader br = new BufferedReader(new FileReader(mqttfile))) {
				ret = new String[4];
				for (int i = 0; i < ret.length; i++) {
					ret[i] = br.readLine();
				}
			}
		}else {
			LOGGER.severe(mqttfile + " not found");
		}
		return ret;
	}
	
	@Override
	public void close() {
		try {
			mqttClient.close(true);
		} catch (MqttException e) {
			e.printStackTrace();
		}
	}

}

package de.recondita.espmqttbridge;

import java.io.Closeable;
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

public class MqttPublisher implements Closeable {

	private final static Logger LOGGER = Logger.getLogger(MqttClient.class.getName());
	private MqttClient mqttClient;

	public MqttPublisher(String[] config) throws MqttException {
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
	
	public void publishTemprature(String sensor, float temprature) throws MqttPersistenceException, MqttException {
		mqttClient.publish(sensor, String.valueOf(temprature).getBytes(), 0, true);
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

}

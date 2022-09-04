package de.recondita.espmqttbridge;

import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.eclipse.paho.client.mqttv3.MqttException;

import de.recondita.espmqttbridge.EspReceiver.ReceiverCallBack;

public class Service implements Daemon {

	private EspReceiver espReceiver;
	private MqttPublisher mqttPublisher;

	
	@Override
	public void init(DaemonContext context) throws DaemonInitException, Exception {
		String[] args = context.getArguments();
		String configDir = args.length == 0 ? "/etc/espreceiver" : args[0];
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
	}

	@Override
	public void start() throws Exception {
		espReceiver.startListener();
	}

	@Override
	public void stop() throws Exception {
		espReceiver.close();
		mqttPublisher.close();
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}

package de.recondita.espmqttbridge;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EspReceiver implements AutoCloseable{

	
	private final static Logger LOGGER = Logger.getLogger(EspReceiver.class.getName());

	private ServerSocket serverSocket;

	private ExecutorService executorService = Executors.newWorkStealingPool(2);

	private ReceiverCallBack callback;
	
	private Timer timeoutTimer;

	public EspReceiver(ReceiverCallBack callback) throws IOException {
		this.callback = callback;
		timeoutTimer = new Timer();
		serverSocket = new ServerSocket(50000);
	}

	public void acceptLoop() {
		while (!serverSocket.isClosed()) {
			try {
				Socket socket = serverSocket.accept();
				executorService.execute(new ServiceRequest(socket));
			} catch (IOException e) {
				LOGGER.log(Level.INFO, e.getMessage(), e);
			}
		}
	}

	public void startListener() {
		new Thread(() -> acceptLoop()).start();
	}

	private class ServiceRequest implements Runnable {

		private Socket socket;

		public ServiceRequest(Socket connection) {
			this.socket = connection;
			LOGGER.info("Client connected with IP: " + socket.getInetAddress().getHostName());
		}

		@Override
		public void run() {
			LOGGER.info("Read from Client");
			TimerTask timeout = new TimerTask() {
				
				@Override
				public void run() {
						LOGGER.info("closed connection by timeout");
						try {
							socket.close();
						} catch (IOException e) {
							LOGGER.log(Level.INFO, e.getMessage(), e);
						}
					
				}
			};
			timeoutTimer.schedule(timeout, 5000);
			try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
				String line;
				while ((line = br.readLine()) != null) {
					LOGGER.info("Received: " + line);
					String[] parts = line.split(":");
					if (parts.length < 2)
						continue;
					try {
						String room = parts[0].trim();
						parts = parts[1].split("/");
						callback.updateTemp(room, Float.parseFloat(parts[0].trim()));
						if(parts.length > 1)
							callback.updateHumidity(room, Float.parseFloat(parts[1].trim()));
					} catch (NumberFormatException ne) {
						LOGGER.log(Level.WARNING, "Exception while reciving Temprature:\n" + ne.getMessage(), ne);
					}
				}
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Exception while reciving Temprature:\n" + e.getMessage(), e);
			}finally {
				timeout.cancel();
			}
			LOGGER.info("Closed connection");
		}
	}
	
	public interface ReceiverCallBack{
		public void updateTemp(String room, float temp);
		public void updateHumidity(String room, float hum);
	}

	@Override
	public void close() throws Exception {
		timeoutTimer.cancel();
		serverSocket.close();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

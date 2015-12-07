package com.dbay.apns4j.demo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import com.dbay.apns4j.IApnsService;
import com.dbay.apns4j.impl.ApnsServiceImpl;
import com.dbay.apns4j.model.ApnsConfig;
import com.dbay.apns4j.model.Feedback;
import com.dbay.apns4j.model.Payload;

/**
 * @author RamosLi
 * 
 */
public class Apns4jDemo {
	private static IApnsService	apnsService;
	
	private static IApnsService getApnsService() throws FileNotFoundException {
		if (apnsService == null) {
			ApnsConfig config = new ApnsConfig();
			
			// 这里可以直接放byte流初始化
			String keystore = "C:\\Users\\zhaohaolin.HIK\\git\\dbay-apns-for-java\\src\\test\\java\\com\\dbay\\apns4j\\demo\\ezviz_distribution.p12";
			// InputStream is = Apns4jDemo.class.getClassLoader()
			// .getResourceAsStream("videointercom_development.p12");
			
			InputStream is = new FileInputStream(keystore);
			
			config.setKeyStore(is);
			config.setDevEnv(false);
			config.setPassword("hikvision");
			config.setPoolSize(3);
			// 假如需要在同个java进程里给不同APP发送通知，那就需要设置为不同的name
			// config.setName("welove1");
			apnsService = ApnsServiceImpl.createInstance(config);
		}
		return apnsService;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		IApnsService service = getApnsService();
		
		// send notification
		String token = "03f863657aadd9c5ced74c74717f208fc64fc5dbcefcc72d37c83cda88cf9ca6";
		
		Payload payload = new Payload();
		payload.setAlert("How are you?");
		// If this property is absent, the badge is not changed. To remove the
		// badge, set the value of this property to 0
		payload.setBadge(8);
		// set sound null, the music won't be played
		// payload.setSound(null);
		//payload.setSound("msg.mp3");
		//payload.addParam("uid", 123456);
		//payload.addParam("type", 12);
		
		// send notification
		service.sendNotification(token, payload);
		
		// payload, use loc string
		Payload payload2 = new Payload();
		payload2.setBadge(3);
		payload2.setAlertLocKey("GAME_PLAY_REQUEST_FORMAT");
		payload2.setAlertLocArgs(new String[] { "Jenna", "Frank" });
		
		// send notification
		//service.sendNotification(token, payload2);
		
		// get feedback
		List<Feedback> list = service.getFeedbacks();
		if (list != null && list.size() > 0) {
			for (Feedback feedback : list) {
				System.out.println(feedback.getDate() + " "
						+ feedback.getToken());
			}
		}
		
		// try {
		// Thread.sleep(5000);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		
		// It's a good habit to shutdown what you never use
		 //service.shutdown();
		
		 //System.exit(0);
	}
}

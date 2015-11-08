package com.dbay.apns4j.demo;

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
	
	private static IApnsService getApnsService() {
		if (apnsService == null) {
			ApnsConfig config = new ApnsConfig();
			
			// 这里可以直接放byte流初始化
			InputStream is = Apns4jDemo.class.getClassLoader()
					.getResourceAsStream("push_production.p12");
			
			config.setKeyStore(is);
			config.setDevEnv(false);
			config.setPassword("XXXXXX");
			config.setPoolSize(3);
			// 假如需要在同个java进程里给不同APP发送通知，那就需要设置为不同的name
			// config.setName("welove1");
			apnsService = ApnsServiceImpl.createInstance(config);
		}
		return apnsService;
	}
	
	public static void main(String[] args) {
		IApnsService service = getApnsService();
		
		// send notification
		String token = "4f2c96241ed061c5e7c58cfd20e77b361fd4f7fb9422b6d1d2efc2807aba7733";
		
		Payload payload = new Payload();
		payload.setAlert("How are you?");
		// If this property is absent, the badge is not changed. To remove the
		// badge, set the value of this property to 0
		payload.setBadge(1);
		// set sound null, the music won't be played
		// payload.setSound(null);
		payload.setSound("msg.mp3");
		payload.addParam("uid", 123456);
		payload.addParam("type", 12);
		
		// send notification
		service.sendNotification(token, payload);
		
		// payload, use loc string
		Payload payload2 = new Payload();
		payload2.setBadge(1);
		payload2.setAlertLocKey("GAME_PLAY_REQUEST_FORMAT");
		payload2.setAlertLocArgs(new String[] { "Jenna", "Frank" });
		
		// send notification
		service.sendNotification(token, payload2);
		
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
		// service.shutdown();
		
		// System.exit(0);
	}
}

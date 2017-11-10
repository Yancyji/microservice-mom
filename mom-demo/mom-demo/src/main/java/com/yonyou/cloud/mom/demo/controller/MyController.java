package com.yonyou.cloud.mom.demo.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yonyou.cloud.mom.client.MqSender;
import com.yonyou.cloud.mom.demo.dao.UserTMapper;
import com.yonyou.cloud.mom.demo.msg.entity.LoginMsg;
import com.yonyou.cloud.mom.demo.service.BizService;

@Controller
public class MyController {
	
	private static Logger logger = Logger.getLogger(MyController.class);
	
	@Autowired
	private BizService bizService;
	
	@Autowired
	private UserTMapper mapper;
	
	@Autowired
	private MqSender mqSender;
	
	private ExecutorService executor = Executors.newFixedThreadPool(10);
	
	public class testRunnable implements Runnable{
		private LoginMsg msg;
		private MqSender mqSender;
		
		public testRunnable(LoginMsg msg, MqSender mqSender){
			this.msg = msg;
			this.mqSender = mqSender;
		}
		

		@Override
		public void run() {
			mqSender.send("event-exchange", "login", msg);
		}
		
	}
	
	@RequestMapping(value="/Hello/{message}")  
    public String HelloWorld(Model model,@PathVariable("message") String message){  
        model.addAttribute("message",message);  
        return "hello";  
    }  
	
	@RequestMapping(value="/test/{message}")  
	@ResponseBody
    public void test(@PathVariable("message") String message){ 
		int selectCnt = mapper.selectCnt();
			logger.info(selectCnt);
		logger.error(message);
		logger.debug(message);
		logger.info(message);
		logger.warn(message);
    }  

	@RequestMapping("/test")
	@ResponseBody
	public String test() {
		int size = 10;
		List<testRunnable> l = new ArrayList<testRunnable>();
		
		for(int i = 0; i<size; i++){
			LoginMsg msg = new LoginMsg();
			msg.setLoginName(UUID.randomUUID().toString());
			msg.setLoginTime(new Date().getTime());
			l.add(new testRunnable(msg, mqSender));
		}
		for(testRunnable r:l){
			executor.execute(r);
		}
		return "done";
	}
	
	@RequestMapping("/login/{name}")
	@ResponseBody
	public String login(@PathVariable("name") String name) throws Exception{
		return bizService.saveLoginUser(name);
	}

}

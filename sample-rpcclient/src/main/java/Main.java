import iface.Hello;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * author: chao.hua
 * createTime: Dec 30, 2014 1:44:29 PM
 */

public class Main {

	public static void main(String[] args){
		ApplicationContext ctx = new ClassPathXmlApplicationContext("config/spring/appcontext-*.xml");
		final Hello basic = (Hello) ctx.getBean("hello");
		System.out.println(basic.hello());
	}
}

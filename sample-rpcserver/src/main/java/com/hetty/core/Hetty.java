package com.hetty.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.hetty.conf.HettyConfig;
import com.hetty.object.Application;
import com.hetty.object.HettyException;
import com.hetty.register.IPlugin;
import com.hetty.register.XmlConfigPluginRegistor;

import file.FileUtil;


public final class Hetty {

	private static  Logger logger = LoggerFactory.getLogger(Hetty.class);
	
	private ServerBootstrap httpBootstrap = null;
	private ServerBootstrap httpsBootstrap = null;
	private HettyConfig hettyConfig = HettyConfig.getInstance();
	private int httpListenPort;
	private int httpsListenPort;
	private ApplicationContext ctx;
	private ChannelFuture cf;
	private EventLoopGroup boss;
	private EventLoopGroup worker; 
	
	public Hetty(){
		HettyConfig.getInstance().loadPropertyFile("config/server.properties");//default file is this.
	}
	public Hetty(String file){
		HettyConfig.getInstance().loadPropertyFile(file);
	}
	
	private void init() {
		ctx = new ClassPathXmlApplicationContext(new String[]{"classpath*:config/spring/appcontext-*.xml"});
		initServerInfo();
		initHettySecurity();
		initPlugins();
		initServiceMetaData();
		if (httpListenPort == -1 && httpsListenPort == -1) {
			httpListenPort = 8081;//default port is 8081
		}
		if (httpListenPort != -1) {
			initHttpBootstrap();
		}
		if (httpsListenPort != -1) {
			initHttpsBootstrap();
		}
	}
	/**
	 * init hetty server info
	 */
	private void initServerInfo(){
		httpListenPort = hettyConfig.getHttpPort();
		httpsListenPort = hettyConfig.getHttpsPort();
	}
	/**
	 * init http bootstrap
	 */
    private void initHttpBootstrap(){
    	logger.info("init HTTP Bootstrap...........");
    	int coreSize = hettyConfig.getServerCorePoolSize();
		int maxSize = hettyConfig.getServerMaximumPoolSize();
		int keepAlive = hettyConfig.getServerKeepAliveTime();
		ThreadFactory threadFactory = new NamedThreadFactory("hetty-");
    	ExecutorService threadPool = new ThreadPoolExecutor(coreSize, maxSize, keepAlive,
				TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory);
    	
    	
    	 boss = new NioEventLoopGroup();
    	 worker = new NioEventLoopGroup();
    	ServerBootstrap httpBootStrap = new ServerBootstrap();
    		cf = httpBootStrap.group(boss, worker)
	    				 .channel(NioServerSocketChannel.class)
	    				 .childHandler(new HettyServerHandlerInitializer(threadPool))
	    				 .option(ChannelOption.SO_KEEPALIVE, true)
	    				 .bind(new InetSocketAddress(httpListenPort));
	    	
	    	
    	/*
		ThreadFactory serverBossTF = new NamedThreadFactory("HETTY-BOSS-");
		ThreadFactory serverWorkerTF = new NamedThreadFactory("HETTY-WORKER-");
		httpBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
				Executors.newCachedThreadPool(serverBossTF),
				Executors.newCachedThreadPool(serverWorkerTF)));
		httpBootstrap.setOption("tcpNoDelay", Boolean.parseBoolean(hettyConfig
				.getProperty("hetty.tcp.nodelay", "true")));
		httpBootstrap.setOption("reuseAddress", Boolean.parseBoolean(hettyConfig
				.getProperty("hetty.tcp.reuseaddress", "true")));
		
		int coreSize = hettyConfig.getServerCorePoolSize();
		int maxSize = hettyConfig.getServerMaximumPoolSize();
		int keepAlive = hettyConfig.getServerKeepAliveTime();
		ThreadFactory threadFactory = new NamedThreadFactory("hetty-");
		ExecutorService threadPool = new ThreadPoolExecutor(coreSize, maxSize, keepAlive,
				TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory);
		httpBootstrap.setPipelineFactory(new HettyChannelPipelineFactory(threadPool));
		
		if (!checkPortConfig(httpListenPort)) {
			throw new IllegalStateException("port: " + httpListenPort + " already in use!");
		}
		httpBootstrap.bind(new InetSocketAddress(httpListenPort));
		*/
    }
    /**
	 * init https bootstrap
	 */
    private void initHttpsBootstrap(){
//    	if(!checkHttpsConfig()){
//    		return;
//    	}
//    	logger.info("init HTTPS Bootstrap...........");
//		ThreadFactory serverBossTF = new NamedThreadFactory("HETTY-BOSS-");
//		ThreadFactory serverWorkerTF = new NamedThreadFactory("HETTY-WORKER-");
//		httpsBootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(
//				Executors.newCachedThreadPool(serverBossTF),
//				Executors.newCachedThreadPool(serverWorkerTF)));
//		httpsBootstrap.setOption("tcpNoDelay", Boolean.parseBoolean(hettyConfig
//				.getProperty("hetty.tcp.nodelay", "true")));
//		httpsBootstrap.setOption("reuseAddress", Boolean.parseBoolean(hettyConfig
//				.getProperty("hetty.tcp.reuseaddress", "true")));
//		
//		int coreSize = hettyConfig.getServerCorePoolSize();
//		int maxSize = hettyConfig.getServerMaximumPoolSize();
//		int keepAlive = hettyConfig.getServerKeepAliveTime();
//		ThreadFactory threadFactory = new NamedThreadFactory("hetty-");
//		ExecutorService threadPool = new ThreadPoolExecutor(coreSize, maxSize, keepAlive,
//				TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), threadFactory);
//		httpsBootstrap.setPipelineFactory(new SslHettyChannelPipelineFactory(threadPool));
//		
//		if (!checkPortConfig(httpsListenPort)) {
//			throw new IllegalStateException("port: " + httpsListenPort + " already in use!");
//		}
//		httpsBootstrap.bind(new InetSocketAddress(httpsListenPort));
    }

	/**
	 * init plugins
	 */
	private void initPlugins() {
		logger.info("init plugins...........");
		try{
			IPlugin localPlugin = (XmlConfigPluginRegistor) ctx.getBean("localPlugin");
			localPlugin.start();
		}catch(NoSuchBeanDefinitionException ex){
			logger.debug("no localPlugin bean defined");
		}
		
		try{
			IPlugin remotePlugin = (XmlConfigPluginRegistor) ctx.getBean("remotePlugin");
			remotePlugin.start();
		}catch(NoSuchBeanDefinitionException ex){
			logger.debug("no remotePlugin bean defined");
		}
		/*
		List<Class<?>> pluginList = hettyConfig.getPluginClassList();
		try {
			for (Class<?> cls : pluginList) {
				IPlugin p;
				p = (IPlugin) cls.newInstance();
				p.start();
			}
		} catch (InstantiationException e) {
			logger.error("init plugin failed.");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			logger.error("init plugin failed.");
			e.printStackTrace();
		}
		*/
	}
	
	/**
	 * init service metaData
	 */
    private void initServiceMetaData() {
		// TODO Auto-generated method stub
    	logger.info("init service MetaData...........");
    	MetadataProcessor.initMetaDataMap();
	}
    /**
	 * init service metaData
	 */
    private void initHettySecurity() {
		// TODO Auto-generated method stub
    	logger.info("init hetty security...........");
    	Application app = new Application(hettyConfig.getServerKey(),hettyConfig.getServerSecret());
    	HettySecurity.addToApplicationMap(app);
	}
    /**
     * check the netty listen port
     * @param listenPort
     * @return
     */
	private boolean checkPortConfig(int listenPort) {
		if (listenPort < 0 || listenPort > 65536) {
			throw new IllegalArgumentException("Invalid start port: "
					+ listenPort);
		}
		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(listenPort);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(listenPort);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
		} finally {
			if (ds != null) {
				ds.close();
			}
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					// should not be thrown, just detect port available.
				}
			}
		}
		return false;
	}
	/**
	 * check https config
	 * @return
	 */
	private boolean  checkHttpsConfig(){
		if(StringUtils.isNotEmpty(hettyConfig.getKeyStorePath())){
			if(!FileUtil.getFile(hettyConfig.getKeyStorePath()).exists()){
				throw new HettyException("we can't find the file which you configure:[ssl.keystore.file]");
			}
		}else if(StringUtils.isNotEmpty(hettyConfig.getCertificateKeyFile()) && 
				StringUtils.isNotEmpty(hettyConfig.getCertificateFile())){
			if(!FileUtil.getFile(hettyConfig.getCertificateKeyFile()).exists()){
				throw new HettyException("we can't find the file which you configure:[ssl.certificate.key.file]");
			}
			if(!FileUtil.getFile(hettyConfig.getCertificateFile()).exists()){
				throw new HettyException("we can't find the file which you configure:[ssl.certificate.file]");
			}
		}else{
			throw new HettyException("please check your ssl's config.");
		}
		return true;
	}
	public void serverLog(){
		logger.info("devMod:"+hettyConfig.getDevMod());
		logger.info("server key:"+hettyConfig.getServerKey());
		logger.info("server secret:"+hettyConfig.getServerSecret());
		if(httpListenPort != -1){
			logger.info("Server started,listening for HTTP on port "+httpListenPort);
		}
		if(httpsListenPort != -1){
			logger.info("Server started,listening for HTTPS on port "+httpsListenPort);
		}
	}
	/**
	 * start hetty
	 */
	public void start(){
		init();
		serverLog();
		try{
			cf.channel().closeFuture().sync();
		}catch(InterruptedException ex){
			logger.debug(ex.toString());
		}finally{
			boss.shutdownGracefully();
			worker.shutdownGracefully();
		}
		
	}
	/**
	 * stop hetty
	 */
	public void stop(){
		logger.info("Server stop!");
		if(httpBootstrap != null){
//			httpBootstrap.releaseExternalResources();
		}
		if(httpsBootstrap != null){
//			httpsBootstrap.releaseExternalResources();
		}
	}
	public static void main(String[] args) {
		new Hetty().start();
	}
}

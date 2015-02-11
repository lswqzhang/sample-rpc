package com.hetty.core;

import java.util.concurrent.ExecutorService;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

/**
 * author: chao.hua
 * createTime: Feb 10, 2015 4:25:51 PM
 */

public class HettyServerHandlerInitializer extends ChannelInitializer<Channel> {
	
	private ExecutorService threadpool;

	public HettyServerHandlerInitializer(ExecutorService threadpool) {
		this.threadpool = threadpool;
	}

	@Override
	protected void initChannel(Channel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast("deflater", new HttpContentCompressor());
		pipeline.addLast("httpResponseEncoder", new HttpResponseEncoder());
		pipeline.addLast("httpRequestDecoder", new HttpRequestDecoder());
		pipeline.addLast("httpChunkAggregator", new HttpObjectAggregator(1048576));
		pipeline.addLast("handler", new HettyHandler(threadpool));
		
	}

}

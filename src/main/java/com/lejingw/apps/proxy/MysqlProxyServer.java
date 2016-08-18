package com.lejingw.apps.proxy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetServer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.util.Properties;

public class MysqlProxyServer {
	private static final Logger logger = LoggerFactory.getLogger(MysqlProxyServer.class);

	public static void main(String[] args) throws IOException {
		Properties props = new Properties();
		props.load(MysqlProxyServer.class.getResourceAsStream("/conf.properties"));
		String host = props.getProperty("host");
		String portStr = props.getProperty("port");
		String proxyPortStr = props.getProperty("proxy_port");

		int port = Integer.parseInt(portStr);
		int proxyPort = Integer.parseInt(proxyPortStr);

		Vertx.vertx().deployVerticle(new MysqlProxyServerVerticle(host, port, proxyPort));
	}

	public static class MysqlProxyServerVerticle extends AbstractVerticle {
		private final String mysqlHost;
		private final int port;
		private final int proxyPort;

		public MysqlProxyServerVerticle(String host, int port, int proxyPort) {
			this.mysqlHost = host;
			this.port = port;
			this.proxyPort = proxyPort;
		}

		@Override
		public void start() throws Exception {
			NetServer netServer = vertx.createNetServer();//创建代理服务器
			NetClient netClient = vertx.createNetClient();//创建连接mysql客户端
			netServer.connectHandler(socket -> netClient.connect(port, mysqlHost, result -> {
				//响应来自客户端的连接请求，成功之后，在建立一个与目标mysql服务器的连接
				if (result.succeeded()) {
					//与目标mysql服务器成功连接连接之后，创造一个MysqlProxyConnection对象,并执行代理方法
					new MysqlProxyConnection(socket, result.result()).proxy();
				} else {
					logger.error(result.cause().getMessage(), result.cause());
					socket.close();
				}
			})).listen(proxyPort, listenResult -> {//代理服务器的监听端口
				if (listenResult.succeeded()) {
					//成功启动代理服务器
					logger.info("proxy server startup and listen on port:" + proxyPort);
				} else {
					//启动代理服务器失败
					logger.error("proxy exit. because: " + listenResult.cause().getMessage(), listenResult.cause());
					System.exit(1);
				}
			});
		}
	}

	public static class MysqlProxyConnection {
		private final NetSocket clientSocket;
		private final NetSocket serverSocket;

		public MysqlProxyConnection(NetSocket clientSocket, NetSocket serverSocket) {
			this.clientSocket = clientSocket;
			this.serverSocket = serverSocket;
		}

		private void proxy() {
			//当代理与mysql服务器连接关闭时，关闭client与代理的连接
			serverSocket.closeHandler(v -> clientSocket.close());
			//反之亦然
			clientSocket.closeHandler(v -> serverSocket.close());
			//不管那端的连接出现异常时，关闭两端的连接
			serverSocket.exceptionHandler(e -> {
				logger.error(e.getMessage(), e);
				close();
			});
			clientSocket.exceptionHandler(e -> {
				logger.error(e.getMessage(), e);
				close();
			});
			//当收到来自客户端的数据包时，转发给mysql目标服务器
			clientSocket.handler(buffer -> serverSocket.write(buffer));
			//当收到来自mysql目标服务器的数据包时，转发给客户端
			serverSocket.handler(buffer -> clientSocket.write(buffer));
		}

		private void close() {
			clientSocket.close();
			serverSocket.close();
		}
	}
}
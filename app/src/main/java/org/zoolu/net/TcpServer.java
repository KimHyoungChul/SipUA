package org.zoolu.net;

import java.io.IOException;
import java.net.ServerSocket;

public class TcpServer extends Thread {
	public static final int DEFAULT_SOCKET_TIMEOUT = 5000;
	static int socket_backlog;
	long alive_time;
	boolean is_running;
	TcpServerListener listener;
	ServerSocket server_socket;
	int socket_timeout;
	boolean stop;

	static {
		TcpServer.socket_backlog = 50;
	}

	public TcpServer(final int n, final IpAddress ipAddress, final long n2, final TcpServerListener tcpServerListener) throws IOException {
		this.init(n, ipAddress, n2, tcpServerListener);
		this.start();
	}

	public TcpServer(final int n, final IpAddress ipAddress, final TcpServerListener tcpServerListener) throws IOException {
		this.init(n, ipAddress, 0L, tcpServerListener);
		this.start();
	}

	public TcpServer(final int n, final TcpServerListener tcpServerListener) throws IOException {
		this.init(n, null, 0L, tcpServerListener);
		this.start();
	}

	private void init(final int n, final IpAddress ipAddress, final long alive_time, final TcpServerListener listener) throws IOException {
		this.listener = listener;
		if (ipAddress == null) {
			this.server_socket = new ServerSocket(n);
		} else {
			this.server_socket = new ServerSocket(n, TcpServer.socket_backlog, ipAddress.getInetAddress());
		}
		this.socket_timeout = 5000;
		this.alive_time = alive_time;
		this.stop = false;
		this.is_running = true;
	}

	public int getPort() {
		return this.server_socket.getLocalPort();
	}

	public void halt() {
		this.stop = true;
		try {
			this.server_socket.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public boolean isRunning() {
		return this.is_running;
	}

	@Override
	public void run() {
		//
		// This method could not be decompiled.
		//
		// Original Bytecode:
		//
		//     1: astore          4
		//     3: lconst_0
		//     4: lstore_1
		//     5: aload_0
		//     6: getfield        org/zoolu/net/TcpServer.alive_time:J
		//     9: lconst_0
		//    10: lcmp
		//    11: ifle            23
		//    14: invokestatic    java/lang/System.currentTimeMillis:()J
		//    17: aload_0
		//    18: getfield        org/zoolu/net/TcpServer.alive_time:J
		//    21: ladd
		//    22: lstore_1
		//    23: aload_0
		//    24: getfield        org/zoolu/net/TcpServer.stop:Z
		//    27: istore_3
		//    28: iload_3
		//    29: ifeq            74
		//    32: aload_0
		//    33: iconst_0
		//    34: putfield        org/zoolu/net/TcpServer.is_running:Z
		//    37: aload_0
		//    38: getfield        org/zoolu/net/TcpServer.server_socket:Ljava/net/ServerSocket;
		//    41: invokevirtual   java/net/ServerSocket.close:()V
		//    44: aload_0
		//    45: aconst_null
		//    46: putfield        org/zoolu/net/TcpServer.server_socket:Ljava/net/ServerSocket;
		//    49: aload_0
		//    50: getfield        org/zoolu/net/TcpServer.listener:Lorg/zoolu/net/TcpServerListener;
		//    53: ifnull          68
		//    56: aload_0
		//    57: getfield        org/zoolu/net/TcpServer.listener:Lorg/zoolu/net/TcpServerListener;
		//    60: aload_0
		//    61: aload           4
		//    63: invokeinterface org/zoolu/net/TcpServerListener.onServerTerminated:(Lorg/zoolu/net/TcpServer;Ljava/lang/Exception;)V
		//    68: aload_0
		//    69: aconst_null
		//    70: putfield        org/zoolu/net/TcpServer.listener:Lorg/zoolu/net/TcpServerListener;
		//    73: return
		//    74: new             Lorg/zoolu/net/TcpSocket;
		//    77: dup
		//    78: aload_0
		//    79: getfield        org/zoolu/net/TcpServer.server_socket:Ljava/net/ServerSocket;
		//    82: invokevirtual   java/net/ServerSocket.accept:()Ljava/net/Socket;
		//    85: invokespecial   org/zoolu/net/TcpSocket.<init>:(Ljava/net/Socket;)V
		//    88: astore          5
		//    90: aload_0
		//    91: getfield        org/zoolu/net/TcpServer.listener:Lorg/zoolu/net/TcpServerListener;
		//    94: ifnull          109
		//    97: aload_0
		//    98: getfield        org/zoolu/net/TcpServer.listener:Lorg/zoolu/net/TcpServerListener;
		//   101: aload_0
		//   102: aload           5
		//   104: invokeinterface org/zoolu/net/TcpServerListener.onIncomingConnection:(Lorg/zoolu/net/TcpServer;Lorg/zoolu/net/TcpSocket;)V
		//   109: aload_0
		//   110: getfield        org/zoolu/net/TcpServer.alive_time:J
		//   113: lconst_0
		//   114: lcmp
		//   115: ifle            23
		//   118: invokestatic    java/lang/System.currentTimeMillis:()J
		//   121: aload_0
		//   122: getfield        org/zoolu/net/TcpServer.alive_time:J
		//   125: ladd
		//   126: lstore_1
		//   127: goto            23
		//   130: astore          5
		//   132: aload_0
		//   133: getfield        org/zoolu/net/TcpServer.alive_time:J
		//   136: lconst_0
		//   137: lcmp
		//   138: ifle            23
		//   141: invokestatic    java/lang/System.currentTimeMillis:()J
		//   144: lload_1
		//   145: lcmp
		//   146: ifle            23
		//   149: aload_0
		//   150: invokevirtual   org/zoolu/net/TcpServer.halt:()V
		//   153: goto            23
		//   156: astore          4
		//   158: aload_0
		//   159: iconst_1
		//   160: putfield        org/zoolu/net/TcpServer.stop:Z
		//   163: goto            32
		//   166: astore          5
		//   168: goto            44
		//    Exceptions:
		//  Try           Handler
		//  Start  End    Start  End    Type
		//  -----  -----  -----  -----  --------------------------------
		//  5      23     156    166    Ljava/lang/Exception;
		//  23     28     156    166    Ljava/lang/Exception;
		//  37     44     166    171    Ljava/io/IOException;
		//  74     90     130    156    Ljava/io/InterruptedIOException;
		//  74     90     156    166    Ljava/lang/Exception;
		//  90     109    156    166    Ljava/lang/Exception;
		//  109    127    156    166    Ljava/lang/Exception;
		//  132    153    156    166    Ljava/lang/Exception;
		//
		// The error that occurred was:
		//
		// java.lang.IllegalStateException: Expression is linked from several locations: Label_0044:
		//     at com.strobel.decompiler.ast.Error.expressionLinkedFromMultipleLocations(Error.java:27)
		//     at com.strobel.decompiler.ast.AstOptimizer.mergeDisparateObjectInitializations(AstOptimizer.java:2596)
		//     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:235)
		//     at com.strobel.decompiler.ast.AstOptimizer.optimize(AstOptimizer.java:42)
		//     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:214)
		//     at com.strobel.decompiler.languages.java.ast.AstMethodBodyBuilder.createMethodBody(AstMethodBodyBuilder.java:99)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethodBody(AstBuilder.java:757)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createMethod(AstBuilder.java:655)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.addTypeMembers(AstBuilder.java:532)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeCore(AstBuilder.java:499)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createTypeNoCache(AstBuilder.java:141)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.createType(AstBuilder.java:130)
		//     at com.strobel.decompiler.languages.java.ast.AstBuilder.addType(AstBuilder.java:105)
		//     at com.strobel.decompiler.languages.java.JavaLanguage.buildAst(JavaLanguage.java:71)
		//     at com.strobel.decompiler.languages.java.JavaLanguage.decompileType(JavaLanguage.java:59)
		//     at us.deathmarine.luyten.FileSaver.doSaveJarDecompiled(FileSaver.java:192)
		//     at us.deathmarine.luyten.FileSaver.access.300(FileSaver.java:45)
		//     at us.deathmarine.luyten.FileSaver.4.run(FileSaver.java:112)
		//     at java.lang.Thread.run(Thread.java:745)
		//
		throw new IllegalStateException("An error occurred while decompiling this method.");
	}

	@Override
	public String toString() {
		return "tcp:";
	}
}

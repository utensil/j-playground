package test.tutorial.echo;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.Required;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.tutorial.echo.EchoClient;
import com.tutorial.echo.EchoServer;
import com.tutorial.echo.EchoServerHandler;


public class EchoClientTest {	
	
	private static final Logger logger = Logger.getLogger(
			EchoClientTest.class.getName());
	
	@Rule
    public ContiPerfRule i = new ContiPerfRule();
	
	Thread serverThread;
	
	final String host = "127.0.0.1";
	
	final short port = 8081;
	
	final int firstMessageSize = 256;
	
	class ConcurentEchoClientThread implements Callable<Integer>
	{
		public Integer call()
		{
			new EchoClient(host, port, firstMessageSize).run();
			return new Integer(0);
		}
	}

	@Before
	public void setUp() throws Exception {
		serverThread = new Thread() {
			public void run()
			{
				new EchoServer(port).run();
			}
		};
		serverThread.start();
	}

	@After
	public void tearDown() throws Exception {
		serverThread.stop();
	}

	public void testEchoClient(int secs) {
		EchoServerHandler.setMaxSleepSeonds(secs);
		new EchoClient(host, port, firstMessageSize).run();
	}
	
	@Test
    @PerfTest(invocations = 100, threads = 100)
	public void testSleepingOneSec()
	{		
		testEchoClient(1);
	}
	
	@Test
    @PerfTest(invocations = 100, threads = 100)
	public void testSleepingTwoSecs()
	{		
		testEchoClient(2);
	}
	
	@Test
    @PerfTest(invocations = 100, threads = 100)
	public void testSleepingFourSecs()
	{		
		testEchoClient(4);
	}
	
	@Test
    @PerfTest(invocations = 100, threads = 100)
	public void testSleepingEightSecs()
	{		
		testEchoClient(8);
	}
	
	class ConcurentEchoClientTask implements Callable<Integer>
	{
		public Integer call()
		{
			new EchoClient(host, port, firstMessageSize).run();
			return new Integer(0);
		}
	}
	
	//@Test
	public void testConcurentEchoClientByFixedThreadPool() {
		final int threadSize = 100;
		
		ExecutorService es = Executors.newFixedThreadPool(threadSize);
		
		Collection<ConcurentEchoClientTask> cts = new Vector<ConcurentEchoClientTask>();
		
		for(int i = 0; i < threadSize; ++i)
		{
			cts.add(new ConcurentEchoClientTask());
		}
		
		long startTime = System.currentTimeMillis();
		
		try {
			es.invokeAll(cts);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info(String.format("All messages have been echoed in %f seconds.", (System.currentTimeMillis() - startTime) / 1000.0 ));
	}

}

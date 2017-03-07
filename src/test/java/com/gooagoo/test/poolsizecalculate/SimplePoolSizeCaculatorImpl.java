package com.gooagoo.test.poolsizecalculate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.gooagoo.transport.test.mina.TimeUtils;

public class SimplePoolSizeCaculatorImpl extends PoolSizeCalculator
{
    /** 采集端mac地址 */
    public static String MAC = "AAAAAAAAAAAA";
    /** 采集终端秘钥 与 后台配置保持一致 */
    public static String ENCRYPTKEY = "12345678";
    /** 域名 */
    private static final String DOMAIN = "192.168.4.137";//"ptrans.test1.goago.cn";//"192.168.9.222"
    /** 端口 */
    private static final int PORT = 9999;

    private IoConnector connector;
    private IoSession session;

    public SimplePoolSizeCaculatorImpl()
    {
        this.connector = new NioSocketConnector();
        this.connector.getSessionConfig().setReadBufferSize(1024);
        this.connector.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE, 300);
        this.connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
        this.connector.getFilterChain().addLast("threadPool", new ExecutorFilter(Executors.newCachedThreadPool()));
        this.connector.getFilterChain().addLast("logger", new LoggingFilter());
        this.connector.setHandler(new ConcurrentTestClientHandler());

        ConnectFuture future = this.connector.connect(new InetSocketAddress(this.DOMAIN, this.PORT));
        future.awaitUninterruptibly();
        this.session = future.getSession();
    }

    @Override
    protected Runnable creatTask()
    {

        return new ShortSocketSendTask(this.connector, DOMAIN, PORT, this.buildObj(MAC));
        //return new AsyncIOTask();
    }

    @Override
    protected BlockingQueue<Runnable> createWorkQueue()
    {
        return new LinkedBlockingQueue<Runnable>(1000);
    }

    @Override
    protected long getCurrentThreadCPUTime()
    {
        return ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
    }

    public static void main(String[] args)
    {
        PoolSizeCalculator poolSizeCalculator = new SimplePoolSizeCaculatorImpl();
        poolSizeCalculator.calculateBoundaries(new BigDecimal(1.0), new BigDecimal(1000000000));
    }

    private Map<String, Object> buildObj(String mac)
    {
        String time = TimeUtils.getCurrentDateTime("yyyyMMddHHmmss");
        double amount = 300;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("shopid", TimeUtils.getCurrentDateTime("yyyyMMddHHmmssSSS"));//商家编号
        map.put("shopentityid", TimeUtils.getCurrentDateTime("yyyyMMddHHmmssSSS"));//实体店编号
        map.put("shopentityfullname", "实体店名称");//店铺全名

        map.put("terminalnumber", mac);//采集终端编号
        map.put("billno", "账单序号" + TimeUtils.getCurrentDateTime("HHmmssSSS"));//账单序号
        map.put("intercepttime", time);//截获时间
        map.put("shopentityaddress", "中山路");//店铺地址
        map.put("telephone", "010-12345678");//店铺电话
        map.put("saler", "售货员");//售货员
        map.put("checkstand", "收银台");//收银台
        map.put("cashier", "收银员");//收银员
        map.put("totalfee", 10.0);//总金额
        map.put("totalnum", 10.0);//商品总数
        map.put("receivableamount", amount);//应收金额
        map.put("paidamount", 10.0);//实收金额
        map.put("discountamount", 0.0);//折扣金额
        map.put("couponamount", 0.0);//优惠金额
        map.put("changeamount", 0.0);//找零金额
        map.put("saletime", time);//销售时间yyyyMMddHHmmss
        map.put("membercardnumber", "AABB11001100");//会员卡号
        map.put("totalconsumption", "2.0");//累计消费
        map.put("website", "http://www.baidu.com/");//网站
        map.put("billimage", "xiaopiao.png");//小票图片名称
        map.put("roomno", "房间1115");//房间号
        map.put("checkinname", "张三");//入住姓名
        map.put("deskno", "桌号1115");//桌号
        map.put("text", "minageshiminami" + TimeUtils.getCurrentDateTime("yyyyMMddHHmmss"));//”原始账单所有文本信息[必填]”

        return map;
    }
}

class LongSocketSendTask implements Runnable
{
    private final IoSession session;
    private final Map<String, Object> map;

    public LongSocketSendTask(IoSession session, Map<String, Object> map)
    {
        this.session = session;
        this.map = map;
    }

    @Override
    public void run()
    {
        this.session.write(this.map);
    }
}

class ShortSocketSendTask implements Runnable
{
    private final IoConnector connector;
    private final String DOMAIN;
    private final int PORT;
    private final Map<String, Object> map;

    public ShortSocketSendTask(IoConnector connector, String domain, int port, Map<String, Object> map)
    {
        this.connector = connector;
        this.DOMAIN = domain;
        this.PORT = port;
        this.map = map;
    }

    @Override
    public void run()
    {
        ConnectFuture future = this.connector.connect(new InetSocketAddress(this.DOMAIN, this.PORT));
        future.awaitUninterruptibly();
        IoSession session = future.getSession();
        session.write(this.map);
    }

}

/**
 * 自定义的异步IO任务
 * @author Will
 *
 */
class AsyncIOTask implements Runnable
{
    @Override
    public void run()
    {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try
        {
            String getURL = "http://www.baidu.com";
            URL getUrl = new URL(getURL);

            connection = (HttpURLConnection) getUrl.openConnection();
            connection.connect();
            reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null)
            {
                // empty loop
            }
        }

        catch (IOException e)
        {
            //System.out.println("IOException=" + e.getMessage());
        }
        finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                }
                catch (Exception e)
                {

                }
            }
            connection.disconnect();
        }

    }
}
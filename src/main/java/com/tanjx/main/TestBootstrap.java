package com.tanjx.main;

import javax.servlet.ServletContextEvent;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

/**
 * 主程序启动
 * @author jmb
 *
 */
public class TestBootstrap extends ContextLoaderListener
{
    private static Logger log = Logger.getLogger("gaglog");

    @Override
    public void contextInitialized(final ServletContextEvent event)
    {
        log.debug("TPOS拆解包服务启动>>>");
        long a = System.currentTimeMillis();
        super.contextInitialized(event);
        WebApplicationContext applicationContext = getCurrentWebApplicationContext();
        initTPosParser(applicationContext, event, this, a);
    }

    private static void initTPosParser(final ApplicationContext applicationContext, final ServletContextEvent event, final ContextLoaderListener contextLoaderListener, long startTime)
    {
        long b = System.currentTimeMillis();
        log.debug(String.format("TPOS拆解包服务启动成功,耗时[%s]毫秒>>>", (b - startTime)));

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                log.debug("TPOS拆解包服务停止开始>>>");
                if (event != null && contextLoaderListener != null)
                {
                    contextLoaderListener.contextDestroyed(event);
                }
                log.debug("TPOS拆解包服务停止成功>>>");
            }
        });
    }

    //非web启动
    public static void main(String[] args)
    {
        log.debug("TPOS拆解包服务启动>>>");
        long a = System.currentTimeMillis();
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:applicationContext-base.xml");
        initTPosParser(applicationContext, null, null, a);
    }
}
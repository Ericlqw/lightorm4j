package smartpush;

import javax.naming.NamingException;
import javax.resource.spi.ResourceAdapterInternalException;

import org.junit.runner.RunWith;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import cn.pconline.r.route.PcRouteJNDI;

/**
 * 
 * abstract unit test class
 * 
 * 1.单元测试抽象父类-可以mock jndi，极大简化了对环境的依赖，除了jdbc配置，包括r系统等常用依赖
 * 2.直接共用开发的配置文件，不需要再整一套配置文件，方便快捷
 * 
 * @author 崇锜
 * @updated Mike He
 * 
 * http://rdwiki.pc.com.cn/pages/viewpage.action?pageId=2031683
 * http://rdwiki.pc.com.cn/pages/viewpage.action?pageId=1016395
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
//spring配置文件 applicationContext.xml  E:\workspace\best_smartpush\src\main\webapp\WEB-INF
@ContextConfiguration(locations = {"file:E:\\workspace\\best_smartpush\\src\\main\\webapp\\WEB-INF\\applicationContext.xml"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
public abstract class AbstractTestCase{
    static {
        try {
            SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
            //java:comp/env/props  应用环境配置 E:\workspace\best_smartpush\conf
            builder.bind("java:comp/env/props","E:\\workspace\\best_smartpush\\conf\\best_smartpush_envirement.properties");
            //java:comp/env/app-name
            builder.bind("java:comp/env/app-name", "best_smartpush");
            //java:comp/env/app-name-override
            builder.bind("java:comp/env/app-name-override", "best_smartpush");
            
            builder.bind("java:comp/env/app-name-override", "best_smartpush");

            //java:comp/env/memCachedClientConfig4RClient
         /*   StringBuffer sb = new StringBuffer();
            sb.append("servers=192.168.20.118:11211\n");
            sb.append("initConn=20\n");
            sb.append("minConn=10\n");
            sb.append("maxConn=50\n");
            sb.append("maintSleep=30\n");
            sb.append("nagle=false\n");
            sb.append("socketTO=3000\n");
            builder.bind("java:comp/env/memCachedClientConfig4RClient", sb.toString());*/
            
            //java:comp/env/internalInetnums
            builder.bind("java:comp/env/internalInetnums", "192.168.236.0/22,192.168.230.0/24");

            //binding datasource
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.gjt.mm.mysql.Driver");
            dataSource.setUrl("jdbc:mysql://192.168.75.100:3311/best_smartpush");
            dataSource.setUsername("best_smartpush");
            dataSource.setPassword("best_smartpush");
            builder.bind("jdbc/best_smartpush", dataSource);

            //binding datasource
            DriverManagerDataSource dataSourceBest = new DriverManagerDataSource();
            dataSourceBest.setDriverClassName("org.gjt.mm.mysql.Driver");
            dataSourceBest.setUrl("jdbc:mysql://192.168.75.100:3311/pcbest?useUnicode=true&amp;characterEncoding=GBK&amp;zeroDateTimeBehavior=convertToNull");
            dataSourceBest.setUsername("pcbest");
            dataSourceBest.setPassword("pcbest");
            builder.bind("java:comp/env/jdbc/best", dataSourceBest);

            //PcRouteJNDI
            PcRouteJNDI  pcRouteJndi = new PcRouteJNDI();
            pcRouteJndi.setRouteUri("http://192.168.75.1/route.txt");
            pcRouteJndi.setDnsAddr("192.168.11.228");
            try {
                pcRouteJndi.start(null);
            } catch (ResourceAdapterInternalException ex) {
                ex.printStackTrace();
            }

            builder.bind("jca/pc_route", pcRouteJndi);

            builder.activate();
            //This is default test output directory, if changed in pom.xml please modify this bingding too.
        } catch (NamingException ex) {
            ex.printStackTrace();
        }

    } 
}     

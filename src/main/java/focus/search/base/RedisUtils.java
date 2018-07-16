package focus.search.base;

import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.util.Properties;
import java.util.Set;

public final class RedisUtils {

    private static final Logger logger = Logger.getLogger(RedisUtils.class);

    private static String ADDR;
    private static int PORT;
    private static int DB;

    private static final int MAX_TOTAL = 1024;
    private static final int MAX_IDLE = 200;
    private static final int MIN_IDLE = 8;
    private static final long MAX_WAIT_MILLIS = 10000L;

    // 访问密码
    // private static String AUTH = "admin";

    private static JedisPoolConfig config = new JedisPoolConfig();

    static {
        InputStream inputStream = null;
        try {
            File file = new File("/srv/focus/conf/search/redis.properties");
            if (!file.exists()) {
                file = new File(System.getProperty("user.dir") + "/src/main/resources/conf/redis.properties");
            }
            inputStream = new BufferedInputStream(new FileInputStream(file));
            Properties properties = new Properties();
            properties.load(inputStream);
            ADDR = properties.getProperty("redis_host", "127.0.0.1");
            PORT = Integer.parseInt(properties.getProperty("redis_port", "6379"));
            DB = Integer.parseInt(properties.getProperty("redis_db", "9"));

        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
        config.setMaxTotal(MAX_TOTAL);
        config.setMaxIdle(MAX_IDLE);
        config.setMinIdle(MIN_IDLE);
        config.setMaxWaitMillis(MAX_WAIT_MILLIS);
        config.setTestOnBorrow(true);
        config.setTestOnReturn(true);
        config.setTestWhileIdle(true);
    }

    private static JedisPool jedisPool = null;

    /*
      初始化Redis连接池
     */
    private static void initialPool() {
        try {
            jedisPool = new JedisPool(config, ADDR, PORT);
        } catch (Exception e) {
            logger.error("First create JedisPool error : " + e);
            try {
                jedisPool = new JedisPool(config, ADDR, PORT);
            } catch (Exception e2) {
                logger.error("Second create JedisPool error : " + e2);
            }
        }
    }

    /**
     * 在多线程环境同步初始化
     */
    private static synchronized void poolInit() {
        if (jedisPool == null) {
            initialPool();
        }
    }

    /**
     * 获取Jedis实例
     *
     * @return Jedis
     */
    private synchronized static Jedis getInstance() {
        if (jedisPool == null) {
            poolInit();
        }
        Jedis jedis = null;
        try {
            if (jedisPool != null) {
                jedis = jedisPool.getResource();
                jedis.select(DB);
            }
        } catch (Exception e) {
            logger.error("Get jedis error : " + e);
            if (jedis != null && jedis.isConnected()) {
                jedis.close();
            }
        }
        return jedis;
    }

    public static void set(String key, String value) {
        Jedis jedis = getInstance();
        jedis.set(key, value);
        if (jedis.isConnected()) {
            jedis.close();
        }
        logger.info(String.format("redis set: %s", key));
    }

    public static void delete(String key) {
        Jedis jedis = getInstance();
        jedis.del(key);
        if (jedis.isConnected()) {
            jedis.close();
        }
        logger.info(String.format("redis delete:%s", key));
    }

    public static String get(String key) {
        Jedis jedis = getInstance();
        String value = null;
        if (!jedis.exists(key)) {
            logger.warn(String.format("redis key is not exist:%s", key));
        } else {
            logger.info(String.format("redis  get:%s", key));
            value = jedis.get(key);
        }
        if (jedis.isConnected()) {
            jedis.close();
        }
        return value;
    }

    public static Set<String> keys(String key) {
        Jedis jedis = getInstance();
        Set<String> keys = jedis.keys(key);
        if (jedis.isConnected()) {
            jedis.close();
        }
        return keys;
    }

}

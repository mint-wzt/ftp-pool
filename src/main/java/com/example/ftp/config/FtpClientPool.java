package com.example.ftp.config;

import com.alibaba.fastjson.JSON;
import com.example.ftp.property.FtpPoolProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author wzt
 * @date 2021-12-12
 */
@Slf4j
@Component
public class FtpClientPool {

    /**
     * 连接池
     */
    private GenericObjectPool<FTPClient> ftpClientPool;

    private final FtpClientFactory ftpClientFactory;

    private final FtpPoolProperty clientPoolProperty;

    public FtpClientPool(FtpClientFactory ftpClientFactory, FtpPoolProperty clientPoolProperty) {
        this.ftpClientFactory = ftpClientFactory;
        this.clientPoolProperty = clientPoolProperty;
    }

    /**
     * 初始化连接池
     * 加上该注解表明该方法会在bean初始化后调用
     */
    @PostConstruct
    public void init() {
        log.info("===>>>ftp初始化 ftpClientFactory：{} \n clientPoolProperty: {}", JSON.toJSONString(ftpClientFactory), JSON.toJSONString(clientPoolProperty));
        // 初始化对象池配置
        GenericObjectPoolConfig<FTPClient> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(clientPoolProperty.getMaxTotal());
        poolConfig.setMinIdle(clientPoolProperty.getMinIdle());
        poolConfig.setMaxIdle(clientPoolProperty.getMaxIdle());
        poolConfig.setMaxWaitMillis(clientPoolProperty.getMaxWait());
        poolConfig.setBlockWhenExhausted(clientPoolProperty.isBlockWhenExhausted());
        poolConfig.setTestOnBorrow(clientPoolProperty.isTestOnBorrow());
        poolConfig.setTestOnReturn(clientPoolProperty.isTestOnReturn());
        poolConfig.setTestOnCreate(clientPoolProperty.isTestOnCreate());
        poolConfig.setTestWhileIdle(clientPoolProperty.isTestWhileIdle());
        poolConfig.setLifo(clientPoolProperty.isLifo());

        //空闲对象检查
        poolConfig.setMinEvictableIdleTimeMillis(clientPoolProperty.getMinEvictableIdleTimeMillis());
        poolConfig.setSoftMinEvictableIdleTimeMillis(clientPoolProperty.getSoftMinEvictableIdleTimeMillis());
        //每*毫秒运行一次维护任务
        poolConfig.setTimeBetweenEvictionRunsMillis(clientPoolProperty.getTimeBetweenEvictionRunsMillis());

        //设置移除超时未归还的对象，检查泄露
        AbandonedConfig abandonedConfig = new AbandonedConfig();
        //在Maintenance的时候检查是否有泄漏
        abandonedConfig.setRemoveAbandonedOnMaintenance(clientPoolProperty.isRemoveAbandonedOnMaintenance());
        //borrow 的时候检查泄漏
        abandonedConfig.setRemoveAbandonedOnBorrow(clientPoolProperty.isRemoveAbandonedOnBorrow());
        //如果一个对象borrow之后*秒还没有返还给pool，认为是泄漏的对象
        abandonedConfig.setRemoveAbandonedTimeout(clientPoolProperty.getRemoveAbandonedTimeout());

        // 初始化对象池
        ftpClientPool = new GenericObjectPool<>(ftpClientFactory, poolConfig, abandonedConfig);
    }

    public FTPClient borrowObject() throws Exception {
        return ftpClientPool.borrowObject();
    }

    public void returnObject(FTPClient ftpClient) {
        log.info("===>>>释放ftpClient:{}", ftpClient.hashCode());
        ftpClientPool.returnObject(ftpClient);
    }
}


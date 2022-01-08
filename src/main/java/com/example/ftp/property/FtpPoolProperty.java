package com.example.ftp.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author wzt
 * @date 2021-12-12
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "ftp-pool")
public class FtpPoolProperty {

    /**
     * 最大数
     */
    private int maxTotal;

    /**
     * 最小空闲数
     */
    private int minIdle;

    /**
     * 最大空闲数
     */
    private int maxIdle;

    /**
     * 最大等待时间 maxWait<0时一直等待
     */
    private long maxWait;

    /**
     * 池对象耗尽之后是否阻塞
     */
    private boolean blockWhenExhausted;

    /**
     * 取对象验证
     */
    private boolean testOnBorrow;

    /**
     * 回收验证
     */
    private boolean testOnReturn;

    /**
     * 创建时验证
     */
    private boolean testOnCreate;

    /**
     * 空闲验证
     */
    private boolean testWhileIdle;

    /**
     * 后进先出
     */
    private boolean lifo;

    /**
     * 连接空闲的最小时间，达到此值后空闲连接将可能会被移除
     */
    private long minEvictableIdleTimeMillis;

    /**
     * 连接空闲的最小时间，达到此值后空闲链接将会被移除，且保留minIdle个空闲连接数。默认为-1
     */
    private long softMinEvictableIdleTimeMillis;

    /**
     * 获取连接池对象时移除长时间为归还连接池的对象
     */
    private boolean removeAbandonedOnBorrow;

    /**
     * 维护并移除长时间为归还连接池的对象
     */
    private boolean removeAbandonedOnMaintenance;

    /**
     * 如果一个对象borrow之后*秒还没有返还给pool，认为是泄漏的对象 默认300秒
     */
    private int removeAbandonedTimeout;

    /**
     * 每*秒运行一次维护任务
     */
    private long timeBetweenEvictionRunsMillis;
}


package com.example.ftp.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author wzt
 * @date 2021-12-12
 */

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ftp")
public class FtpProperty {
    /**
     * #本地用于保存临时文件
     */
    private String localPath;

    /**
     * 文件前缀
     */
    private String filePrefix;

    /**
     * 文件上传失败下次超时重试时间 ms
     */
    private Integer uploadSleep = 500;

    /**
     * 文件上传失败重试次数
     */
    private Integer uploadRetry = 3;

    /**
     * 保存数据文件的目录
     */
    private String filePath;

    /**
     * FTP ip地址
     */
    private String host;
    /**
     * FTP 端口号
     */
    private int port;
    /**
     * FTP 用户名
     */
    private String username;
    /**
     * FTP 密码
     */
    private String password;

    /**
     * 连接超时时间 默认 0 表示一直连接
     */
    private int connectTimeout = 0;

    /**
     * 数据传输超时时间（下载）设置soTimeout
     */
    private int dataTimeout;

    /**
     * 数据传输超时时间（下载）设置soTimeout 会覆盖dataTime参数
     */
    private int soTimeout;

    /**
     * 编码格式 可用于处理文件名中文乱码的问题
     */
    private String controlEncoding = "UTF-8";

    /**
     * 默认1024字节 = 1KB
     */
    private int bufferSize = 1024;

    /**
     * 是否开启被动模式
     */
    private boolean passiveMode = true;

    /**
     * 客户端发送控制命令时间间隔（上传或者下载文件过程中）单位：秒（s）
     */
    private int controlKeepAliveTimeout = 3;

    /**
     * 客户端（发送控制命令后）接收服务端的超时时间（上传或者下载文件过程中）单位：毫秒（ms）
     */
    private int controlKeepAliveReplyTimeout = 10000;
}


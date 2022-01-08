package com.example.ftp.config;

import com.alibaba.fastjson.JSON;
import com.example.ftp.property.FtpProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.util.Charsets;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author wzt
 * @date 2021-12-12
 */
@Slf4j
@Component
public class FtpClientFactory extends BasePooledObjectFactory<FTPClient> {

    private final FtpProperty ftpProperty;

    public FtpClientFactory(FtpProperty ftpProperty) {
        this.ftpProperty = ftpProperty;
    }

    @Override
    public FTPClient create() throws IOException {
        log.info("===>>>开始创建ftpClient连接 ftpProperty:{}", JSON.toJSONString(ftpProperty));
        FTPClient ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(ftpProperty.getConnectTimeout());
        try {
            ftpClient.connect(ftpProperty.getHost(), ftpProperty.getPort());
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpClient.disconnect();
                log.error("===>>>FTPServer 连接失败,replyCode: {}", replyCode);
                return null;
            }

            if (!ftpClient.login(ftpProperty.getUsername(), ftpProperty.getPassword())) {
                log.error("===>>>ftpClient 登录失败： {}, {}" , ftpProperty.getUsername() , ftpProperty.getPassword());
                return null;
            }
            //文件类型
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setControlEncoding(ftpProperty.getControlEncoding());
            ftpClient.setCharset(Charsets.toCharset("utf-8"));
            ftpClient.setBufferSize(ftpProperty.getBufferSize());
            ftpClient.setControlKeepAliveTimeout(ftpProperty.getControlKeepAliveTimeout());
            ftpClient.setControlKeepAliveReplyTimeout(ftpProperty.getControlKeepAliveReplyTimeout());

            if (ftpProperty.isPassiveMode()) {
                //这个方法的意思就是每次数据连接之前，ftp client告诉ftp server开通一个端口来传输数据
                ftpClient.enterLocalPassiveMode();
            }
            ftpClient.setDataTimeout(ftpProperty.getDataTimeout());
            ftpClient.setSoTimeout(ftpProperty.getSoTimeout());
        } catch (IOException e) {
            log.error("===>>>FtpClient 创建失败： ", e);
            throw e;
        }
        log.info("===>>>创建ftpClient连接：{}", ftpClient.hashCode());
        return ftpClient;
    }

    /**
     * 验证FtpClient对象
     */
    @Override
    public boolean validateObject(PooledObject<FTPClient> ftpPooled) {
        try {
            FTPClient ftpClient = ftpPooled.getObject();
            log.info("===>>>开始验证ftpClient对象: {}",ftpClient.hashCode());
            return ftpClient.sendNoOp();
        } catch (IOException e) {
            log.error("===>>>验证FtpClient对象异常: ",e);
        }
        return false;
    }

    /**
     * 用PooledObject封装对象放入池中
     */
    @Override
    public PooledObject<FTPClient> wrap(FTPClient ftpClient) {
        log.info("===>>>ftpClient: {} 加入连接池", ftpClient.hashCode());
        return new DefaultPooledObject<>(ftpClient);
    }


    /**
     * 销毁FtpClient对象
     */
    @Override
    public void destroyObject(PooledObject<FTPClient> ftpPooled) {
        log.info("===>>>开始执行销毁FtpClient对象操作:{}", JSON.toJSONString(ftpPooled));
        if (ftpPooled == null) {
            return;
        }

        FTPClient ftpClient = ftpPooled.getObject();
        log.info("===>>>即将销毁FtpClient对象：{}",ftpClient.hashCode());
        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
            }
        } catch (Exception io) {
            log.error("===>>>退出FtpClient登录异常:", io);
        } finally {
            try {
                ftpClient.disconnect();
                log.info("===>>>销毁FtpClient对象{}成功", ftpClient.hashCode());
            } catch (IOException io) {
                log.error("===>>>销毁FtpClient异常:", io);
            }
        }
    }

}


package com.example.ftp.config;

import com.alibaba.fastjson.JSON;
import com.example.ftp.property.FtpProperty;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author wzt
 * @date 2021-12-12
 */
@Slf4j
@Component
public class FtpClientTemplate {

    private final FtpClientPool ftpClientPool;

    public FtpClientTemplate(FtpClientPool ftpClientPool) {
        this.ftpClientPool = ftpClientPool;
    }


    /**
     * 上传文件
     *
     * @param ftpClient
     * @param localFile
     * @param uploadRetry
     * @param uploadSleep
     * @return
     */
    public boolean upload(FTPClient ftpClient, File localFile, int uploadRetry, long uploadSleep) {
        boolean success = false;
        int i = 0;
        try (BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(localFile))) {
            while (!success && i <= uploadRetry) {
                i++;
                try {
                    success = ftpClient.storeFile(localFile.getName(), inStream);
                    if (success) {
                        log.info("====>>>>文件上传成功! {}", localFile.getName());
                        return true;
                    }
                } catch (Exception e) {
                    log.error("====>>>>ftp文件上传失败，重试中...第" + i + "次，错误信息", e);
                    if (i > uploadRetry) {
                        log.error("=====>>>>ftp文件上传失败，超过重试次数结束重试，错误信息", e);
                        return false;
                    }
                    TimeUnit.MILLISECONDS.sleep(uploadSleep);
                }
            }
        } catch (Exception e) {
            log.error("====>>>>文件上传错误! " + localFile.getName(), e);
        }
        return success;
    }

    /**
     * 批量上传文件
     *
     * @param dir
     * @param ftpProperty
     * @param dataList
     */
    public void batchUploadFile(File dir, FtpProperty ftpProperty, List<Map<String, String>> dataList) {
        // 获取ftp连接池中的ftpClient
        FTPClient ftpClient = getClient(ftpProperty.getFilePath());
        for (Map<String, String> item : dataList) {
            // 先创建本地临时文件
            String fileName = ftpProperty.getFilePrefix() + System.currentTimeMillis();
            boolean success = false;
            File tempFile = null;
            try {
                if (ftpClient == null) {
                    throw new RuntimeException("====>>>>获取ftpClient失败");
                }
                log.info("===>>>>上传数据：{}", JSON.toJSONString(item));
                tempFile = new File(dir, fileName + ".csv");
                log.info("===>>>> 本地路径：{} 文件名: {}", ftpProperty.getLocalPath(), tempFile.getName());
                try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tempFile))) {
                    bufferedWriter.write(item.get("data"));
                    bufferedWriter.flush();
                } catch (IOException e) {
                    throw new IOException(e);
                }
                success = upload(ftpClient, tempFile, ftpProperty.getUploadRetry(), ftpProperty.getUploadSleep());
            } catch (Exception e) {
                log.error("===>>>>上传异常", e);
            } finally {
                item.put("success", success ? "1" : "0");
                log.info("===>>>>文件:{} 上传：{}", fileName, success ? "成功" : "失败");
                // 删除临时文件
                if (tempFile != null && tempFile.exists()) {
                    try {
                        log.info("===>>>>临时文件：{} 大小: {} B", fileName, tempFile.length());
                        Files.delete(tempFile.toPath());
                        log.info("===>>>>删除临时文件 {} 成功", JSON.toJSONString(tempFile.toPath()));
                    } catch (Exception e) {
                        log.error("===>>>>文件删除异常", e);
                    }
                }
            }
        }
        try {
            // 释放资源
            if (ftpClient != null) {
                ftpClientPool.returnObject(ftpClient);
                log.error("===>>>>归还ftpClient对象：{}", ftpClient.hashCode());
            }
        } catch (Exception e) {
            log.error("===>>>>归还对象异常：", e);
        }
    }

    /**
     * 获取client
     *
     * @param remotePath
     * @return
     */
    public FTPClient getClient(String remotePath) {
        FTPClient ftpClient;
        try {
            //从池中获取对象
            ftpClient = ftpClientPool.borrowObject();
            // 验证FTP服务器是否登录成功
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                log.error("====>>>>FTP服务器校验失败, 上传replyCode:{}", replyCode);
                return null;
            } else {
                log.info("====>>>>获取ftpClient连接:{}", ftpClient.hashCode());
            }
            //切换到上传目录
            if (!ftpClient.changeWorkingDirectory(remotePath)) {
                //如果目录不存在创建目录
                String[] dirs = remotePath.split("/");
                String tempPath = "";
                for (String dir : dirs) {
                    if (null == dir || "".equals(dir)) {
                        continue;
                    }
                    tempPath += "/" + dir;
                    if (!ftpClient.changeWorkingDirectory(tempPath)) {
                        if (!ftpClient.makeDirectory(tempPath)) {
                            return null;
                        } else {
                            ftpClient.changeWorkingDirectory(tempPath);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("====>>>>获取ftpClient失败", e);
            return null;
        }
        return ftpClient;
    }
}


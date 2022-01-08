package com.example.ftp;

import com.example.ftp.config.FtpClientTemplate;
import com.example.ftp.property.FtpProperty;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class FtpApplicationTests {

    @Autowired
    private FtpClientTemplate ftpClientTemplate;

    @Autowired
    private FtpProperty ftpProperty;

    @Test
    void contextLoads() {
        File dir = new File(ftpProperty.getLocalPath());
        if (!dir.exists()){
            dir.mkdirs();
        }
        List<Map<String, String>> dataList = new ArrayList<>();
        for (int i = 0; i < 10; i++){
            Map<String, String> map = new HashMap<>();
            map.put("data", "key"+i+":hello world");
            dataList.add(map);
        }
        ftpClientTemplate.batchUploadFile(dir,ftpProperty,dataList);
    }

}

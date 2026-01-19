package com.smartuxapi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.yaml.snakeyaml.Yaml;

/**
 * @author KIUNSEA
 *
 */
public class PropertiesUtil {
    
    private final static String DEFAULT_PROPERTIES_PATH = "DEFAULT.PROPERTIES";
    public static String USER_PROPERTIES_PATH = null;

    /**
     * 프로그램에서 프로퍼티 파일 기본 경로 확인
     * @return 파일 경로
     */
    public static String getDefaultPropertiesPath() {
        return DEFAULT_PROPERTIES_PATH;
    }
    
    /**
     * 프로퍼티 파일 존재 여부
     * @return
     */
    public static boolean existsPropertiesFile() {
        if (USER_PROPERTIES_PATH != null) {
            return FileUtil.exists(USER_PROPERTIES_PATH);
        }
        return false;
    }
    
    /**
     * 프로퍼티 파일 경로를 지정한다
     * @param propFilePath
     */
    public static void setPropertiesFilePath(String propFilePath) {
        USER_PROPERTIES_PATH = propFilePath;
    }
    
    public static String get(String key) {
        String value = null;
        String filePath = (USER_PROPERTIES_PATH != null) ? USER_PROPERTIES_PATH : DEFAULT_PROPERTIES_PATH;
        
        // YAML 파일인지 확인
        if (filePath.endsWith(".yml") || filePath.endsWith(".yaml")) {
            value = getFromYaml(filePath, key);
        } else {
            // Properties 파일 읽기
            value = getFromProperties(filePath, key);
        }
        
        return value;
    }
    
    /**
     * Properties 파일에서 값을 읽어옵니다.
     */
    private static String getFromProperties(String filePath, String key) {
        String value = null;
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            Reader reader = new InputStreamReader(is, "UTF-8");
            Properties p = null;
            try {
                p = new Properties();
                p.load(reader);
                value = p.getProperty(key);
            } finally {
                reader.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {}
        }
        return value;
    }
    
    /**
     * YAML 파일에서 값을 읽어옵니다.
     */
    @SuppressWarnings("unchecked")
    private static String getFromYaml(String filePath, String key) {
        String value = null;
        InputStream is = null;
        try {
            is = new FileInputStream(filePath);
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(is);
            if (data != null && data.containsKey(key)) {
                Object obj = data.get(key);
                value = (obj != null) ? obj.toString() : null;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {}
        }
        return value;
    }

    /**
     * 프로퍼티 파일에 사용자 값을 저장한다.
     */
    public static void putProperties(Map<String, String> paramMap) throws FileNotFoundException, IOException {
        // 프로퍼티 파일 경로 key
        Properties proper = null;
        FileOutputStream output = null;
        try {
            String comment = paramMap.get("properties.comment").toString();
            output = new FileOutputStream((USER_PROPERTIES_PATH != null) ? USER_PROPERTIES_PATH : DEFAULT_PROPERTIES_PATH);
            proper = new Properties();
            proper.putAll(paramMap);
            proper.store(output, comment);
        } catch (FileNotFoundException fnfe) {
            throw new FileNotFoundException("properties 파일을 찾을수 없습니다 : "+fnfe);
        } catch (IOException ioe) {
            throw new IOException("putPropertie Exception!", ioe);
        } finally {
            try {
                if (output != null)
                    output.close();
            } catch (IOException e) {
                throw e;
            }
        }
    }

    /**
     * Class Test
     * @param args
     */
    /**
    public static void main(String[] args) throws Exception {
        
        PropertiesUtil.USER_PROPERTIES_PATH = "F:\\TOBE_TFS_WIT\\TOBE_TFS_WIT.PROPERTIES";
        
        // 테스트 코드
        Map<String, String> paramMap = new HashMap<String, String>();
        paramMap.put("properties.comment", "UPDATE_WIT");
        paramMap.put("name", "홍길동");
        paramMap.put("age", "31");
        paramMap.put("phone", "0111234567");
        PropertiesUtil.putProperties(paramMap);


        System.out.println(PropertiesUtil.getDefaultPropertiesPath());
        System.out.println(PropertiesUtil.get("name"));
    }
    */
}

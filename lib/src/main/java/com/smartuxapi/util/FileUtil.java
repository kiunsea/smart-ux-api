package com.smartuxapi.util;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;


/**
 * @author KIUNSEA
 * File 을 제어
 *
 */
public class FileUtil {

	/**
	 * Compile 시의 encoding (UTF-8)
	 */
	public static final String FILE_ENCODE_UTF_8 = "UTF-8";
	/**
	 * Compile 시의 encoding (UTF8)
	 */
	public static final String FILE_ENCODE_UTF8 = "UTF8";
	private static BufferedReader reader;
	
	/**
	 * 부모의 전체 경로를 추출해낸다.
	 * C:\\A dir\\B dir\\filename.txt -> C:\\dir\\B dir
	 * @param filepath
	 * @return
	 */
	public static String getParentPath(String filepath) {
		int index = filepath.lastIndexOf('/');
		if (index == -1) {
			index = filepath.lastIndexOf('\\');
		}

		return (index == -1) ? null : filepath.substring(0, index);
	}
	
    /**
     * 부모의 전체 경로를 추출해낸다.
     * C:\\A dir\\B dir\\filename.txt -> C:\\dir\\B dir
     * @param fileObj
     * @return
     */
    public static String getParentPath(File fileObj) {
        String filepath = fileObj.getAbsolutePath();
        int index = filepath.lastIndexOf('/');
        if (index == -1) {
            index = filepath.lastIndexOf('\\');
        }

        return (index == -1) ? null : filepath.substring(0, index);
    }
	
    /**
     * 파일 확장자를 추출해낸다.
     * ex) dir/filename.txt -> txt
     * @param filepath
     * @return
     */
    public static String getExtension(String filepath) {
        int index = filepath.lastIndexOf('.');

        return (index == -1) ? null : filepath.substring(index + 1);
    }

    /**
     * 파일 이름만 추출해낸다.
     * ex) dir/filename.txt -> filename
     * @param filepath
     * @return
     */
    public static String getSimpleName(String filepath) {
        String name = getFullName(filepath);

        int index = name.lastIndexOf('.');

        return (index == -1) ? name : name.substring(0, index);
    }
    
	/**
	 * 파일 이름 + 확장자를 추출해낸다.
	 * ex) dir/filename.txt -> filename.txt
	 * @param filepath
	 * @return
	 */
	public static String getFullName(String filepath) {
		int index = filepath.lastIndexOf('/');
		if (index == -1) {
			index = filepath.lastIndexOf('\\');
		}

		return (index == -1) ? filepath : filepath.substring(index + 1);
	}
	
	/**
	 * 부모의 디렉토리를 생성한다.
	 * @param filepath
	 * @return 성공여부
	 */
	public static boolean makeParentDirs(String filepath) {
		return makeParentDirs(new File(filepath));
	}
	
	/**
	 * 부모의 디렉토리를 생성한다.
	 * @param fileObj
	 * @return 성공여부
	 */
	public static boolean makeParentDirs(File fileObj) {
		File parent = fileObj.getParentFile();

		return (parent.exists()) ? true : parent.mkdirs();
	}
	
	/**
	 * 입력 경로의 폴더 존재여부를 검사후 없으면 생성(부모 경로 포함)
	 * File 클래스의 함수를 wrapping
	 * @param filepath
	 * @return 성공여부
	 */
    public static boolean makeDirs(String filepath) {
        boolean rtnVal = false;
        File f = new File(filepath);
        rtnVal = f.mkdirs();
        f = null;
        return rtnVal;
    }

    /**
     * 파일복사
     * @param srcFile
     * @param destFile
     * @throws Exception
     */
    public static void copyFile(File srcFile, File destFile) throws FileNotFoundException, IOException {
		makeParentDirs(destFile);
        @SuppressWarnings("resource")
        FileChannel sourceChannel = new FileInputStream(srcFile).getChannel();
        @SuppressWarnings("resource")
        FileChannel destinationChannel = new FileOutputStream(destFile).getChannel();
        sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
        sourceChannel.close();
        destinationChannel.close();
    }
    
    /**
     * 디렉토리인 경우 내부를 비우고 File 을 삭제한다. (하위경로 포함)
     * @param fileObj
     * @return
     */
    public static boolean deleteFile(File fileObj) {
    	
    	if (!fileObj.exists()) {
			return false;
		}
    	
    	if (fileObj.isDirectory()) {
			File[] files = fileObj.listFiles();
			for (int i = 0; i < files.length; i++) {
				deleteFile(files[i]);
			}
		}

    	return fileObj.delete();
    }
    
    /**
     * copy and delete
     * @param source
     * @param target
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void moveFile(File source, File target) throws FileNotFoundException, IOException {
        copyFile(source, target);
        System.gc();
        deleteFile(source);
    }
    
	/**
	 * 모든 하위 폴더를 조사하여 파일들의 목록을 반환한다 (디렉토리는 제외하고 파일만 반환)
	 * @param fileObj
	 * @return File object list (File class)
	 */
	public static List<File> listFiles(File fileObj) {

		if (!fileObj.exists()) {
			return null;
		}

		List<File> fileList = new ArrayList<File>();

		if (fileObj.isDirectory()) {
			File[] files = fileObj.listFiles();
			for (int i = 0; i < files.length; i++) {
				fileList.addAll(listFiles(files[i]));
			}
		} else {
			fileList.add(fileObj);
		}

		return fileList;
	}

    /**
     * 디렉토리의 내부를 비운다 (하위경로 포함)
     * @param folder
     * @return
     */
    public static void truncateFile(File folder) {
    	
    	if (!folder.exists()) {
			return;
		}
    	
    	if (folder.isDirectory()) {
			File[] files = folder.listFiles();
			for (int i = 0; i < files.length; i++) {
				deleteFile(files[i]);
			}
		}
    }
    
    /**  
     * 파일의 내용을 읽어 String으로 리턴한다.  
     * @param fileObj  
     * @return  
     * @throws FileNotFoundException  
     * @throws IOException
     */  
    public static String readFile(File fileObj) throws FileNotFoundException, IOException {   
        StringBuffer fileText = new StringBuffer();
        if (fileObj.exists()) {
        	BufferedReader bufferedReader = null;
        	try {
	            bufferedReader = new BufferedReader(new FileReader(fileObj));   
	            String textLine = null;
	            boolean firstFetch = true;
	            while ((textLine = bufferedReader.readLine()) != null) {
	            	if (!firstFetch) {
						fileText.append(System.getProperty("line.separator"));
					}
	                fileText.append(textLine);
	                firstFetch = false;
	            }
        	} finally {
        		if (bufferedReader != null) {
        			bufferedReader.close();
				}
        	}
        }   
        return fileText.toString();
    }
    
    /**
     * 현재 실행중인 클래스의 경로를 기준으로 text 파일을 찾아 내용을 반환한다.
     * 
     * @param curClass : this.getClass()
     * @param filePath : /로 시작하면 클래스 경로의 최상위 디렉토리로, 그렇지 않으면 현재 클래스가 위치한 패키지를 기준으로 한다.
     * @return
     * @throws IOException
     */
    public static String readFile(Class<Object> curClass, String filePath) throws IOException {
        // 현재 클래스의 경로를 기준으로 파일을 찾음
        InputStream inputStream = curClass.getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new IOException("파일을 찾을 수 없습니다: " + filePath);
        }

        // BufferedReader를 사용하여 InputStream을 읽고, String으로 변환
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
	/**
	 * 파일의 내용을 읽어 StringBuilder로 리턴한다. (멀티쓰레드로 성능 향상)
	 * @param filepath 파일경로
	 * @param encode 인코딩정보 (ex>UTF8)
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static StringBuilder readFile(String filepath, String encode)
			throws UnsupportedEncodingException, FileNotFoundException,
			IOException {

		if (encode == null || encode.trim().length() < 1)
			encode = "UTF8";
		
		reader = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), encode));

		StringBuilder contentSb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			contentSb.append(line + System.getProperty("line.separator"));
		}

		return contentSb;
	}
	
    /**
     * 파일이름을 변경한다
     * beforeName 과 afterName 은 full path 로 기입한다
     * @param beforeName
     * @param afterName
     * @return 변경된 File 객체
     */
    public static File rename(String beforeName, String afterName) {
        File file_name = null;// 원래 파일이름
        File file_rename = null;// 바꾼 파일이름
        if (beforeName != null) {
            file_name = new File(beforeName);
            file_rename = new File(afterName);

            if (file_name.renameTo(file_rename)) {
                return file_rename;
            }

        }
        return null;
    }
	
	/**
	 * 파일의 내용을 읽어 String List로 리턴한다.
	 * @param filepath 파일경로
	 * @param encode 인코딩정보 (default:UTF8, ex>UTF8)
     * @param seperator item을 구분할 구분자(default:줄바꿈, 공백 지정 가능)
     * @return split한 결과 string 목록 리스트
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
    @SuppressWarnings("unused")
    public static List<String> readFile(String filepath, String encode, String seperator)
            throws UnsupportedEncodingException, FileNotFoundException, IOException {

        encode = encode == null ? "UTF8" : encode;
        seperator = seperator == null ? "\n" : seperator;
        
        @SuppressWarnings("resource")
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filepath), encode));

        List<String> stringList = new ArrayList<String>();
        String line = null;
        if (seperator == null) {
            while ((line = reader.readLine()) != null) {
                stringList.add(line);
            }
        } else {
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String strTmp = sb.toString();
            String[] listTmp = strTmp.split(" ");
            for (int i = 0; i < listTmp.length; i++) {
                stringList.add(listTmp[i]);
            }
        }

        return stringList;
	}	

	/**
	 * 파일을 생성하고 내용을 출력한다
	 * @param filepath 파일경로
	 * @param contents 내용
	 * @param encode 인코딩정보 (ex>UTF8)
	 * @return 파일 생성 결과 (true/false)
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static boolean writeFile(String filepath, String contents, String encode)
			throws UnsupportedEncodingException, FileNotFoundException, IOException {

		if (encode == null || encode.trim().length() < 1)
			encode = "UTF8";
		
		makeParentDirs(filepath);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(filepath), encode));

		char intxt[] = new char[contents.length()];
		contents.getChars(0, contents.length(), intxt, 0);
		writer.write(intxt);
		writer.close();

		return true;
	}
    
    /**
     * file 내에 BOM이 존재하는지 여부 검사
     * @param fileObj
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static boolean checkBOM(File fileObj) throws FileNotFoundException, IOException {

    	DataInputStream dataInputStream = null;
        int firstChar = -1;   
        int secondChar = -1;   
        int thirdChar = -1;
    	
        try {

	    	dataInputStream = new DataInputStream(new FileInputStream(fileObj));       
	        firstChar = dataInputStream.readByte();   
	        secondChar = dataInputStream.readByte();   
	        thirdChar = dataInputStream.readByte();
        
        } finally {
        	if(dataInputStream != null) {
        		dataInputStream.close();
        	}
        }
           
        if (firstChar == -17 && secondChar == -69 && thirdChar == -65) {
        	return true;
        }
        
        return false;
    }
    
    /**
     * File의 32bit CRC값을 long으로 리턴한다
     * @param filepath
     * @return CRC32 (long)
     * @throws IOException
     */
    public static long getFileCRC32Long(String filepath) throws IOException {
    	long crc32Long = -1;
		CRC32 crc32 = new CRC32();

		CheckedInputStream in = null;

		try {
			in = new CheckedInputStream(new FileInputStream(filepath), crc32);
			while (in.read() != -1);
			crc32Long = crc32.getValue();
		} catch (FileNotFoundException e) {
			System.err.println("FileUtils.getFileCRC32(): " + e);
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("FileUtils.getFileCRC32(): " + e);
			System.exit(-1);
		} finally {
			if(in != null) {
				in.close();	
			}
		}

		return crc32Long;
	}
	
    /**
     * 파일사이즈를 기가바이트 단위로 리턴(소수점 이하도 포함)
     * @param f
     * @return 파일사이즈(GB)
     */
    public static double getFileSize(File f) {
        return f.length() / Math.pow(1024, 3);
    }
	
	/**
	 * 파일 존재 여부
	 * @param filepath
	 * @return
	 */
	public static boolean exists(String filepath) {

		return new File(filepath).exists();
	}

	private FileUtil() {
		;
	}
}

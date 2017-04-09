package plugins;

import com.harsen.app.utils.annotation.Command;
import com.harsen.app.utils.annotation.Control;
import com.harsen.app.utils.annotation.Param;
import com.harsen.app.utils.util.StringUtils;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件编码处理工具
 * Created by HarsenLin on 2016/6/5.
 */
@Control(name = "file", tip = "文件编码处理工具")
public class file {

    /**
     * 获取文件编码
     * @param path 文件路径
     */
    @Command(name = "encode", tip = "查看编码")
    public static void encode(@Param(name = "path", tip = "文件路径")String path){
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        BufferedInputStream bis = null;
        try {
            boolean checked = false;
            bis = new BufferedInputStream(new FileInputStream(path));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1){
                System.out.println(charset);
                return;
            }
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "Unicode";//UTF-16LE
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
                charset = "Unicode";//UTF-16BE
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF8";
                checked = true;
            }
            bis.reset();
            if (!checked) {
                while ((read = bis.read()) != -1) {
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) //单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) //双字节 (0xC0 - 0xDF) (0x80 - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) { //也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
            System.out.println(charset);
        }catch (FileNotFoundException e){
            System.out.println("文件不存在：" + path);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (bis != null) try { bis.close(); } catch (IOException ex) { ex.printStackTrace(); }
        }
    }

    @Command(name = "cat", tip = "加载文件")
    public static void cat(
            @Param(name = "path", tip = "文件路径")String path
            , @Param(name = "encode", tip = "文件编码")String encode
            , @Param(name = "position", tip = "开始加载位置")String position) {
        Charset charset = Charset.forName(encode);
        CharsetDecoder decoder = charset.newDecoder();
        RandomAccessFile rafRead = null;
        try {
            byte b;
            CharBuffer cb = CharBuffer.allocate(200);
            ByteBuffer buf = ByteBuffer.allocate(200);
            rafRead = new RandomAccessFile(path, "r");
            rafRead.seek(Long.valueOf(position));
            while (true){
                try { b = rafRead.readByte(); } catch (EOFException e) { break; }

                buf.put(b);
                if (b == 10) {
                    buf.flip();
                    decoder.decode(buf, cb, false);
                    cb.flip();
                    System.out.print(cb);
                    cb.clear();
                    buf.clear();
                }
            }
            buf.flip();
            decoder.decode(buf, cb, false);
            cb.flip();
            System.out.println(cb);
            cb.clear();
            buf.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try { if (null != rafRead) rafRead.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    @Command(name = "collect", tip = "采集文件")
    public static void collect(
            @Param(name = "path", tip = "文件路径")String path
            , @Param(name = "encode", tip = "文件编码")String encode
            , @Param(name = "position", tip = "开始加载位置")String position
            , @Param(name = "toPath", tip = "保存位置")String toPath
            , @Param(name = "toEncode", tip = "保存编码")String toEncode) {
        Charset charset = Charset.forName(encode);
        CharsetDecoder decoder = charset.newDecoder();
        RandomAccessFile rafRead = null;
        RandomAccessFile exportRAF = null;
        try {
            byte b;
            CharBuffer cb = CharBuffer.allocate(200);
            ByteBuffer buf = ByteBuffer.allocate(200);
            rafRead = new RandomAccessFile(path, "r");
            exportRAF = new RandomAccessFile(toPath, "rw");
            rafRead.seek(Long.valueOf(position));
            while (true){
                try { b = rafRead.readByte(); } catch (EOFException e) { break; }

                buf.put(b);
                if (b == 10) {
                    buf.flip();
                    decoder.decode(buf, cb, false);
                    cb.flip();
                    exportRAF.write(cb.toString().getBytes(toEncode));
                    cb.clear();
                    buf.clear();
                }
            }
            buf.flip();
            decoder.decode(buf, cb, false);
            cb.flip();
            exportRAF.write(cb.toString().getBytes(toEncode));
            cb.clear();
            buf.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try { if (null != rafRead) rafRead.close(); } catch (IOException e) { e.printStackTrace(); }
            try { if (null != exportRAF) exportRAF.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    @Command(name = "mkFiles", tip = "创建文件")
    public static void mkFiles(
            @Param(name = "path", tip = "文件路径，变量（${i},${times}）")String path
            , @Param(name = "size", tip = "文件数")String size
            , @Param(name = "delay", tip = "创建间隔时间")String delay) {
        Map<String, String> valMap = new HashMap<String, String>();

        String tempPath;
        long delayLong = Long.valueOf(delay);
        int sizeInt = Integer.valueOf(size);
        for (int i = 0; i < sizeInt; i ++){
            valMap.put("i", String.valueOf(i));
            valMap.put("times", String.valueOf(System.currentTimeMillis()));
            tempPath = StringUtils.replace(path, valMap);
            File file = new File(tempPath);
            file.getParentFile().mkdirs();
            try {
                if(!file.createNewFile()){
                    System.out.println("文件创建失败：" + tempPath);
                }
            } catch (IOException e) {
                System.out.println("文件创建失败[" + tempPath + "]：" + e.getMessage());
            }

            if(delayLong > 0){
                try { Thread.sleep(delayLong); } catch (InterruptedException e) { break; }
            }
        }
    }

    public static void main(String[] args) {
        mkFiles("Z:\\base\\workspace\\flume-maven-im\\watcher-logs\\test-${i}-${times}.log", "3000", "0");
    }

}

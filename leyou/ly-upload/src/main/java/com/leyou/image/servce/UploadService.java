package com.leyou.image.servce;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @package: com.leyou.image.servce
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:
 */
@Service
public class UploadService {

    // 支持的文件类型
    private static final List<String> suffixes = Arrays.asList("image/png", "image/jpeg", "image/bmp");

    public String upload(MultipartFile file){

        //对文件的类型进行验证
        String contentType = file.getContentType();
        if(!suffixes.contains(contentType)){
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }

        //对图片的内容进行验证
        BufferedImage image = null;
        try {
            image = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }

        if(null == image){
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }
        //保存图片

        File dir = new File("C:/hm70/tools/nginx-1.12.2/html");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            file.transferTo(new File(dir, file.getOriginalFilename()));
            // 2.3、拼接图片地址
            return "http://image.leyou.com/" + file.getOriginalFilename();
        } catch (IOException e) {
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }

    }

}

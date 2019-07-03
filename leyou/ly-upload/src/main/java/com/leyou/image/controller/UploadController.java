package com.leyou.image.controller;

import com.leyou.image.servce.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @package: com.leyou.image.controller
 * @version: V1.0
 * @author: Administrator
 * @date: 2019/4/21 22:12
 * @description:图片的上传功能
 */
@RestController
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @PostMapping("image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file){
        return ResponseEntity.ok(uploadService.upload(file));
    }

    @GetMapping("signature")
    public ResponseEntity<Map<String,Object>> getAliSignature(){
        return ResponseEntity.ok(uploadService.getSignature());
    }


}

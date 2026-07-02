package com.project.platform.controller;

import cn.hutool.core.io.FileUtil;
import com.project.platform.config.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/files")
@Slf4j
public class FilesController {

    private static final String filePath = System.getProperty("user.dir") + "/files/";

    @PostMapping("/upload")
    public Result upload(MultipartFile file) {  // 文件流的形式接收前端发送过来的文件
        String originalFilename = file.getOriginalFilename();  // xxx.png

        if (!FileUtil.isDirectory(filePath)) {  // 如果目录不存在 需要先创建目录
            FileUtil.mkdir(filePath);  // 创建一个 files 目录
        }
        // 提供文件存储的完整的路径
        // 给文件名 加一个唯一的标识
        String fileName = System.currentTimeMillis() + "_" + originalFilename;  // 156723232322_xxx.png
        String realPath = filePath + fileName;  // 完整的文件路径

        try {
            FileUtil.writeBytes(file.getBytes(), realPath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("文件上传失败");
        }
        String url = "http://localhost:1000/files/download/" + fileName;
        return Result.success(url);
    }
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable("fileName") String fileName, HttpServletResponse response){
        try {
            response.addHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            response.setContentType("application/octet-stream");
            OutputStream os = response.getOutputStream();
            String realPath = filePath + fileName;
            byte[] bytes = FileUtil.readBytes(realPath);
            os.write(bytes);
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("文件下载失败");
        }
    }
}
package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Alvin
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {

    private Logger logger= LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file,String path){
        String fileName=file.getOriginalFilename();
        String fileExtensionName=fileName.substring(fileName.lastIndexOf('.')+1);
        String uploadFileName= UUID.randomUUID().toString()+"."+fileExtensionName;
        logger.info("Begin to upload file, upload file name:{},upload path:{},new file name:{}",fileName,path,uploadFileName);

        File fileDir=new File(path);

        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile=new File(path,uploadFileName);

        try {
            file.transferTo(targetFile);
            FTPUtil.uploadFile(Lists.newArrayList(targetFile));
            targetFile.delete();
        } catch (IOException e) {
            logger.error("Exception when uploading file",e);
            return null;
        }
        return targetFile.getName();
    }
}

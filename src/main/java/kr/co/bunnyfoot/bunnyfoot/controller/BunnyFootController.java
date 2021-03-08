package kr.co.bunnyfoot.bunnyfoot.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiModelProperty;
import kr.co.bunnyfoot.bunnyfoot.dto.BbtiReqDto;
import kr.co.bunnyfoot.bunnyfoot.dto.BbtiResDto;
import kr.co.bunnyfoot.bunnyfoot.dto.PredictResDto;
import kr.co.bunnyfoot.bunnyfoot.feign.PredictClient;

@RestController
@RequestMapping("/")
public class BunnyFootController {
  
  @Autowired
  private AmazonS3Client amazonS3Client;
  
  @Autowired
  private PredictClient predictClient;
  
  @Value("${cloud.aws.s3.bucket}")
  private String bucket;
  
  @PostMapping("bbti")
  @ApiImplicitParams({
    @ApiImplicitParam(name = "answers", value = "answers", dataType = "string", paramType = "form", example = "0,0,0,0,0,0,0,0,0", required = true),
    @ApiImplicitParam(name = "image", value = "image", dataType = "file", paramType = "form", required = true)
  })
  public BbtiResDto getBbti(
      @RequestPart(value = "answers", required = true) String answers,
      @RequestPart(value = "image", required = true) MultipartFile image) throws Exception {
    
    BbtiResDto result = new BbtiResDto();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
    
    File imageFile = new File(image.getOriginalFilename()); 
    if(imageFile.createNewFile()) { 
      try (FileOutputStream fos = new FileOutputStream(imageFile)) { 
        fos.write(image.getBytes()); 
      } 
    }
    
    amazonS3Client.putObject(new PutObjectRequest(bucket, df.format(new Date()), imageFile).withCannedAcl(CannedAccessControlList.PublicRead));
    
    result.setBbti(null);
    
    PredictResDto predictRes = predictClient.predict(image);
    if(predictRes.getProbability() < 0.3) {      
      result.setPredict("NORMAL");
    }
    else if(predictRes.getProbability() < 0.5) {      
      result.setPredict("WATCH");
    }
    else if(predictRes.getProbability() < 0.7) {      
      result.setPredict("WARNING");
    }
    else {      
      result.setPredict("DANGER");
    }
    
    return result;
  }
}

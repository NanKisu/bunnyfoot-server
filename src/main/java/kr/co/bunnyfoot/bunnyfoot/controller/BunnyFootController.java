package kr.co.bunnyfoot.bunnyfoot.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import kr.co.bunnyfoot.bunnyfoot.dto.BbtiDto;
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

  @PostMapping("/imageUpload")
  public String uploadSingle(@RequestParam("image") MultipartFile image) throws Exception {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
    
    File imageFile = new File(image.getOriginalFilename()); 
    if(imageFile.createNewFile()) { 
      try (FileOutputStream fos = new FileOutputStream(imageFile)) { 
        fos.write(image.getBytes()); 
      } 
    }
    
    amazonS3Client.putObject(new PutObjectRequest(bucket, df.format(new Date()), imageFile).withCannedAcl(CannedAccessControlList.PublicRead));

    predictClient.predict(image);
    
    return "uploaded";
  }
  
  @PostMapping("/bbti")
  public BbtiDto getBbti(List<Integer> answers, @RequestParam("image") MultipartFile image) throws Exception {
    BbtiDto result = new BbtiDto();
    result.setBbti(null);
    result.setPredict(predictClient.predict(image));
    return result;
  }
}

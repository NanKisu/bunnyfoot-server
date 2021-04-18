package kr.co.bunnyfoot.bunnyfoot.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
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
import kr.co.bunnyfoot.bunnyfoot.config.QuestionConfig;
import kr.co.bunnyfoot.bunnyfoot.dto.BbtiReqDto;
import kr.co.bunnyfoot.bunnyfoot.dto.BbtiResDto;
import kr.co.bunnyfoot.bunnyfoot.dto.MySlackSendDto;
import kr.co.bunnyfoot.bunnyfoot.dto.PredictResDto;
import kr.co.bunnyfoot.bunnyfoot.dto.QuestionDto;
import kr.co.bunnyfoot.bunnyfoot.dto.SlackSendDto;
import kr.co.bunnyfoot.bunnyfoot.feign.PredictClient;
import kr.co.bunnyfoot.bunnyfoot.feign.SlackClient;
import kr.co.bunnyfoot.bunnyfoot.googlaanalytics.GoogleAnalyticsReporting;

@RestController
@RequestMapping("/")
@CrossOrigin(origins="*", allowedHeaders="*")
public class BunnyFootController {
  
  @Autowired
  private AmazonS3Client amazonS3Client;
  
  @Autowired
  private PredictClient predictClient;
  
  @Autowired
  private SlackClient slackClient;
  
  @Autowired
  private GoogleAnalyticsReporting googleAnalyticsReporting;
  
  @Autowired
  private QuestionConfig questionConfig;
  
  @Value("${cloud.aws.s3.bucket}")
  private String bucket;
    
  @PostMapping("bbti")
  @ApiImplicitParams({
    @ApiImplicitParam(name = "answers", value = "answers", dataType = "string", paramType = "form", example = "0,0,0,0,0,0,0,0,0", required = true),
    @ApiImplicitParam(name = "image", value = "image", dataType = "file", paramType = "form", required = true)
  })
  public BbtiResDto getBbti(
      @RequestPart(value = "answers", required = true) String answers,
      @RequestPart(value = "image", required = false) MultipartFile image) throws Exception {
    BbtiResDto result = new BbtiResDto();
    
    if(!ObjectUtils.isEmpty(image)) {
      SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
      File imageFile = new File(image.getOriginalFilename()); 
      if(imageFile.createNewFile()) { 
        FileOutputStream fos = new FileOutputStream(imageFile);
        fos.write(image.getBytes()); 
      }
      
      amazonS3Client.putObject(new PutObjectRequest(bucket, df.format(new Date()), imageFile).withCannedAcl(CannedAccessControlList.PublicRead));

      try {
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
      }
      catch (Exception e) {
        result.setPredict("ERROR");
        exceptionHandeler(e);
      }
    }
    
    Map<String, QuestionDto> questionMap = questionConfig.getQuestionMap();
    String[] answerList = answers.split(",");
    Integer cur = 1;
    QuestionDto curQuestion = questionMap.get("1");
    while(true) {
      if(!ObjectUtils.isEmpty(curQuestion.getResult())) {
        result.setBbti(curQuestion.getResult());
        break;
      }
      
      if(answerList[cur - 1].equals("0")) {
        curQuestion = questionMap.get(curQuestion.getYestNextNo());
      }
      else {
        curQuestion = questionMap.get(curQuestion.getNoNextNo());        
      }
      cur = Integer.parseInt(curQuestion.getCurNo().substring(curQuestion.getCurNo().length() - 1));
      if(ObjectUtils.isEmpty(curQuestion)) {
        break;
      }
    }
    
    return result;
  }
  
  @GetMapping("pageView")
  public String getPageView() {
    return googleAnalyticsReporting.getPageView();
  }
  
  @PostMapping("sendSlackMsg")
  public String sendSlackMsg(@RequestBody MySlackSendDto mySlackSend) {
    SlackSendDto slackSend = new SlackSendDto();
    slackSend.setText(mySlackSend.getMsg());
    
    if(mySlackSend.getChannel().equals("welcome")) {      
      slackClient.sendSlackMsgToWelcome(slackSend);
    }
    else if(mySlackSend.getChannel().equals("error")){
      slackClient.sendSlackMsgToError(slackSend);      
    }
    else {
      return "FAIL";
    }
    
    return "SUCCESS";
  }
  
  @ExceptionHandler(Exception.class)
  public void exceptionHandeler(Exception e) {
	  SlackSendDto slackSend = new SlackSendDto();
	  String errMsg = "";
	  for(StackTraceElement ste : e.getStackTrace()) {
		  errMsg += ste.toString() + "\n";
	  }
	  slackSend.setText(errMsg);
	  slackClient.sendSlackMsgToError(slackSend);
  }
}

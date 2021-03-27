package kr.co.bunnyfoot.bunnyfoot.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import feign.Headers;
import feign.Param;
import kr.co.bunnyfoot.bunnyfoot.dto.PredictResDto;
import kr.co.bunnyfoot.bunnyfoot.dto.SlackSendDto;

@FeignClient(name="feign2", url="https://hooks.slack.com/services")
public interface SlackClient {

    @PostMapping(path = {"${slack.welcome}"})
    public void sendSlackMsgToWelcome(@RequestBody SlackSendDto slackSend);
    
    @PostMapping(path = {"${slack.error}"})
    public void sendSlackMsgToError(@RequestBody SlackSendDto slackSend);
}
package kr.co.bunnyfoot.bunnyfoot.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import feign.Headers;
import feign.Param;
import kr.co.bunnyfoot.bunnyfoot.dto.PredictDto;

@FeignClient(name="feign", url="http://localhost:5000")
public interface PredictClient {

    @PostMapping(path = {"/predict"}, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public PredictDto predict(@Param("image") MultipartFile image);
}
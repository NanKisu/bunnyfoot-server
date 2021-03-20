package kr.co.bunnyfoot.bunnyfoot.config;

import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import kr.co.bunnyfoot.bunnyfoot.dto.QuestionDto;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "question")
public class QuestionConfig {
  private Map<String, QuestionDto> questionMap;
}

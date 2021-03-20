package kr.co.bunnyfoot.bunnyfoot.dto;

import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import lombok.Data;

@Data
public class QuestionDto {
  private String curNo;
  private String yestNextNo;
  private String noNextNo;
  private String result;
}

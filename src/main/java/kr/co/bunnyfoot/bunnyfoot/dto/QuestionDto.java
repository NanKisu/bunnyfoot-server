package kr.co.bunnyfoot.bunnyfoot.dto;

import java.util.Map;

import lombok.Data;

@Data
public class QuestionDto {
  private Map<String, ScoreDto> scoreMap;
}

package kr.co.bunnyfoot.bunnyfoot.dto;

import lombok.Data;

@Data
public class PredictDto {
  private Double probability;
  private Boolean success;
}

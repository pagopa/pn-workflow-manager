package it.pagopa.pn.workflowmanager.dto.timeline.details.common;

import lombok.Getter;

@Getter
public enum DeliveryModeInt {
  
  DIGITAL("DIGITAL"),
  
  ANALOG("ANALOG");

  private final String value;

  DeliveryModeInt(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }
  
}


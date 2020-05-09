
package com.crio.warmup.stock.portfolio;

import org.springframework.web.client.RestTemplate;

public class PortfolioManagerFactory {

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Implement the method in such a way that it will return new Instance of
  // PortfolioManager using RestTemplate provided.
  public static PortfolioManager getPortfolioManager(RestTemplate restTemplate) {

    return new PortfolioManagerImpl(restTemplate);
  }




}


package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.management.RuntimeErrorException;

import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {




  private RestTemplate restTemplate;

  // Caution: Do not delete or modify the constructor, or else your build will
  // break!
  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // Now we want to convert our code into a module, so we will not call it from main anymore.
  // Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  // into #calculateAnnualizedReturn function here and make sure that it
  // follows the method signature.
  // Logic to read Json file and convert them into Objects will not be required further as our
  // clients will take care of it, going forward.
  // Test your code using Junits provided.
  // Make sure that all of the tests inside PortfolioManagerTest using command below -
  // ./gradlew test --tests PortfolioManagerTest
  // This will guard you against any regressions.
  // run ./gradlew build in order to test yout code, and make sure that
  // the tests and static code quality pass.

  //CHECKSTYLE:OFF





  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }


  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
      LocalDate endDate) throws JsonProcessingException
  {

    List<AnnualizedReturn> annualReturn = new ArrayList<>();

    

    for (PortfolioTrade portfolioTrade : portfolioTrades) {

    
    List <TiingoCandle>  tiingoCandles = getStockQuote(portfolioTrade.getSymbol() , portfolioTrade.getPurchaseDate(), endDate);
    
    int no = tiingoCandles.size();
    int flag=1;

    if(portfolioTrade.getPurchaseDate().isAfter(endDate))
    {
      flag = 0;

    }
    
  
    LocalDate nodata;
    if(((tiingoCandles.get(no-1)).getDate()).equals(endDate))
    {
       nodata = endDate;
    }

    else if(flag == 0)
    {
      nodata = LocalDate.now();
      
    }

    else
    {
      nodata = tiingoCandles.get(no-1).getDate();

    }
    
  
      double sellprice = tiingoCandles.stream()
         .filter(candle -> candle.getDate().equals(endDate)
         || candle.getDate().equals(nodata))
         .findFirst().get().getClose();

      //System.out.println(sellprice);
    double buyprice = tiingoCandles.stream()
          .filter(candle -> candle.getDate().equals(portfolioTrade.getPurchaseDate()) )
          .findFirst().get().getOpen();
      //System.out.println(buyprice);


    //System.out.println(buyPrice);
    //System.out.println(sellPrice);  
    double totalReturn = (sellprice - buyprice) / buyprice;
    double totalnumdays = ChronoUnit.DAYS.between(portfolioTrade.getPurchaseDate(),endDate);
    //System.out.println(totalnumdays);
    double totalnumyears = totalnumdays / 365;
    double inv = 1 / totalnumyears;
    //System.out.println(totalnumyears);
    //System.out.println(totalReturn);
    double annualizedreturns = Math.pow((1 + totalReturn),inv) - 1;
    //System.out.println(annualizedreturns);
    
    AnnualizedReturn turn = new AnnualizedReturn(portfolioTrade.getSymbol(),annualizedreturns, totalReturn);

    annualReturn.add(turn);

    

    
    
    }
    annualReturn.sort(Comparator.comparing(AnnualizedReturn::getAnnualizedReturn));
    Collections.reverse(annualReturn);
    

    return annualReturn;



  }


  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }
  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo thirdparty APIs to a separate function.
  //  It should be split into fto parts.
  //  Part#1 - Prepare the Url to call Tiingo based on a template constant,
  //  by replacing the placeholders.
  //  Constant should look like
  //  https://api.tiingo.com/tiingo/daily/<ticker>/prices?startDate=?&endDate=?&token=?
  //  Where ? are replaced with something similar to <ticker> and then actual url produced by
  //  replacing the placeholders with actual parameters.


  public List<TiingoCandle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException {
  
      ObjectMapper mapper = getObjectMapper();
      //RestTemplate restTemplate = new RestTemplate();
      //PortfolioManagerImpl(restTemplate);
      if(to.isBefore(from))
      {
        to=LocalDate.now();
      }
      String url = buildUri(symbol, from, to);
      String result = restTemplate.getForObject(url, String.class);

      

     return Arrays.asList(mapper.readValue(result, TiingoCandle[].class));
        
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
       String uriTemplate = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
            + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
      
       String token = "671b3c89b9edaf3aec0390fe4ecf1b2aeb62afb5";
       String url = uriTemplate.replace("$APIKEY", token).replace("$SYMBOL", symbol)
          .replace("$STARTDATE", startDate.toString())
          .replace("$ENDDATE", endDate.toString());
       
       

       return url;

  }


  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Modify the function #getStockQuote and start delegating to calls to
  //  stockQuoteService provided via newly added constructor of the class.
  //  You also have a liberty to completely get rid of that function itself, however, make sure
  //  that you do not delete the #getStockQuote function.

}

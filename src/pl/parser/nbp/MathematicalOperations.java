package pl.parser.nbp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

public class MathematicalOperations {



    public static BigDecimal getAverageRate(List<BigDecimal> decimalList){
        BigDecimal length = new BigDecimal(decimalList.size());
        BigDecimal sum = BigDecimal.ZERO;
        for(BigDecimal dec : decimalList)
            sum = sum.add(dec);

        BigDecimal avg =  sum.divide(length,10,RoundingMode.HALF_UP);
        return avg;
    }


    public static BigDecimal getStandardDeviation(List<BigDecimal> decimalList, BigDecimal avg){
        BigDecimal length = new BigDecimal(decimalList.size());
        List<BigDecimal> list = decimalList.stream()
                                           .map(n -> n.subtract(avg).pow(2))
                                           .collect(Collectors.toList());

        BigDecimal sum = BigDecimal.ZERO;
        for(BigDecimal b : list){
            sum = sum.add(b);
        }

        BigDecimal standardDeviation =  sqrt(sum.divide(length, 10, RoundingMode.HALF_UP)).setScale(4,RoundingMode.HALF_UP);
        return standardDeviation;
    }


    public static BigDecimal sqrt(BigDecimal x) {
        return BigDecimal.valueOf(StrictMath.sqrt(x.doubleValue()));
    }

}

package com.example.lezh1k.sensordatacollector;

import com.example.lezh1k.sensordatacollector.SensorsAux.DeviationCalculator;

import org.junit.Test;

import java.util.Random;

import static junit.framework.Assert.assertTrue;

/**
 * Created by lezh1k on 12/19/17.
 */

public class DeviationsCalculatorTest {

    @Test
    public void SigmasTest() throws Exception {
        final int measurementCount = 1000000;
        final double eps = 1.0 / measurementCount;
        final int valuesCount = 1;
        Random rnd = new Random();
        DeviationCalculator c = new DeviationCalculator(measurementCount, valuesCount);

        for (int j = 0; j < valuesCount; ++j) {
            double sigma = rnd.nextDouble();
            double sigma2 = sigma*sigma;
            double base = rnd.nextDouble();
            for (int i = 0; i <= measurementCount; ++i) {
                c.Measure(base + sigma * ((i & 0x01) == 0 ? -1.0 : 1.0));
            }
            assertTrue(Math.abs(Math.abs(sigma2) - Math.abs(c.getSigmas()[j])) < eps);
        }
    }
}

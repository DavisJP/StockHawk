package com.udacity.stockhawk.sync;

import android.content.Context;

import com.udacity.stockhawk.R;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * @Author Davis Miyashiro
 */
public class QuoteSyncJobTest {

    @Mock
    Context mMockContext;

    @Mock
    YahooFinance yahooFinance;

    QuoteSyncJob quoteSyncJob;
    static String[] defaultStocksList = {"AAPL", "YHOO", "MSFT", "FB"};
    static Map<String, Stock> quotes = new HashMap<>();

    @Before
    public void setupQuoteSyncJob () {

        MockitoAnnotations.initMocks(this);
        //quoteSyncJob = new QuoteSyncJob(mMockContext);

    }

    @Test
    public void getQuotes() throws Exception {
        when(YahooFinance.get(defaultStocksList)).thenReturn(quotes);

        QuoteSyncJob.getQuotes(mMockContext);


    }

}
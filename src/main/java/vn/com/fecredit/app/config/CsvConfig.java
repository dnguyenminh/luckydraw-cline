package vn.com.fecredit.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.opencsv.CSVParserBuilder;

@Configuration
public class CsvConfig {

    @Bean
    public CSVParserBuilder csvParserBuilder() {
        return new CSVParserBuilder()
                .withSeparator(',')
                .withQuoteChar('"')
                .withIgnoreQuotations(false)
                .withIgnoreLeadingWhiteSpace(true);
    }

//    @Bean
//    public CSVReaderBuilder csvReaderBuilder(CSVParserBuilder parserBuilder) {
//        return new CSVReaderBuilder(null)
//                .withCSVParser(parserBuilder.build())
//                .withSkipLines(1); // Skip header row
//    }
}
package vn.com.fecredit.app.config;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
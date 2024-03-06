package com.example.salesinfo.batch;

import com.example.salesinfo.batch.processor.SalesInfoItemProcessor;
import com.example.salesinfo.dto.SalesInfoDTO;
import com.example.salesinfo.entity.SalesInfo;
import com.example.salesinfo.partition.RowRangePartitioner;
import jakarta.persistence.EntityManagerFactory;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.kafka.KafkaItemWriter;
import org.springframework.batch.item.kafka.builder.KafkaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static org.apache.logging.log4j.message.MapMessage.MapFormat.names;


@Configuration
@RequiredArgsConstructor
public class SpringBatchSalesInfoConfig {

    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final SalesInfoItemProcessor salesInfoItemProcessor;
    private final KafkaTemplate<String,SalesInfo> salesInfoKafkaTemplate;

//    Reader
    @Bean
    public FlatFileItemReader<SalesInfoDTO> salesInfoFileReader(){
        return  new FlatFileItemReaderBuilder<SalesInfoDTO>()
                .resource(new ClassPathResource("/data/product_spring_batch.csv"))
                .name("salesInfoFileReader")
                .delimited()
                .delimiter(",")
                .names("Product","Seller","SellerId","Price","City","Category")
                .linesToSkip(1 )
                .targetType(SalesInfoDTO.class)
                .build();
    }

    @Bean
    public JpaItemWriter<SalesInfo> salesInfoJpaItemWriter(){
        return new JpaItemWriterBuilder<SalesInfo>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("step", jobRepository)
                .<SalesInfoDTO, Future<SalesInfo>>chunk(250,platformTransactionManager)
                .reader(salesInfoFileReader())
                .processor(asyncItemProcessor())
                .writer(asyncItemWriter())
                .taskExecutor(taskExecutor())
                .build();

    }


    @Bean
    public Job importSalesInfo(){
        return new JobBuilder("salesInfoJob", jobRepository)
                .start(step(jobRepository,platformTransactionManager))
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor(){
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("Thread N-> :");
        return executor;
    }

    @Bean
    public AsyncItemProcessor<SalesInfoDTO,SalesInfo> asyncItemProcessor(){
        var asyncItemProcessor = new AsyncItemProcessor<SalesInfoDTO,SalesInfo>();
        asyncItemProcessor.setDelegate(salesInfoItemProcessor);
        asyncItemProcessor.setTaskExecutor(taskExecutor());
        return asyncItemProcessor;
    }

    @Bean
    public AsyncItemWriter<SalesInfo> asyncItemWriter(){
        var asyncItemWriter = new AsyncItemWriter<SalesInfo>();
        asyncItemWriter.setDelegate(stringSalesInfoKafkaItemWriter());
        return asyncItemWriter;
    }
    
    @Bean
    public KafkaItemWriter<String,SalesInfo> stringSalesInfoKafkaItemWriter(){

        return new KafkaItemWriterBuilder<String,SalesInfo>()
                .itemKeyMapper(salesInfo->String.valueOf(salesInfo.getSellerId()))
                .kafkaTemplate(salesInfoKafkaTemplate)
                .build();
        }

    @Bean
    public RowRangePartitioner rowRangePartitioner(){
        return new RowRangePartitioner();
    }

    @Bean
    public PartitionHandler partitionHandler(){
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setGridSize(4);
        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
        taskExecutorPartitionHandler.setStep(step(jobRepository,platformTransactionManager));
        return taskExecutorPartitionHandler;
    }

}




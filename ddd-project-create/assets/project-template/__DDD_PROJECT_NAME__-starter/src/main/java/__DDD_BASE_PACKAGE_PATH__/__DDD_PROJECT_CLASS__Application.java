package __DDD_BASE_PACKAGE__;

import __DDD_BASE_PACKAGE__.infra.sampleorder.persistence.mapper.SampleOrderMapper;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackageClasses = SampleOrderMapper.class)
@EnableDubbo(scanBasePackages = "__DDD_BASE_PACKAGE__.trigger.rpc")
public class __DDD_PROJECT_CLASS__Application {

    public static void main(String[] args) {
        SpringApplication.run(__DDD_PROJECT_CLASS__Application.class, args);
    }
}

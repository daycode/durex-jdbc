package cn.daycode.configuration.custom;

import cn.daycode.orm.Mapper;
import cn.daycode.orm.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

class CustomQueryRegistrar implements ImportBeanDefinitionRegistrar {

    private Logger logger = LoggerFactory.getLogger(CustomQueryRegistrar.class);

    CustomQueryRegistrar() {
    }

    protected Class<? extends Annotation> getAnnotation() {
        return EnableCustomQuery.class;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(EnableCustomQuery.class.getName()));
        String directory = attributes.getString("directory");
        String suffix = attributes.getString("suffix");

        //加载自定义SQL语句
        loadSQL(Scanner.getFiles(directory, suffix));

    }

    private void loadSQL(List<File> files) {
        for(File file : files) {
            List<String> lines = null;
            try {
                lines = Files.readAllLines(Paths.get(file.getPath()));
                boolean skipNext = false;
                for(int index = 0; index < lines.size(); index++) {
                    if(lines.get(index).trim().isEmpty()) {
                        continue;
                    }

                    if(skipNext) {
                        skipNext = false;
                        continue;
                    }
                    if(lines.get(index).startsWith("--")) {
                        Mapper.sql(lines.get(index).replace("--", ""), lines.get(index + 1));
                        skipNext = true;
                    }
                }
            } catch (IOException e) {
                logger.error("读取文件失败", e);
            }
            logger.info("load " + file.getName() + " finish");
        }

    }
}

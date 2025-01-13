package com.stremio.addon.service.searcher;

import com.stremio.addon.controller.dto.TorrentSearcher;
import com.stremio.addon.model.SearchEngineModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TorrentSearcherFactoryImpl implements TorrentSearcherFactory {

    private final AutowireCapableBeanFactory beanFactory;

    @Autowired
    public TorrentSearcherFactoryImpl(ApplicationContext applicationContext) {
        this.beanFactory = applicationContext.getAutowireCapableBeanFactory();
    }

    @Override
    public TorrentSearcherStrategy getSearcher(String type, SearchEngineModel torrentSearcher) {

        // Obtener el nombre del bean basado en el tipo de buscador
        String beanName = type + torrentSearcher.getType();
        log.info("Getting the bean [{}]...", beanName);
        Class<?> beanClass = getBeanClass(beanName);

        if (beanClass == null) {
            throw new RuntimeException("No bean found for type: " + beanName);
        }

        TorrentSearcherStrategy strategyInstance = (TorrentSearcherStrategy) beanFactory.createBean(
                beanClass, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, false
        );

        strategyInstance.initialize(torrentSearcher); // Asignación del objeto TorrentSearcher

        return strategyInstance;
    }

    private Class<?> getBeanClass(String beanName) {
        try {
            return beanFactory.getBean(beanName).getClass();
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving bean class for " + beanName, e);
        }
    }
}
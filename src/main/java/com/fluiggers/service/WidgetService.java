package com.fluiggers.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import javax.servlet.ServletContext;

import com.fluiggers.dto.WidgetDto;
import com.fluiggers.repository.WidgetRepository;

public class WidgetService {

    public List<WidgetDto> findAll() throws Exception {
        return new WidgetRepository().findAll();
    }

    public FileInputStream getWidgetFileInputStream(
        ServletContext servletContext,
        String filename
    ) throws FileNotFoundException {
        File widgetFile = new File(getWidgetPath(servletContext, filename));

        if (!widgetFile.exists() || !widgetFile.isFile()) {
            throw new FileNotFoundException();
        }

        return new FileInputStream(widgetFile);
    }

    private String getWidgetPath(ServletContext servletContext, String filename) {
        return servletContext
            .getRealPath("/")
            .replaceAll("^(.+appserver).*", "$1")
            + File.separator
            + "apps"
            + File.separator
            + filename
        ;
    }
}

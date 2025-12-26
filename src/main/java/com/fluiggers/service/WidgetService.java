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
        var repository = new WidgetRepository();
        return repository.findAll();
    }

    public FileInputStream getWidgetFileInputStream(
        ServletContext context,
        String filename
    ) throws FileNotFoundException {
        File widgetFile = new File(getAppWidgetsPath(context) + File.separator + filename);

        if (!widgetFile.exists() || !widgetFile.isFile()) {
            throw new FileNotFoundException();
        }

        return new FileInputStream(widgetFile);
    }

    private String getAppWidgetsPath(ServletContext servletContext) {
        return servletContext
            .getRealPath("/")
            .replaceAll("^(.+appserver).*", "$1")
            + File.separator
            + "apps"
        ;
    }
}

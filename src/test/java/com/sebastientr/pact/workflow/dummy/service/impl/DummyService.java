package com.sebastientr.workflow.dummy.service.impl;

import com.sebastientr.workflow.dummy.service.IDummyService;
import org.springframework.stereotype.Service;

@Service
public class DummyService implements IDummyService {
    @Override
    public String first(String arg) {
        return "first - %s".formatted(arg);
    }

    @Override
    public String second(String arg) {
        return "second - %s".formatted(arg);
    }

    @Override
    public String third(String arg) {
        return "third - %s".formatted(arg);
    }
}

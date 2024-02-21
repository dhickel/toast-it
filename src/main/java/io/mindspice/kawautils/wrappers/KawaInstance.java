package io.mindspice.kawautils.wrappers;

import gnu.kawa.io.InPort;
import kawa.standard.Scheme;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;


public class KawaInstance extends Scheme {
    private Set<String> userDefinitions = new HashSet<>();

//    public KawaInstance() {
////        loadSchemeFile(new File("kawa-functional-aliases.scm"));
////        loadSchemeFile(new File("stream-op.scm"));
//    }

    public void defineObject(String defName, Object object) {
        userDefinitions.add(defName);
        define(defName, object);
    }

    public List<String> userDefinitions() {
        List<String> defs = new ArrayList<>();
        getEnvironment().enumerateLocations().forEachRemaining(d ->
                defs.add(d.getKey().toString() + ":" + d.getValue())
        );
        getEnvironment().enumerateAllLocations().forEachRemaining( d-> {
            if (userDefinitions.contains(d.getKey().toString())) {
                defs.add(d.getKey().toString() + ":" + d.getValue());
            }
        });

        return defs.stream().sorted().distinct().toList();
    }

    public KawaResult<?> loadSchemeFile(File file) {
        try (Reader reader = new BufferedReader(new FileReader(file))) {
            return safeEval(reader);
        } catch (Exception e) {
            return new KawaResult<>(null, Optional.of(e));
        }
    }

    public <T> KawaResult<T> safeEval(String string) {
        try {
            Object result = eval(string);
            @SuppressWarnings("unchecked")
            T castedResult = (T) result;
            return new KawaResult<>(Optional.of(castedResult), Optional.empty());
        } catch (Throwable e) {
            return new KawaResult<T>(Optional.empty(), Optional.of(e));
        }
    }

    public <T> KawaResult<T> safeEval(Reader in) {
        try {
            Object result = eval(in);
            @SuppressWarnings("unchecked")
            T castedResult = (T) result;
            return new KawaResult<>(Optional.of(castedResult), Optional.empty());
        } catch (Throwable e) {
            return new KawaResult<T>(Optional.empty(), Optional.of(e));
        }
    }

    public <T> KawaResult<T> safeEval(InPort in) {
        try {
            Object result = eval(in);
            @SuppressWarnings("unchecked")
            T castedResult = (T) result;
            return new KawaResult<>(Optional.of(castedResult), Optional.empty());
        } catch (Throwable e) {
            return new KawaResult<T>(Optional.empty(), Optional.of(e));
        }
    }

    public <T> T castEval(String string) throws Throwable {
        Object result = eval(string);
        @SuppressWarnings("unchecked")
        T castedResult = (T) result;
        return castedResult;
    }

    public <T> T castEval(Reader in) throws Throwable {
        Object result = eval(in);
        @SuppressWarnings("unchecked")
        T castedResult = (T) result;
        return castedResult;
    }

    public <T> T castEval(InPort in) throws Throwable {
        Object result = eval(in);
        @SuppressWarnings("unchecked")
        T castedResult = (T) result;
        return castedResult;
    }


}

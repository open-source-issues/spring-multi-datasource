package com.foo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ResultCaptor<T, R> implements Answer<T> {

    @Getter
    private final Collection<R> results = new ArrayList<>();

    private final Function<T, R> getResultFunction;

    @Override
    public T answer(InvocationOnMock invocationOnMock) throws Throwable {
        @SuppressWarnings("unchecked")
        var result = (T) invocationOnMock.callRealMethod();
        results.add(getResultFunction.apply(result));
        return result;
    }
}

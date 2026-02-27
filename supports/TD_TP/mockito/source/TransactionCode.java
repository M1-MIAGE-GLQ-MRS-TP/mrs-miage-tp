package fr.amu.univ.miage.m1.glq.mockito;

import java.util.function.Function;

@FunctionalInterface
public interface TransactionCode<T> extends Function<EmployeeRepository, T> {

}

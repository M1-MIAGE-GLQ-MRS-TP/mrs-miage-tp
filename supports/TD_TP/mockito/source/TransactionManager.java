package fr.amu.univ.miage.m1.glq.mockito;

public interface TransactionManager {

	<T> T doInTransaction(TransactionCode<T> code);

}

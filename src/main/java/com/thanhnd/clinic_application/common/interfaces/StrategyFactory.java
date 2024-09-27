package com.thanhnd.clinic_application.common.interfaces;

public abstract class StrategyFactory<T> {
	abstract public T getStrategy(String strategy);
}

package com.ardublock.ui;

public class Worker extends Thread {
	private final Process process;
	Integer exit;

	Worker(Process process) {
		this.process = process;
	}

	public void run() {
		try {
			exit = process.waitFor();
		} catch (InterruptedException ignore) {
			return;
		}
	}
}
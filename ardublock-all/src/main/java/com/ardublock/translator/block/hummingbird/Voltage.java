package com.ardublock.translator.block.hummingbird;

import com.ardublock.translator.Translator;
import com.ardublock.translator.block.TranslatorBlock;
import com.ardublock.translator.block.exception.SocketNullException;
import com.ardublock.translator.block.exception.SubroutineNotDeclaredException;

public class Voltage extends TranslatorBlock {

	public Voltage(Long blockId, Translator translator, String codePrefix,
			String codeSuffix, String label) {
		super(blockId, translator, codePrefix, codeSuffix, label);
	}

	@Override
	public String toCode() throws SocketNullException,
			SubroutineNotDeclaredException {
		translator.addHeaderFile("Hummingbird.h");
		translator.addDefinitionCommand("Hummingbird bird;");
		translator.addSetupCommand("bird.init();");
		String ret = "bird.readInputVoltage()";
		return codePrefix+ret+codeSuffix;
	}

}
